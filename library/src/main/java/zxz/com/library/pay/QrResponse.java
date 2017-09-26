package zxz.com.library.pay;

/**
 * 支付宝返回二维码
 * Created by Administrator on 2017/1/4.
 */

public class QrResponse {
    @Override
    public String toString() {
        return "QrResponse{" +
                "alipay_trade_precreate_response=" + alipay_trade_precreate_response +
                ", sign='" + sign + '\'' +
                '}';
    }

    private AlipayTradePrecreateResponseBean alipay_trade_precreate_response;
    private String sign;

    public AlipayTradePrecreateResponseBean getAlipay_trade_precreate_response() {
        return alipay_trade_precreate_response;
    }

    public void setAlipay_trade_precreate_response(AlipayTradePrecreateResponseBean alipay_trade_precreate_response) {
        this.alipay_trade_precreate_response = alipay_trade_precreate_response;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public static class AlipayTradePrecreateResponseBean {
        @Override
        public String toString() {
            return "AlipayTradePrecreateResponseBean{" +
                    "code='" + code + '\'' +
                    ", msg='" + msg + '\'' +
                    ", out_trade_no='" + out_trade_no + '\'' +
                    ", qr_code='" + qr_code + '\'' +
                    '}';
        }

        private String code;
        private String msg;
        private String out_trade_no;
        private String qr_code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getOut_trade_no() {
            return out_trade_no;
        }

        public void setOut_trade_no(String out_trade_no) {
            this.out_trade_no = out_trade_no;
        }

        public String getQr_code() {
            return qr_code;
        }

        public void setQr_code(String qr_code) {
            this.qr_code = qr_code;
        }
    }
}
