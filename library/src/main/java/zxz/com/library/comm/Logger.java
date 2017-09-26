package zxz.com.library.comm;

import android.util.Log;

import zxz.com.library.BuildConfig;


/**
 * Created by zxz on 2017/8/24.
 */

public class Logger {
    public static boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "LoggerTest";

    public static void test(String s) {
        if (DEBUG && s != null)
            Log.e(TAG, s);
    }

    public static void e(String tag, String msg) {
        if (DEBUG && msg != null)
            Log.e(tag, msg);
    }
}
