

package cr4zyc4t.cafe24h;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cr4zyc4t.cafe24h.model.Category;
import cr4zyc4t.cafe24h.util.Utils;
import cr4zyc4t.cafe24h.widget.HidingScrollListener;
import cr4zyc4t.cafe24h.widget.MySlidingTabLayout;


public class ListNewsActivity extends AppCompatActivity {
    private MySlidingTabLayout tabBar;
    private List<Integer> colors = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    private LinearLayout mToolbarContainer;
    private int mToolbarHeight;
    private HidingScrollListener hidingScrollListener;

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
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        CategoryPagerAdapter pagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

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

            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });



        mToolbarContainer = (LinearLayout) findViewById(R.id.toolbar_container);
        //Add hiding listener
        hidingScrollListener = new HidingScrollListener(this) {
            @Override
            public void onMoved(int distance) {
                mToolbarContainer.setTranslationY(-distance);
            }

            @Override
            public void onShow() {
                showActionBar();
            }

            @Override
            public void onHide() {
                hideActionBar();
            }
        };

        setStyleColor(categoryList.get(0).getStyleColor());
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

    public class CategoryPagerAdapter extends FragmentStatePagerAdapter {

        public CategoryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ListNewsFragment fragment = ListNewsFragment.newInstance(categoryList.get(position).getId(), categoryList.get(position).getStyleColor());
            if (hidingScrollListener != null) {
                fragment.setHidingScrollListener(hidingScrollListener);
            }
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        mToolbarHeight = Utils.getToolbarHeight(this);

        // Setup
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_cafe);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    private void showActionBar() {
        mToolbarContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    private void hideActionBar() {
        mToolbarContainer.animate().translationY(-mToolbarHeight).setInterpolator(new AccelerateInterpolator(2)).start();
    }
}
