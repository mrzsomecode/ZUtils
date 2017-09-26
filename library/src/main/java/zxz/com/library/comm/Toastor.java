package zxz.com.library.comm;

import android.content.Context;
import android.widget.Toast;


/**
 * Created by Administrator on 2017/5/2.
 */

public class Toastor {
    private static Toast toast = null;

    // 长吐司
    public static void longToast(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        }
        toast.setText(text);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    // 短吐司
    public static void shortToast(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        }
        toast.setText(text);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void shortErr(Context context) {
        shortToast(context, "网络异常");
    }

    public static void shortToast(Context context, int textId) {
        if (toast == null) {
            toast = Toast.makeText(context, context.getString(textId), Toast.LENGTH_SHORT);
        }
        toast.setText(context.getString(textId));
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
