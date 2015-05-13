package cr4zyc4t.cafe24h;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import cr4zyc4t.cafe24h.util.Configs;
import cr4zyc4t.cafe24h.util.Utils;


public class SourceFilterActivity extends AppCompatActivity {
    private String source;
    private int source_id;
    private int own_color;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_filter);

        source = getIntent().getStringExtra("source");
        source_id = getIntent().getIntExtra("source_id", 0);
        own_color = getIntent().getIntExtra("color", getResources().getColor(R.color.primary));

        Utils.setStyleColor(own_color, this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(source);

        if((savedInstanceState == null) && (source_id != 0)){
            ListNewsFragment listNewsFragment = ListNewsFragment.newInstance(Configs.SOURCE_TYPE, source_id, own_color);

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
