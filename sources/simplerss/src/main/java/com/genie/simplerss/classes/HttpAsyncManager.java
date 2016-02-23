package com.genie.simplerss.classes;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;
import com.androidquery.util.XmlDom;
import com.genie.simplerss.R;

import java.util.Map;

/**
 * API handler for project service
 */
public final class HttpAsyncManager {
    public static final String TAG = "HttpAsyncManager";

    private static String sUserAgent = "";

    private static AjaxCallback<XmlDom> mCb; // Latest http request callback>

    /**
     * Returns default user-agent
     *
     * @param context
     * @return Default user-agent
     */
    private static String getDefaultUserAgent(Context context) {
        if (sUserAgent.length() == 0) {
            Resources res = context.getResources();

            // Attempt to find a name for this application
            String appName = context.getString(R.string.app_name);

            // Get application version name
            String appVersionName = "";
            int appVersionCode = 0;
            try {
                PackageInfo pinfo = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0);
                appVersionName = pinfo.versionName;
                appVersionCode = pinfo.versionCode;

                String deviceName = android.os.Build.MODEL;
                String OSVersion = android.os.Build.VERSION.RELEASE;
                String locale = res.getConfiguration().locale.getDisplayName();

                // To encode data can be used Charset.encode
                sUserAgent = String.format(res.getString(R.string.fmt_user_agent), appName,
                        appVersionName, appVersionCode, deviceName, Constants.OS, OSVersion, locale);
            } catch (NameNotFoundException e) {
                if (Constants.ENABLE_DEBUG) {
                    e.printStackTrace();
                    Log.w(TAG, e.getClass().getName());
                }

                return context.getString(R.string.app_name) + Constants.OS;
            }
        }

        return sUserAgent;
    }

    /**
     * Calls async http request without post body (GET requests)
     *
     * @param httpMethod Http request method
     * @param request    Request string
     * @param context
     * @param callback
     */
    private static void callHttpRequest(String httpMethod, String request, Context context,
                                        String callback) {
        Map<String, Object> post = null;

        callHttpRequest(httpMethod, request, post, context, callback, context);
    }

    /**
     * Calls async http request with post body
     *
     * @param httpMethod Http request method
     * @param request    Request string
     * @param post       Post body
     * @param context
     * @param callback
     */
    private static void callHttpRequest(String httpMethod, String request,
                                        Map<String, Object> post, Context context, String callback) {
        callHttpRequest(httpMethod, request, post, context, callback, context);
    }

    /**
     * Calls async http request
     *
     * @param httpMethod Http request method
     * @param request    Request string
     * @param post       Post body
     * @param context
     * @param callback
     * @param handler
     */
    private static void callHttpRequest(String httpMethod, String request,
                                        Map<String, Object> post, Context context, String callback, Object handler) {
        // Check isOnline and show error message if isn't for the 1st failure
        if (Tools.isOnline(context)) {
            AQuery aq = new AQuery(context);

            // To encode request can be used URLEncoder.encode ('#' -> %23)
            mCb = new AjaxCallback<XmlDom>();
            mCb.url(request).type(XmlDom.class)
                    .encoding(Constants.HTTP_ENCODING).weakHandler(handler, callback)
                    .fileCache(Constants.ENABLE_HTTP_DISK_CACHE)
                    .expire(Constants.AQ_DISK_CACHE_EXPIRE);

            mCb.header("User-Agent", getDefaultUserAgent(context));
            mCb.header("Accept", Constants.CONTENT_TYPE_JSON);

            // Support for PUT and DELETE methods
            int method = com.androidquery.util.Constants.METHOD_GET;
            if (httpMethod.equals(Constants.HTTP_METHOD_POST)) {
                method = com.androidquery.util.Constants.METHOD_POST;
            } else if (httpMethod.equals(Constants.HTTP_METHOD_PUT)) {
                method = com.androidquery.util.Constants.METHOD_PUT;
            } else if (httpMethod.equals(Constants.HTTP_METHOD_DELETE)) {
                method = com.androidquery.util.Constants.METHOD_DELETE;
            }
            mCb.method(method);

            // Prepare POST/PUT/DELETE request body
            if ((method != com.androidquery.util.Constants.METHOD_GET) && (post != null)) {
                if (Constants.ENABLE_DEBUG) {
                    Log.i(TAG, "Data: " + post.toString());
                }
                mCb.params(post);
            }

            // Set type of callback: UI or nonUI, indicate the ajax request should use the main ui thread for callback
            if (!(handler instanceof Context)) {
                mCb.uiCallback(false);
            } else {
                mCb.uiCallback(true);
            }

            // Main call
            aq.progress(context).ajax(mCb);
        } else {
            final Class<?>[] DEFAULT_SIG = {String.class, Object.class, AjaxStatus.class};
            final Class<?>[] AJAX_SIG = {String.class, XmlDom.class, AjaxStatus.class};
            AQUtility.invokeHandler(handler, callback, true, true, AJAX_SIG, DEFAULT_SIG, request,
                    null,
                    new AjaxStatus(AjaxStatus.NETWORK_ERROR, context
                            .getString(R.string.msg_connect_error)));
        }
    }

    public static void getFeed(String feedUrl, Context context, String callback) {
        if (feedUrl != null) {
            callHttpRequest(Constants.HTTP_METHOD_GET, feedUrl, context, callback);
        }
    }

}