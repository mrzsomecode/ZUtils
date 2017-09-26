package zxz.com.library.pay;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import zxz.com.library.comm.Logger;
import zxz.com.library.comm.Toastor;
import zxz.com.library.encrypt.MD5;
import zxz.com.library.pay.callback.CodeCallBack;
import zxz.com.library.pay.callback.QueryCallBack;
import zxz.com.library.pay.utils.Http;


public class WxPayUtil {
    /**
     * 你的服务器接受微信支付回调的网页，获取支付结果
     */
    private String url_order_up;
    /**
     * 支付金额
     */
    private int money;
    /**
     * 商品信息，标题
     */
    private String title;
    /**
     * 附加数据
     */
    private String attach;

    //沙箱环境
    private String sandBoxPort = "sandboxnew/";
    private boolean isSandBox = false;
    private GetPrepayListener mGetPrepayListener;

    public void setGetPrepayListener(GetPrepayListener getPrepayListener) {
        mGetPrepayListener = getPrepayListener;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public String getUrl_order_up() {
        return url_order_up;
    }

    public void setUrl_order_up(String url_order_up) {
        this.url_order_up = url_order_up;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser_ip() {
        return user_ip;
    }

    public void setUser_ip(String user_ip) {
        this.user_ip = user_ip;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String payNum) {
        this.out_trade_no = payNum;
    }

    /**
     * 用户的ip
     */
    private String user_ip;
    /**
     * 安卓传入 固定APP
     */
    private String type;
    /**
     * 微信开放平台对应APP的id
     */
    public String APP_ID;
    /**
     * 微信开放平台商户平台给你的密钥
     */
    public String API_KEY;
    /**
     * 微信商户号
     */
    public String MCH_ID;
    /**
     * 保存取得预订单号的信息
     */
    private Map<String, String> resultunifiedorder;
    /**
     * 微信支付類
     */
    private PayReq req;
    /**
     * 商户订单号
     */
    private String out_trade_no;
    /**
     * 微信
     */
    private IWXAPI msgApi;
    private Context context;
    private CodeCallBack mCodeCallBack;
    private static final String TAG = "WxPayUtil";

    public void setCodeCallBack(CodeCallBack codeCallBack) {
        mCodeCallBack = codeCallBack;
    }

    /**
     * 传入所需要的参数
     *
     * @param aPP_ID  APP_ID
     * @param aPI_KEY aPI_KEY
     * @param mCH_ID  MCH_ID
     * @param context
     */
    public WxPayUtil(String aPP_ID, String aPI_KEY, String mCH_ID,
                     Context context) {
        this.context = context;
        APP_ID = aPP_ID;
        API_KEY = aPI_KEY;
        MCH_ID = mCH_ID;
        registApi();
        if (!isSandBox) {
            sandBoxPort = "";
        }
    }

    /**
     * 向微信注册你的APP，如果不註冊微信將拒絕
     */
    private void registApi() {
        if (context != null) {
            msgApi = WXAPIFactory.createWXAPI(context, null);
            msgApi.registerApp(APP_ID);
        }
    }


    public Map<String, String> decodeXml(String content) {
        Logger.e(TAG, "decodeXml: " + content);
        try {
            Map<String, String> xml = new HashMap<String, String>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if ("xml".equals(nodeName) == false) {
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }
            return xml;
        } catch (Exception e) {
            Logger.e(TAG, "decodeXml: " + e.toString());
        }
        return null;
    }

    //统一下单参数
    private SortedMap getParams() {
        SortedMap<String, Object> parameterMap = new TreeMap<>();
        parameterMap.put("appid", APP_ID);
        if (!TextUtils.isEmpty(attach))
            parameterMap.put("attach", attach);
        parameterMap.put("mch_id", MCH_ID);
        parameterMap.put("nonce_str", genNonceStr());
        parameterMap.put("body", title);
        parameterMap.put("out_trade_no", out_trade_no);
        parameterMap.put("total_fee", money + "");
        parameterMap.put("spbill_create_ip", user_ip);
        parameterMap.put("notify_url", url_order_up);
        //native扫码支付,APP登录支付
        parameterMap.put("trade_type", mCodeCallBack != null ? "NATIVE" : "APP");
        parameterMap.put("sign", createSign(parameterMap));
        return parameterMap;
    }

    private String genNonceStr() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
                .getBytes());
    }

    private long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    //请求参数 转xml
    public static String getRequestXml(SortedMap<String, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            sb.append("<" + key + ">" + value + "</" + key + ">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    //生成签名
    public String createSign(SortedMap<String, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v)
                    && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + API_KEY);
        String sign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        return sign;
    }

