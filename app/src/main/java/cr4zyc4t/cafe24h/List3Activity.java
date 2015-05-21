package cr4zyc4t.cafe24h;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;

import toanvq.atest_playstorebar.widget.SlidingTabLayout;


public class List3Activity extends AppCompatActivity implements RecylcerViewFragment.ContentScrollListenter {
    private View mHeaderView;
    private Toolbar mToolbarView;
    private ViewPager mPager;
    private NavigationAdapter mPagerAdapter;

    private float scrollDistance = 0;
    private int headerOffset = 0;
    private int mToolbarHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list3);

        mToolbarView = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbarView);

        mToolbarHeight = Utils.getToolbarHeight(this);

        mHeaderView = findViewById(R.id.header);
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mPager);

        // When the page is selected, other fragments' scrollY should be adjusted
        // according to the toolbar status(shown/hidden)
        slidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                fixPadding();
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list3, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void scrollStateChange(RecyclerView recyclerView, int state) {
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            if (scrollDistance < mToolbarHeight) {
                showToolbar();
            } else {
                if (headerOffset < 0.65f * mToolbarHeight) {
                    showToolbar();
                } else {
                    hideToolbar();
                }
            }
        }
    }

    @Override
    public void onScroll(RecyclerView recyclerView, int dx, int dy) {
        headerOffset += dy;
        if (headerOffset < 0) headerOffset = 0;
        if (headerOffset > mToolbarHeight) headerOffset = mToolbarHeight;
        mHeaderView.setTranslationY(-headerOffset);

        if (scrollDistance < 0) {
            scrollDistance = 0;
        } else {
            scrollDistance += dy;
        }
    }

    private void fixPadding() {
        scrollDistance = toolbarIsShown() ? 0 : mToolbarHeight;
        mPagerAdapter.setScrollY((int) scrollDistance);

        // Set scrollY for the active fragments
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            // Skip current item
//            if (i == mPager.getCurrentItem()) {
//                continue;
//            }

            // Skip destroyed or not created item
            Fragment f = mPagerAdapter.getItemAt(i);
            if (f == null) {
                continue;
            }

            View view = f.getView();
            if (view == null) {
                continue;
            }

            RecyclerView scrollContent = (RecyclerView) view.findViewById(R.id.scroll);
            if (scrollContent == null) {
                continue;
            }

            LinearLayoutManager layoutManager = (LinearLayoutManager) scrollContent.getLayoutManager();
            Log.i("offset", "" + headerOffset);
            if (headerOffset > 0) {
                if (scrollContent.computeVerticalScrollOffset() == 0)
                    layoutManager.scrollToPositionWithOffset(1, 0);
            }
        }
    }

    private boolean toolbarIsShown() {
        return mHeaderView.getTranslationY() == 0;
    }

    private boolean toolbarIsHidden() {
        return mHeaderView.getTranslationY() == -mToolbarHeight;
    }

    private void showToolbar() {
        headerOffset = 0;
        mHeaderView.animate().cancel();
        mHeaderView.animate().translationY(0).setDuration(200).start();
    }

    private void hideToolbar() {
        headerOffset = mToolbarHeight;
        mHeaderView.animate().cancel();
        mHeaderView.animate().translationY(-mToolbarHeight).setDuration(200).start();
    }

    private class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        private final String[] TITLES = new String[]{"Applepie", "Butter Cookie", "Cupcake", "Donut", "Eclair", "Froyo", "Gingerbread", "Honeycomb", "Ice Cream Sandwich", "Jelly Bean", "KitKat", "Lollipop"};

        private int mScrollY;

        public NavigationAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setScrollY(int scrollY) {
            mScrollY = scrollY;
        }

        @Override
        protected Fragment createItem(int position) {
            // Initialize fragments.
            // Please be sure to pass scroll position to each fragments using setArguments.
            RecylcerViewFragment f = new RecylcerViewFragment();
            f.setContentScrollListenter(List3Activity.this);
            if (mScrollY > 0) {
                Bundle args = new Bundle();
                args.putInt(RecylcerViewFragment.ARG_INITIAL_POSITION, 1);
                f.setArguments(args);
            }

            return f;
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }
}
