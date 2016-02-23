package com.genie.simplerss.classes;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Common tools to work with lists, archives etc.
 */
public final class Tools {
    public final static String TAG = "Tools";

    /**
     * Check that device is online
     * 
     * @param context
     * @param networkType
     *            ConnectivityManager.TYPE_WIFI or ConnectivityManager.TYPE_MOBILE etc.
     * @return
     */
    public static boolean isOnline(Context context, int networkType) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getNetworkInfo(networkType);
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check that device is online
     * 
     * @param context
     * @return
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns external storage write state
     * 
     * @return externalStorageWriteable
     */
    public static boolean isExternalStorageWritable() {
        boolean externalStorageWriteable = false;

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            externalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            externalStorageWriteable = false;
        } else {
            externalStorageWriteable = false;
        }

        return externalStorageWriteable;
    }

    /**
     * Returns is external storage available
     * 
     * @return externalStorageAvailable
     */
    public static boolean isExternalStorageAvailable() {
        boolean externalStorageAvailable = false;

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            externalStorageAvailable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            externalStorageAvailable = true;
        } else {
            externalStorageAvailable = false;
        }

        return externalStorageAvailable;
    }

    /**
     * Shows little toast message in new thread
     * 
     * @param context
     * @param message
     *            Message text
     * @param interval
     *            Time interval to show toast (Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
     */
    public static void showToast(final Context context, final String message, final int interval) {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast toast = Toast.makeText(context, message, interval);
                toast.show();
                Looper.loop();
            }

        }.start();
    }

    public static void showToast(final Context context, final String message) {
        Tools.showToast(context, message, Toast.LENGTH_SHORT);
    }

    /**
     * Shows little toast message in new thread
     * 
     * @param context
     * @param message
     *            Message string id
     */
    public static void showToast(final Context context, final int message) {
        Tools.showToast(context, context.getString(message), Toast.LENGTH_SHORT);
    }

    public static void showAlert(final Context context, final String title, final String message) {
        new AlertDialog.Builder(context).setTitle(title).setMessage(message)
                .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false).create().show();
    }

    public static void setPrefString(Context context, String version, String id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putString(id, version);
        editor.commit();
    }

    public static String getPrefString(Context context, String id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(id, "");
    }

    public static void clearPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * Returns array of JSONObjects with "id"
     * 
     * @param ja
     *            JSONArray to parse in array of JSONObjects
     * @return Array of JSONObjects
     * @throws JSONException
     */
    public static List<JSONObject> parseJSONArray(JSONArray ja) throws JSONException {
        return parseJSONArray(ja, true);
    }

    /**
     * Returns array of JSONObjects with "id"
     * 
     * @param ja
     *            JSONArray to parse in array of JSONObjects
     * @param isId
     *            Should we check id parameter on adding?
     * @return Array of JSONObjects
     * @throws JSONException
     */
    public static List<JSONObject> parseJSONArray(JSONArray ja, boolean isId) throws JSONException {
        List<JSONObject> items = new ArrayList<JSONObject>();

        for (int i = 0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);
            if (isId) {
                if (jo.has("id")) { // Check mandatory argument and add item to list
                    items.add(jo);
                }
            } else {
                items.add(jo);
            }
        }

        return items;
    }

    /**
     * Returns array of JSONObjects with "id"
     * 
     * @param ja
     *            JSONArray to parse in array of JSONObjects
     * @param addJo
     *            Additional element to add to the top of list
     * @return Array of JSONObjects
     * @throws JSONException
     */
    public static List<JSONObject> parseJSONArray(JSONArray ja, JSONObject addJo)
            throws JSONException {
        List<JSONObject> items = new ArrayList<JSONObject>();

        // Add additional element to the top of list
        if (addJo.has("id")) { // Check mandatory argument and add item to list
            items.add(addJo);
        }

        for (int i = 0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);
            if (jo.has("id")) { // Check mandatory argument and add item to list
                items.add(jo);
            }
        }

        return items;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // Should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}