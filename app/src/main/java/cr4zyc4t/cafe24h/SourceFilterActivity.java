package cr4zyc4t.cafe24h;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cr4zyc4t.cafe24h.util.Configs;


public class SourceFilterActivity extends AppCompatActivity {
    private int source_id;
    private int own_color;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_filter);

        source_id = getIntent().getIntExtra("source_id", 0);
        own_color = getIntent().getIntExtra("color", getResources().getColor(R.color.primary));

        if((savedInstanceState == null) && (source_id != 0)){
            ListNewsFragment listNewsFragment = ListNewsFragment.newInstance(Configs.SOURCE_TYPE, source_id, own_color);

            getSupportFragmentManager().beginTransaction().replace(R.id.source_filter_fragment, listNewsFragment).commit();
        }
    }
}
