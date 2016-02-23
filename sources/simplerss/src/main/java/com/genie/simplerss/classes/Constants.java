package com.genie.simplerss.classes;

import com.genie.simplerss.BuildConfig;

/**
 * Common constants
 */
public final class Constants {
    // Common
    public final static String PACKAGE = BuildConfig.APPLICATION_ID;
    public final static String OS = "Android";

    public final static boolean ENABLE_DEBUG = BuildConfig.DEBUG;
    public static final boolean ENABLE_MEMORY_CACHE = true; // Enable image memory cache
    public static final boolean ENABLE_DISK_CACHE = !BuildConfig.DEBUG; // Enable image disk cache
    public static final boolean ENABLE_HTTP_DISK_CACHE = !BuildConfig.DEBUG; // Enable http requests and file disk cache

    // Cache and network limits
    public static final int AQ_NETWORK_LIMIT = 4; // Set the max number of concurrent network connections, default is 4
    public static final int AQ_ICON_CACHE_LIMIT = 50; // Set the max number of icons (image width <= 50) to be cached in
                                                      // memory, default is 20
    public static final int AQ_IMAGE_CACHE_LIMIT = 30; // Set the max number of images (image width > 50) to be cached in
                                                 // memory, default is 20
    public static final int AQ_IMAGE_PIXEL_LIMIT = 800 * 800; // Set the max size of an image to be cached in memory, default
                                                        // is 1600 pixels (i.e. 400x400)
    public static final int AQ_MEMORY_CACHE_LIMIT = 10000000; // Set the max size of the memory cache, default is 1M pixels
                                                          // (4MB)
    public static final int AQ_DISK_CACHE_MAX_LIMIT = 6000000; // Set the max size of the disk cache
    public static final int AQ_DISK_CACHE_MIN_LIMIT = 3000000; // Set the min size of the disk cache
    public static final int AQ_DISK_CACHE_EXPIRE = 86400; // Set the expire time for disk cache (86400 seconds - 1 day)

    // Network constants
    public static final String CONTENT_TYPE_JSON = "application/xml";
    public static final String HTTP_ENCODING = "UTF-8";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_DELETE = "DELETE";

    // Constants
    public static final int UNDEFINED = -1;

    public static final String ID_TITLE = PACKAGE + ".title";
    public static final String ID_DATA = PACKAGE + ".data";

    public static final int LAUNCH_TIMEOUT = 500;
}
