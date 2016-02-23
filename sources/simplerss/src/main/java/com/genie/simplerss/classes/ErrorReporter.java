package com.genie.simplerss.classes;

import android.content.Context;
import android.util.Log;

import com.genie.simplerss.R;

/**
 * Error reporter for unhandled exceptions
 */
public class ErrorReporter implements Thread.UncaughtExceptionHandler {
    public static final String TAG = "ErrorReporter";

    private static final int SLEEP = 3000; // The time to sleep in milliseconds to show alert message

    private static Context sContext;

    public static void installReporter(Context appContext) {
        try {
            Thread.setDefaultUncaughtExceptionHandler(new ErrorReporter());
            sContext = appContext;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            Tools.showToast(sContext, sContext.getString(R.string.msg_error));
            Thread.sleep(SLEEP);
        } catch (InterruptedException e) {
            if (Constants.ENABLE_DEBUG) {
                e.printStackTrace();
                Log.w(TAG, e.getClass().getName());
            }
        } finally {
            ex.printStackTrace();

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }
}