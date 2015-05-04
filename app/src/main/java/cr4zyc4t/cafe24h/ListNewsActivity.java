

package cr4zyc4t.cafe24h;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import cr4zyc4t.cafe24h.model.Category;
import cr4zyc4t.cafe24h.slidingtab.MySlidingTabLayout;
import cr4zyc4t.cafe24h.util.Utils;


public class ListNewsActivity extends AppCompatActivity {
    private MySlidingTabLayout tabBar;
    private CategoryPagerAdapter pagerAdapter;
    private int[] colorArray;
    private int current_color;
    private List<Category> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_news);

        categoryList = (List<Category>) getIntent().getSerializableExtra("categoryList");

        //Initial Value
        colorArray = getResources().getIntArray(R.array.color_style);

        // Setup
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

    public class CategoryPagerAdapter extends FragmentPagerAdapter {

        public CategoryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ListNewsFragment.newInstance("" + position, "2");
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return categoryList.get(position).getTitle();
        }
    }

    private void setStyleColor(int c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Utils.tintColor(c));
        }
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(c));
        tabBar.setBackgroundColor(c);
    }
}
