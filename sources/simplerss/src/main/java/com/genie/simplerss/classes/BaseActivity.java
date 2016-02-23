package com.genie.simplerss.classes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;
import com.androidquery.util.XmlDom;
import com.genie.simplerss.R;

import java.util.List;

/**
 * Base activity with implementation httpAsyncCallback and cleanCacheAsync
 */
abstract public class BaseActivity extends ActionBarActivity {
    public static final String TAG = "BaseActivity";

    // AQuery and UI variables
    protected AQuery aq; // Common AQuery object for root view
    protected AQuery mIAq; // Common AQuery object for personalized UI objects (views) etc.

    // ActionBar variables
    protected boolean showLogo = false;
    protected boolean showHomeUp = true;
    protected boolean showTitle = false;
    protected boolean showOptions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContainer());

        // Set defaults for layout
        aq = new AQuery(this);
        mIAq = new AQuery(this);

        // Set up the action bar
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(showHomeUp);
        actionBar.setDisplayUseLogoEnabled(showLogo);
        actionBar.setDisplayShowTitleEnabled(showTitle);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean the file cache with advance option
        if (isTaskRoot()) {
            // Starts cleaning when cache size is larger than AQ_DISK_CACHE_MAX_LIMIT and remove the least recently used
            // files until cache size is less than AQ_DISK_CACHE_MIN_LIMIT
            AQUtility.cleanCacheAsync(this, Constants.AQ_DISK_CACHE_MAX_LIMIT,
                    Constants.AQ_DISK_CACHE_MIN_LIMIT);

            // Disable all ajax calls
            aq.ajaxCancel();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (showOptions) {
            getMenuInflater().inflate(R.menu.main, menu);

            // Enable this if you want display search widget
//            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//            SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                if (Constants.ENABLE_DEBUG) {
                    Log.i(TAG, "onActivityResult: ok");
                }
            } else if (resultCode == RESULT_FIRST_USER) {
                if (Constants.ENABLE_DEBUG) {
                    Log.i(TAG, "onActivityResult: user");
                }
            } else if (resultCode == RESULT_CANCELED) {
                if (Constants.ENABLE_DEBUG) {
                    Log.i(TAG, "onActivityResult: canceled");
                }
            }
        }
    }

    /**
     * Returns current content view
     *
     * @return R.layout
     */
    abstract protected int getContainer();

    /**
     * Obtain data from server
     *
     * @param url    Requested url
     * @param xml    Response data
     * @param status Current request status
     */
    public void httpAsyncCallback(String url, XmlDom xml, AjaxStatus status) {
        // JSON processing
        if (xml != null) { // Successful ajax call
            if (Constants.ENABLE_DEBUG) {
                //                Log.i(TAG, "Response: " + xml.toString());
            }

            processData(url, xml);
        } else { // Error for http response code that's not 200-299, 422 for current application
            if (status.getCode() == AjaxStatus.NETWORK_ERROR) {
                if (Constants.ENABLE_DEBUG) {
                    Log.i(TAG, "Network error");
                }

                Tools.showToast(this, this.getString(R.string.msg_connect_error));
            } else if (status.getError() != null) {
                if (Constants.ENABLE_DEBUG) {
                    Log.i(TAG, "Error: " + status.getError());
                }

                String errorString = String.format(this.getString(R.string.error),
                        status.getCode(), status.getError());
                Tools.showToast(this, errorString);
            } else {
                if (Constants.ENABLE_DEBUG) {
                    Log.i(TAG, "Error: server response is empty or server is silent");
                }

                Tools.showToast(this, this.getString(R.string.msg_server_error)); // Server didn't answered
            }

            processError(url, status);
        }
    }

    /**
     * Requests data for current activity
     */
    abstract protected void httpAsyncRequest();

    /**
     * Request image icon and set this to view
     *
     * @param resId        Icon holder Id
     * @param thumbnailUrl Image url
     */
    protected void imageAsyncRequest(int resId, String thumbnailUrl) {
        Bitmap preset = mIAq.getCachedImage(thumbnailUrl);
        if (preset != null) {
            mIAq.id(resId).image(preset, AQuery.RATIO_PRESERVE);
        } else {
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            final int width = size.x;

            // Use AQuery.RATIO_PRESERVE if want to use flexible layout like wrap_content for images
            mIAq.id(resId).image(thumbnailUrl, Constants.ENABLE_MEMORY_CACHE,
                    Constants.ENABLE_DISK_CACHE, width, R.drawable.network_error, null,
                    AQuery.FADE_IN, AQuery.RATIO_PRESERVE);
        }
    }

    protected void imageRequest(int resId, int placeholderId) {
        mIAq.id(resId).image(placeholderId);
    }

    /**
     * Process data from server
     *
     * @param url Requested url
     * @param xml Response data
     * @return Processing status: TRUE if handle and FALSE otherwise
     */
    abstract protected void processData(String url, XmlDom xml);

    abstract protected void processError(String url, AjaxStatus status);

    /**
     * Custom ArrayAdapter throws JSONException etc.
     *
     * @author CoMRaDe
     */
    abstract public class CatalogAdapter extends ArrayAdapter<XmlDom> {
        private final int mViewItem;

        public CatalogAdapter(Context context, int textViewResourceId, List<XmlDom> objects) {
            super(context, textViewResourceId, objects);

            mViewItem = textViewResourceId;
        }

        public void addAll(List<XmlDom> items) {
            for (XmlDom item : items) {
                add(item);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(mViewItem, null);
            }

            // Prepare internal views and catch JSON errors
            prepareView(position, convertView, parent);

            return convertView;
        }

        /**
         * Prepares internal views in convertView
         *
         * @param position
         * @param convertView
         */
        abstract public void prepareView(int position, View convertView, ViewGroup parent);
    }
}