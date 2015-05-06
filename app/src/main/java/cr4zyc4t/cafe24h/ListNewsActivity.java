

package cr4zyc4t.cafe24h;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cr4zyc4t.cafe24h.model.Category;
import cr4zyc4t.cafe24h.util.Utils;
import cr4zyc4t.cafe24h.widget.MySlidingTabLayout;


public class ListNewsActivity extends AppCompatActivity implements ListNewsFragment.onNewsScrolledListener {
    private MySlidingTabLayout tabBar;
    private CategoryPagerAdapter pagerAdapter;
    private List<Integer> colors = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    private boolean isBackTwice = false;

    private Toolbar toolbar;

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setMessage("Are you sure to exit?").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListNewsActivity.this.supportFinishAfterTransition();
                    }
                }).setNegativeButton("No", null)
                .show();
//        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_news);

        //Initial Value
        colors.add(getResources().getColor(R.color.bg_1));
        colors.add(getResources().getColor(R.color.bg_2));
        colors.add(getResources().getColor(R.color.bg_3));
        colors.add(getResources().getColor(R.color.bg_4));
        colors.add(getResources().getColor(R.color.bg_5));
        colors.add(getResources().getColor(R.color.bg_6));

        String serverResponse = getIntent().getStringExtra("serverResponse");
        try {
            JSONObject responseObject = new JSONObject(serverResponse);
            JSONArray categories = responseObject.getJSONArray("list_category");
            for (int i = 0; i < categories.length(); i++) {
                JSONObject ctgObject = categories.getJSONObject(i);
                Category newCategory = new Category(ctgObject.getInt("id"), ctgObject.getString("title"));
                Log.i("color", "" + R.color.bg_1);
                newCategory.setStyleColor(colors.get(categoryList.size() % colors.size()));
                categoryList.add(newCategory);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Setup
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(" Cafe24h");
        actionBar.setIcon(R.drawable.ic_cafe);
        actionBar.setDisplayShowHomeEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        pagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager());
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
    public void OnListNewsScrolled(int dx, int dy) {
        Log.i("Scroll", "X:" + dx + ", Y:" + dy);
//        int threshold = getResources().getDimensionPixelSize(R.dimen.scroll_threshold);
//        if (dy > threshold){
//            getSupportActionBar().hide();
//        }else if (dy < -threshold ){
//            getSupportActionBar().show();
//        }
    }

    public class CategoryPagerAdapter extends FragmentStatePagerAdapter {

        public CategoryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ListNewsFragment fragment = ListNewsFragment.newInstance(categoryList.get(position).getId(), categoryList.get(position).getStyleColor());
            fragment.setOnNewsScrolledListener(ListNewsActivity.this);
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
}