    private void genPayReq() {
        if (resultunifiedorder == null)
            return;
        req = new PayReq();
        req.appId = APP_ID;
        req.partnerId = MCH_ID;
        req.prepayId = resultunifiedorder.get("prepay_id");
        req.packageValue = "Sign=WXPay";
        req.nonceStr = genNonceStr();
        req.timeStamp = String.valueOf(genTimeStamp());
        SortedMap signParams = new TreeMap();
        signParams.put("appid", req.appId);
        signParams.put("noncestr", req.nonceStr);
        signParams.put("package", req.packageValue);
        signParams.put("partnerid", req.partnerId);
        signParams.put("prepayid", req.prepayId);
        signParams.put("timestamp", req.timeStamp);
        req.sign = createSign(signParams);
        Logger.e(TAG, "genPayReq: " + signParams.toString());

    }

    /**
     * 开启订单
     *
     * @param isSend 是否直接发送订单 <br/>
     *               传false只获得预订单号，需手动调用 {@link #sendPayReq()}
     */
    public void start(boolean isSend) {
        if (mCodeCallBack != null || msgApi.isWXAppInstalled()) {
            if (isSandBox) {
                SandBoxTask sb = new SandBoxTask();
                sb.sendPay = isSend;
                sb.execute();
            } else {
                GetPrepayIdTask gpt = new GetPrepayIdTask();
                gpt.sendPay = isSend;
                gpt.execute();
            }
        } else {
            Toastor.shortToast(context, "未安装微信客户端,请安装！");
        }
    }

    /**
     * 如果你之前start方法传入的flase调用该方法，提交之前未完成订单
     */
    public void sendPayReq() {
        if (resultunifiedorder == null) {
            return;
        }
        genPayReq();
        msgApi.sendReq(req);
    }


    //查询订单
    public String getQueryPrams() {
        SortedMap<String, Object> parameterMap = new TreeMap<>();
        parameterMap.put("appid", APP_ID);
        parameterMap.put("mch_id", MCH_ID);
        parameterMap.put("nonce_str", genNonceStr());
        parameterMap.put("out_trade_no", out_trade_no);
        String sign = createSign(parameterMap);
        parameterMap.put("sign", sign);
        return getRequestXml(parameterMap);
    }

    private QueryCallBack mQueryCallBack;

    public void setQueryCallBack(QueryCallBack queryCallBack) {
        mQueryCallBack = queryCallBack;
    }

    public void query() {
        QueryOrderTask queryOrderTask = new QueryOrderTask();
        queryOrderTask.execute();
    }

    //查询订单状态
    private class QueryOrderTask extends AsyncTask<String, String, Map<String, String>> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            resultunifiedorder = result;
            if (mQueryCallBack != null)
                if (resultunifiedorder.containsKey("trade_state"))
                    mQueryCallBack.orderStatus(resultunifiedorder.get("trade_state"));
                else
                    mQueryCallBack.orderStatus("FAIL");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Map<String, String> doInBackground(String... params) {
            String requestXML = getQueryPrams();
            Logger.e(TAG, "doInBackground: " + requestXML);
            Log.e("requestXML", requestXML);
            String result = Http.httpsRequest(
                    "https://api.mch.weixin.qq.com/" + sandBoxPort + "pay/orderquery", "POST",
                    requestXML);
            Logger.e(TAG, "doInBackground: " + result);
            Map<String, String> xml = decodeXml(result);
            return xml;
        }
    }

    private class GetPrepayIdTask extends AsyncTask<String, String, Map<String, String>> {
        public boolean sendPay;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            resultunifiedorder = result;
            // 提交订单
            if (resultunifiedorder != null) {
                if (mCodeCallBack == null) {
                    if (mGetPrepayListener != null)
                        mGetPrepayListener.onSuccess(result);
                    if (sendPay)
                        sendPayReq();
                } else
                    mCodeCallBack.returnCode(resultunifiedorder.get("code_url"));
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Map<String, String> doInBackground(String... params) {
            String requestXML = getRequestXml(getParams());
            Logger.e(TAG, "doInBackground: " + requestXML);
            String result = Http.httpsRequest("https://api.mch.weixin.qq.com/" + sandBoxPort + "pay/unifiedorder", "POST", requestXML);
            Logger.e(TAG, "doInBackground: ---" + result);
            Map<String, String> xml = decodeXml(result);
            return xml;
        }
    }

    public interface GetPrepayListener {
        void onSuccess(Map<String, String> result);
    }

    private class SandBoxTask extends AsyncTask<String, String, Map<String, String>> {
        public boolean sendPay;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            GetPrepayIdTask gpt = new GetPrepayIdTask();
            money = 301;
            gpt.sendPay = sendPay;
            gpt.execute();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Map<String, String> doInBackground(String... params) {
            SortedMap<String, Object> getsignkey = new TreeMap<>();
            getsignkey.put("mch_id", MCH_ID);
            getsignkey.put("nonce_str", genNonceStr());
            getsignkey.put("sign", createSign(getsignkey));
            String res = Http.httpsRequest("https://api.mch.weixin.qq.com/sandboxnew/pay/getsignkey", "POST", getRequestXml(getsignkey));
            Map<String, String> xml = decodeXml(res);
            Logger.e(TAG, "doInBackground: " + xml);
            API_KEY = xml.get("sandbox_signkey");
            return xml;
        }
    }

}
