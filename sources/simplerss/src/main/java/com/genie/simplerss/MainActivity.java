package com.genie.simplerss;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;
import com.genie.simplerss.classes.BaseActivity;
import com.genie.simplerss.classes.Constants;
import com.genie.simplerss.classes.HttpAsyncManager;

import java.util.List;

public class MainActivity extends BaseActivity implements ActionBar.TabListener,
        OnItemClickListener, OnClickListener, OnRefreshListener {
    public static final String TAG = "MainActivity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /**
     * Custom view for action bar
     */
    View mCustomActionBar;

    SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Latest requested url
     */
    String mUrlRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        this.showHomeUp = false;
        //        this.backResource = R.drawable.back_button;
        super.onCreate(savedInstanceState);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set custom centered view for ActionBar
        final LayoutInflater mInflater = LayoutInflater.from(this);
        mCustomActionBar = mInflater.inflate(R.layout.bar, null);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(mCustomActionBar, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER));

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Add custom view for tabs
            View customTabView = mInflater.inflate(R.layout.tab, null);
            customTabView.setLayoutParams(new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER));
            TextView tabText = (TextView) customTabView.findViewById(R.id.tab);
            tabText.setText(mSectionsPagerAdapter.getPageTitle(i));
            tabText.setCompoundDrawablesWithIntrinsicBounds(null, mSectionsPagerAdapter.getPageIcon(i), null, null);

            actionBar.addTab(actionBar.newTab().setCustomView(customTabView).setTabListener(this)
                    .setText(mSectionsPagerAdapter.getPageTitle(i)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // Prepare data for new tab
        mViewPager.setCurrentItem(tab.getPosition());
        ((TextView) mCustomActionBar.findViewById(R.id.bar)).setText(tab.getText());

        // Load data from network
        final PlaceholderFragment fragment = (PlaceholderFragment) mSectionsPagerAdapter
                .getRegisteredFragment(mViewPager.getCurrentItem());
        if (fragment == null) {
            // fragment == null for 1st loading when onTabSelected fired before other activities
            httpAsyncRequest();
        } else {
            final ListView lv = (ListView) fragment.getView().findViewById(R.id.items_list);
            if (lv.getChildCount() == 0) {
                final XmlDom xml = fragment.getXml();
                if (xml != null) {
                    mUrlRequested = mSectionsPagerAdapter.getPageLink(mViewPager.getCurrentItem());
                    this.processData(mUrlRequested, xml);
                } else {
                    httpAsyncRequest();
                }
            }
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // Do something here if necessary ...
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // Do something here if necessary ...
    }

    @Override
    protected int getContainer() {
        return R.layout.activity_main;
    }

    @Override
    protected void httpAsyncRequest() {
        mUrlRequested = mSectionsPagerAdapter.getPageLink(mViewPager.getCurrentItem());
        HttpAsyncManager.getFeed(mUrlRequested, this, "httpAsyncCallback");
    }

    @Override
    protected void processData(String url, XmlDom xml) {
        // Clean up states
        aq.id(R.id.network_error_image).clicked(null).gone();
        if (!url.equals(mUrlRequested) || xml == null) {
            return;
        }

        // Get current fragment in the pager list
        final PlaceholderFragment fragment = (PlaceholderFragment) mSectionsPagerAdapter
                .getRegisteredFragment(mViewPager.getCurrentItem());

        // Load data to LiestView
        List<XmlDom> items = xml.tags("item");
        ArrayAdapter<XmlDom> aa = new CatalogAdapter(this, R.layout.item, items) {
            @Override
            public void prepareView(int position, View convertView, ViewGroup parent) {
                XmlDom item = getItem(position);

                AQuery aqAdapter = mIAq.recycle(convertView);
                aqAdapter.id(R.id.item).tag(R.id.data, item);

                aqAdapter.id(R.id.item_title).text(item.text("title"));

                aqAdapter.id(R.id.item_share).clicked(MainActivity.this);

                if (aq.shouldDelay(position, convertView, parent, item.text("image"))) {
                    imageRequest(R.id.item_image, R.drawable.placeholder);
                } else {
                    if (!TextUtils.isEmpty(item.text("image"))) {
                        imageAsyncRequest(R.id.item_image, item.text("image"));
                    } else {
                        imageRequest(R.id.item_image, R.drawable.placeholder_image);
                    }
                }

                // Update NEW ribbon on items
                if (fragment.isItemSeen(position)) {
                    aqAdapter.id(R.id.item_new_ribbon).gone();
                } else {
                    aqAdapter.id(R.id.item_new_ribbon).visible();
                }
            }
        };

        // Setup list with data, store source xml etc.
        if (fragment != null) {
            fragment.setXml(xml);
            aq.id(fragment.getView().findViewById(R.id.items_list)).adapter(aa);

            mSwipeRefreshLayout = (SwipeRefreshLayout) fragment.getView().findViewById(
                    R.id.swipe_container);
            mSwipeRefreshLayout.setColorScheme(R.color.bar_grey, R.color.bar_grey,
                    R.color.grey, R.color.grey);
            mSwipeRefreshLayout.setOnRefreshListener(this);
        }
        aq.itemClicked(this);
    }

    @Override
    protected void processError(String url, AjaxStatus status) {
        if (status.getCode() == AjaxStatus.NETWORK_ERROR) {
            aq.id(R.id.network_error_image).visible().clicked(this);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            String[] titles = MainActivity.this.getResources().getStringArray(
                    R.array.config_feed_titles);
            return titles.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String[] titles = MainActivity.this.getResources().getStringArray(
                    R.array.config_feed_titles);
            return titles[position];
        }

        public Drawable getPageIcon(int position) {
            TypedArray icons = MainActivity.this.getResources().obtainTypedArray(
                    R.array.config_feed_icons);
            final Drawable icon = icons.getDrawable(position);
            icons.recycle();
            return icon;
        }

        public String getPageLink(int position) {
            TypedArray links = MainActivity.this.getResources().obtainTypedArray(
                    R.array.config_feeds);
            final String title = links.getString(position);
            links.recycle();
            return title;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private final SparseBooleanArray seenItems = new SparseBooleanArray();

        private XmlDom xml; // Loaded feed data 

        public boolean isItemSeen(int position) {
            return seenItems.get(position);
        }

        public void setItemSeen(int position) {
            seenItems.put(position, true);
        }

        public XmlDom getXml() {
            return xml;
        }

        public void setXml(XmlDom xml) {
            this.xml = xml;
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.items_list, container, false);
            return rootView;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_share:
                // Share message in apps
                XmlDom item = (XmlDom) ((View) v.getParent()).getTag(R.id.data);

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        String.format(this.getString(R.string.feed_sharing_message),
                                item.text("title"), item.text("link"), getString(R.string.app_name)));
                startActivity(Intent.createChooser(sharingIntent,
                        this.getString(R.string.feed_sharing_dialog)));
                break;
            case R.id.network_error_image:
                httpAsyncRequest();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.putExtra(Constants.ID_TITLE,
                mSectionsPagerAdapter.getPageTitle(mViewPager.getCurrentItem()));
        intent.putExtra(Constants.ID_DATA, view.findViewById(R.id.item).getTag(R.id.data)
                .toString());
        intent.setClass(this, ItemActivity.class);
        startActivity(intent);

        // Update NEW ribbon on items
        final PlaceholderFragment fragment = (PlaceholderFragment) mSectionsPagerAdapter
                .getRegisteredFragment(mViewPager.getCurrentItem());
        if (fragment != null) {
            fragment.setItemSeen(position);
            aq.id(view.findViewById(R.id.item_new_ribbon)).gone();
        }
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);

        final PlaceholderFragment fragment = (PlaceholderFragment) mSectionsPagerAdapter
                .getRegisteredFragment(mViewPager.getCurrentItem());
        if (fragment != null) {
            aq.id(fragment.getView().findViewById(R.id.items_list)).adapter(
                    new ArrayAdapter<XmlDom>(this, 0));
        }
        httpAsyncRequest();
    }

}
