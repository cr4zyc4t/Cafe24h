package cr4zyc4t.cafe24h;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import cr4zyc4t.cafe24h.util.Configs;
import cr4zyc4t.cafe24h.util.Utils;

/**
 * Created by Admin on 2015-05-30.
 */
public class SubcategoryFilterActivity extends AppCompatActivity {
    private String subcategory;
    private int subcategory_id;
    private int own_color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_filter);

        subcategory = getIntent().getStringExtra("subcategory");
        subcategory_id = getIntent().getIntExtra("subcategory_id", 0);
        own_color = getIntent().getIntExtra("color", getResources().getColor(R.color.primary));

        Utils.setStyleColor(own_color, this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(subcategory);

        if ((savedInstanceState == null) && (subcategory_id != 0)) {
            ListNewsFragment listNewsFragment = ListNewsFragment.newInstance(Configs.SUBCATEGORY_TYPE, subcategory_id, own_color);

            getSupportFragmentManager().beginTransaction().add(R.id.source_filter_fragment, listNewsFragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            ActivityCompat.finishAfterTransition(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
