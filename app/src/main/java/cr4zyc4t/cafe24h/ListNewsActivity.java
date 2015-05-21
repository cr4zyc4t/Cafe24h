

package cr4zyc4t.cafe24h;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cr4zyc4t.cafe24h.adapter.CacheFragmentStatePagerAdapter;
import cr4zyc4t.cafe24h.model.Category;
import cr4zyc4t.cafe24h.util.Configs;
import cr4zyc4t.cafe24h.util.Utils;
import cr4zyc4t.cafe24h.widget.MySlidingTabLayout;


public class ListNewsActivity extends AppCompatActivity implements ListNewsFragment.ContentScrollListenter {
    private MySlidingTabLayout tabBar;
    private List<Integer> colors = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    private LinearLayout mToolbarContainer;
    private Toolbar toolbar;
    private CategoryPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private SharedPreferences sharedPreferences;

    private int mToolbarHeight;
    private float scrollDistance = 0;
    private int headerOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_news);

        setUpActionBar();

        initColorStyle();

        String serverResponse = getIntent().getStringExtra("serverResponse");
        try {
            JSONObject responseObject = new JSONObject(serverResponse);
            JSONArray categories = responseObject.getJSONArray("list_category");
            for (int i = 0; i < categories.length(); i++) {
                JSONObject ctgObject = categories.getJSONObject(i);
                Category newCategory = new Category(ctgObject.getInt("id"), ctgObject.getString("title"));
                newCategory.setStyleColor(colors.get(categoryList.size() % colors.size()));
                categoryList.add(newCategory);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        pagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPadding(viewPager.getPaddingLeft(), Utils.getTabBarHeight(this), viewPager.getPaddingRight(), viewPager.getPaddingBottom());

        tabBar = (MySlidingTabLayout) findViewById(R.id.sliding_tabs);
        tabBar.setViewPager(viewPager);
        tabBar.setDistributeEvenly(true);
        tabBar.setCustomTabColorizer(new MySlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.WHITE;
            }
        });
        tabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset > 0f && position < (categoryList.size() - 1)) {
                    int current_color = Utils.blendColors(colors.get((position + 1) % colors.size()), colors.get(position % colors.size()), positionOffset);
                    setStyleColor(current_color);
                }
            }

            @Override
            public void onPageSelected(int i) {
                fixTopPadding();
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        mToolbarContainer = (LinearLayout) findViewById(R.id.toolbar_container);

        setStyleColor(categoryList.get(0).getStyleColor());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void fixTopPadding() {
        scrollDistance = ActionbarIsShown() ? 0 : mToolbarHeight;
        pagerAdapter.setScrollY((int) scrollDistance);

        // Set scrollY for the active fragments
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            // Skip current item
//            if (i == mPager.getCurrentItem()) {
//                continue;
//            }

            // Skip destroyed or not created item
            Fragment f = pagerAdapter.getItemAt(i);
            if (f == null) {
                continue;
            }

            View view = f.getView();
            if (view == null) {
                continue;
            }

            RecyclerView scrollContent = (RecyclerView) view.findViewById(R.id.list_news_container);
            if (scrollContent == null) {
                continue;
            }

            LinearLayoutManager layoutManager = (LinearLayoutManager) scrollContent.getLayoutManager();
            if (headerOffset > 0) {
                if (scrollContent.computeVerticalScrollOffset() == 0)
                    layoutManager.scrollToPositionWithOffset(0, -mToolbarHeight);
            } else {
                if (scrollContent.computeVerticalScrollOffset() <= mToolbarHeight)
                    layoutManager.scrollToPositionWithOffset(0, 0);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_news, menu);
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
            startActivity(new Intent(ListNewsActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        new AlertDialog.Builder(this).setMessage("Are you sure to exit?").setCancelable(false)
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        ListNewsActivity.this.supportFinishAfterTransition();
//                    }
//                }).setNegativeButton("No", null)
//                .show();
        super.onBackPressed();
    }

    @Override
    public void onContentScroll(int dy) {
        headerOffset += dy;
        if (headerOffset < 0) headerOffset = 0;
        if (headerOffset > mToolbarHeight) headerOffset = mToolbarHeight;
        mToolbarContainer.setTranslationY(-headerOffset);

        if (scrollDistance < 0) {
            scrollDistance = 0;
        } else {
            scrollDistance += dy;
        }
    }

    @Override
    public void onContentScrollStateChange(int state) {
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            if (scrollDistance < mToolbarHeight) {
                showActionBar();
            } else {
                if (headerOffset > 0.7f * mToolbarHeight) {
                    hideActionBar();
                } else {
                    showActionBar();
                }
            }
        }
    }

    public class CategoryPagerAdapter extends CacheFragmentStatePagerAdapter {
        private int mScrollY;

        public CategoryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setScrollY(int mScrollY) {
            this.mScrollY = mScrollY;
        }

        @Override
        protected Fragment createItem(int position) {
            ListNewsFragment fragment;
            if (mScrollY > 0) {
                fragment = ListNewsFragment.newInstance(Configs.CATEGORY_TYPE,
                        categoryList.get(position).getId(), categoryList.get(position).getStyleColor(), mScrollY);
            } else {
                fragment = ListNewsFragment.newInstance(Configs.CATEGORY_TYPE,
                        categoryList.get(position).getId(), categoryList.get(position).getStyleColor());
            }
            fragment.setContentScrollListenter(ListNewsActivity.this);
            return fragment;
        }

        @Override
        public int getCount() {
            return categoryList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return categoryList.get(position).getTitle();
        }
    }

    private void setStyleColor(int c) {
        Utils.setStyleColor(c, this);
        tabBar.setBackgroundColor(c);
    }

    private void initColorStyle() {
        //Initial Value
        colors.add(getResources().getColor(R.color.bg_1));
        colors.add(getResources().getColor(R.color.bg_2));
        colors.add(getResources().getColor(R.color.bg_3));
        colors.add(getResources().getColor(R.color.bg_4));
        colors.add(getResources().getColor(R.color.bg_5));
        colors.add(getResources().getColor(R.color.bg_6));
    }

    private void setUpActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        mToolbarHeight = Utils.getToolbarHeight(this);

        // Setup
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_cafe);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    private void showActionBar() {
        headerOffset = 0;
        mToolbarContainer.animate().cancel();
        mToolbarContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    private void hideActionBar() {
        headerOffset = mToolbarHeight;
        mToolbarContainer.animate().cancel();
        mToolbarContainer.animate().translationY(-mToolbarHeight).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private boolean ActionbarIsShown() {
        return mToolbarContainer.getTranslationY() == 0;
    }

    private boolean ActionbarIsHidden() {
        return mToolbarContainer.getTranslationY() == -mToolbarHeight;
    }
}
