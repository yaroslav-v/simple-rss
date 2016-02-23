package com.genie.simplerss;

import org.xml.sax.SAXException;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;
import com.genie.simplerss.classes.BaseActivity;
import com.genie.simplerss.classes.Constants;

public class ItemActivity extends BaseActivity {
    public static final String TAG = "ItemActivity";

    /**
     * Custom view for action bar
     */
    View mCustomActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.showHomeUp = true;
        this.showTitle = true;
        super.onCreate(savedInstanceState);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Set custom centered view for ActionBar
        final LayoutInflater mInflater = LayoutInflater.from(this);
        mCustomActionBar = mInflater.inflate(R.layout.bar, null);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(mCustomActionBar, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER));

        // Load default data
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ((TextView) mCustomActionBar.findViewById(R.id.bar)).setText(extras
                    .getString(Constants.ID_TITLE));

            processDataOnCreate(extras.getString(Constants.ID_DATA));
        }
    }

    @Override
    protected int getContainer() {
        return R.layout.activity_item;
    }

    @Override
    protected void httpAsyncRequest() {
        // Isn't used here
    }

    @Override
    protected void processData(String url, XmlDom xml) {
        // Isn't used here
    }

    @Override
    protected void processError(String url, AjaxStatus status) {
        // Isn't used here
    }

    protected void processDataOnCreate(String xml) {
        if (xml == null) {
            this.finish();
            return;
        }

        XmlDom item = null;
        try {
            item = new XmlDom(xml);
        } catch (SAXException e) {
            if (Constants.ENABLE_DEBUG) {
                e.printStackTrace();
            }
        }

        aq.id(R.id.item_title).text(item.text("title"));
        aq.id(R.id.item_published).text(
                String.format(this.getString(R.string.publication_date), item.text("pubDate")
                        .replaceFirst(" \\+\\d\\d\\d\\d", "")));
        
        // Setted html tags etc. to comply with standard for WebView. Setted max-width for images to fit the screen
        aq.id(R.id.item_description)
                .getWebView()
                .loadDataWithBaseURL(
                        null,
                        "<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><style>img { max-width: 100%; }</style></head><body>"
                                + item.text("description") + "</body></html>", "text/html",
                        "UTF-8", null);

        imageAsyncRequest(R.id.item_image, item.text("image"));
    }

}
