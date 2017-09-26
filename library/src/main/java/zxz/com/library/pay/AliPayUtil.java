package zxz.com.library.pay;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.alipay.sdk.app.PayTask;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import zxz.com.library.comm.Logger;
import zxz.com.library.comm.GsonHelper;
import zxz.com.library.pay.callback.CodeCallBack;
import zxz.com.library.pay.callback.QueryCallBack;
import zxz.com.library.pay.utils.Http;
import zxz.com.library.pay.utils.OrderInfoUtil2_0;
import zxz.com.library.pay.utils.SignUtils;

/**
 * Created by hasee on 2016/4/6.
 */
public class AliPayUtil {
    private static final String TAG = "AliPayUtil";
    public static final int SDK_PAY_FLAG = 1;
    private Handler mHandler;
    private Activity context;

    /**
     * 你的服务器接受微信支付回调的网页，获取支付结果
     */
    private String url_order_up;
    /**
     * 支付金额
     */
    private double money;
    /**
     * 商品信息，标题
     */
    private String title;
    private String body;

    private String APP_ID = "";
    //    商户私钥
    private String RSA_PRIVATE = "";

    private boolean isRsa2 = false;

    private String port = "alipay";
    //    public String port = "alipaydev";


    /**
     * 自己的订单号
     */
    private String out_trade_no;
    private QueryCallBack mQueryCallBack;
    private CodeCallBack mCodeCallBack;

    public AliPayUtil(String APP_ID, String RSA_PRIVATE, boolean isRsa2, Activity context, Handler handler) {
        this.mHandler = handler;
        this.isRsa2 = isRsa2;
        this.context = context;
        this.APP_ID = APP_ID;
        this.RSA_PRIVATE = RSA_PRIVATE;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setQueryCallBack(QueryCallBack queryCallBack) {
        mQueryCallBack = queryCallBack;
    }

    public void setCodeCallBack(CodeCallBack codeCallBack) {
        mCodeCallBack = codeCallBack;
    }

    /**
     * 服务器回调地址
     *
     * @param url_order_up
     */
    public void setUrl_order_up(String url_order_up) {
        this.url_order_up = url_order_up;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 服务器订单id
     *
     * @param out_trade_no
     */
    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }


    /**
     * 查询订单状态
     */
    public void query() {
        String orderInfo = "";
        orderInfo += "app_id=" + APP_ID;
        orderInfo += "&biz_content={" +
                "    \"out_trade_no\":\"" + out_trade_no + "\"}";
        orderInfo += "&charset=utf-8";
        orderInfo += "&method=alipay.trade.query";
        orderInfo += "&sign_type=" + (isRsa2 ? "RSA2" : "RSA");
        orderInfo += "&timestamp=" + getDateTime();
        orderInfo = formatSign(orderInfo);
        QueryTask queryTask = new QueryTask();
        queryTask.execute(orderInfo);
    }

    public static String getDateTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date(System.currentTimeMillis()));
    }


    /**
     * 扫码支付
     */
    public void aliQrPay() {
        final String orderInfo = createOrderInfo();
        QrPayTask qrPayTask = new QrPayTask();
        qrPayTask.execute(orderInfo);
    }

    private String formatSign(String orderInfo) {
        String sign = SignUtils.sign(orderInfo, RSA_PRIVATE, isRsa2);
        try {
            /**
             * 仅需对sign 做URL编码
             */
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        orderInfo += "&sign=" + sign;
        return orderInfo;
    }

    public void payV2() {
        /**
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo的获取必须来自服务端；
         */
        final String orderInfo = createOrderInfo();
        payV2(orderInfo);
    }

    @NonNull
    private String createOrderInfo() {
        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APP_ID, true, title, money,
                out_trade_no, getDateTime(), url_order_up, body);
        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);
        String sign = OrderInfoUtil2_0.getSign(params, RSA_PRIVATE, isRsa2);
        return orderParam + "&" + sign;
    }

    public void payV2(final String orderInfo) {
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(context);
                Logger.test(orderInfo);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Logger.test(result.toString());
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    public String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    private class QrPayTask extends AsyncTask<String, String, QrResponse> {

        @Override
        protected void onPostExecute(QrResponse result) {
            if (mCodeCallBack != null && result != null)
                mCodeCallBack.returnCode(result.getAlipay_trade_precreate_response().getQr_code());
        }

        @Override
        protected QrResponse doInBackground(String... params) {
            String result = Http.httpsRequest(
                    "https://openapi." + port + ".com/gateway.do?" + params[0], "GET", null);
            return GsonHelper.fromJson(result, QrResponse.class);
        }
    }

    private class QueryTask extends AsyncTask<String, String, AliQueryResponse> {


        @Override
        protected void onPostExecute(AliQueryResponse result) {
            if (mQueryCallBack != null)
                mQueryCallBack.orderStatus(result.getAlipay_trade_query_response().getTrade_status());
        }

        @Override
        protected AliQueryResponse doInBackground(String... params) {
            String result = Http.httpsRequest(
                    "https://openapi." + port + ".com/gateway.do?" + params[0], "GET", null);
            return GsonHelper.fromJson(result, AliQueryResponse.class);
        }
    }
}
