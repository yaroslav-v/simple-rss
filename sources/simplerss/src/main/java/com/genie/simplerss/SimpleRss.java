package com.genie.simplerss;

import android.app.Application;
import android.os.Environment;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.util.AQUtility;
import com.genie.simplerss.classes.Constants;
import com.genie.simplerss.classes.ErrorReporter;
import com.genie.simplerss.classes.Tools;

import java.io.File;

/**
 * Base application class
 */
public class SimpleRss extends Application {

    @SuppressWarnings("unused")
    @Override
    public void onCreate() {
        // Set error reporter for unhandled exceptions
        if (!Constants.ENABLE_DEBUG) {
            ErrorReporter.installReporter(getApplicationContext());
        }

        // Set AQuery debug mode
        AQUtility.setDebug(Constants.ENABLE_DEBUG);

        // Set external disk cache directory
        File cacheDir = null;
        if (Tools.isExternalStorageWritable()
                && (Constants.ENABLE_DISK_CACHE || Constants.ENABLE_HTTP_DISK_CACHE)) {
            File ext = Environment.getExternalStorageDirectory();
            cacheDir = new File(ext, Constants.PACKAGE);
        }
        AQUtility.setCacheDir(cacheDir);

        // Set the max number of concurrent network connections, default is 4
        AjaxCallback.setNetworkLimit(Constants.AQ_NETWORK_LIMIT);

        // Set the max number of icons (image width <= 50) to be cached in memory, default is 20
        BitmapAjaxCallback.setIconCacheLimit(Constants.AQ_ICON_CACHE_LIMIT);

        // Set the max number of images (image width > 50) to be cached in memory, default is 20
        BitmapAjaxCallback.setCacheLimit(Constants.AQ_IMAGE_CACHE_LIMIT);

        // Set the max size of an image to be cached in memory, default is 1600 pixels (i.e. 400x400)
        BitmapAjaxCallback.setPixelLimit(Constants.AQ_IMAGE_PIXEL_LIMIT);

        // Set the max size of the memory cache, default is 1M pixels (4MB)
        BitmapAjaxCallback.setMaxPixelLimit(Constants.AQ_MEMORY_CACHE_LIMIT);

        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // Clear all memory cached images when system is in low memory
        BitmapAjaxCallback.clearCache();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}