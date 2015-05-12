package cr4zyc4t.cafe24h;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cr4zyc4t.cafe24h.model.News;
import cr4zyc4t.cafe24h.util.Utils;
import cr4zyc4t.cafe24h.widget.ObservableScrollView;


public class ReadNewsActivity extends AppCompatActivity implements ObservableScrollView.OnScrollListener {
    private final String FETCH_NEWS_URL = "http://content.amobi.vn/api/cafe24h/contentdetail";

    private ProgressBar progressBar;
    private Button buttonRetry;
    private WebView body;
    private ObservableScrollView scrollView;
    private ImageView icon;
    private LinearLayout stickyHeader;
    private View placeholder;
    private int placeholder_top;
    private News news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_sticky);

        news = (News) getIntent().getSerializableExtra("news");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            int style_color = getIntent().getIntExtra("color", getResources().getColor(R.color.primary));
            Utils.setStyleColor(style_color, this);
        }

        stickyHeader = (LinearLayout) findViewById(R.id.sticky_header);
        placeholder = findViewById(R.id.placeholder);

        icon = (ImageView) findViewById(R.id.icon);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        buttonRetry = (Button) findViewById(R.id.buttonRetry);
        body = (WebView) findViewById(R.id.body);

        body.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
        body.getSettings().setJavaScriptEnabled(false);

        scrollView = (ObservableScrollView) findViewById(R.id.scrollView);
        scrollView.setOnScrollListener(this);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                onScrollChanged(scrollView.getScrollY());
            }
        });

        initFirstView();

        new getContentAsync().execute(news.getId());

        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new getContentAsync().execute(news.getId());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            ActivityCompat.finishAfterTransition(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollChanged(int scrollY) {
        if (placeholder != null) {   // Only in tablet mode
            placeholder_top = placeholder.getTop();
            Log.i("Run", "placeholder top :" + placeholder_top + ", scrollY: " + scrollY);
            stickyHeader.setTranslationY(Math.max(placeholder_top, scrollY));

            if (placeholder_top < scrollY) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    stickyHeader.setElevation(getResources().getDimension(R.dimen.news_header_elevation));
                } else {
                    stickyHeader.setBackgroundResource(R.color.news_header_background_highlight);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    stickyHeader.setElevation(0);
                } else {
                    stickyHeader.setBackgroundResource(android.R.color.white);
                }
            }

            //Paralax icon :D
            if (scrollY <= (Utils.getScreenWidth(this) * 0.5f)) {
                icon.setTranslationY(scrollY * 0.5f);
            }
        }
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent() {
    }

    class getContentAsync extends AsyncTask<Integer, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            buttonRetry.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... params) {
            String url = FETCH_NEWS_URL + "?content_id=" + params[0];
            Log.d("Link", url);
            try {
                return Utils.StringRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            JSONObject response = null;
            if (result != null) {
                try {
                    response = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (response != null) {
                try {
                    body.loadData(response.getJSONObject("content_detail").getString("content"), "text/html; charset=utf-8", "UTF-8");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                buttonRetry.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.GONE);
        }
    }

    private void initFirstView() {
        //Initial Values
        TextView description = (TextView) findViewById(R.id.description);
        ImageView source_icon = (ImageView) findViewById(R.id.source_icon);
        TextView title = (TextView) findViewById(R.id.title);
        TextView time = (TextView) findViewById(R.id.timestamp);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.setTransitionName(title, "title");
            ViewCompat.setTransitionName(time, "time");
            ViewCompat.setTransitionName(icon, "icon");
            ViewCompat.setTransitionName(source_icon, "source");
        }

        int icon_width = Utils.getScreenWidth(this);
        if (placeholder == null) { //Tablet Mode
            icon_width = (int) (icon_width * 0.33f);

            LinearLayout firstColumn = (LinearLayout) findViewById(R.id.first_column);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(icon_width, LinearLayout.LayoutParams.MATCH_PARENT);
            firstColumn.setLayoutParams(layoutParams);

            LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(icon_width, (int) (icon_width * 0.5f));
            icon.setLayoutParams(iconLayoutParams);
        }

        Picasso.with(this).load(news.getIcon()).resize(icon_width, (int) (icon_width * 0.5f)).centerCrop().into(icon);
        if (URLUtil.isValidUrl(news.getSource_icon())) {
            Picasso.with(this).load(news.getSource_icon()).resize(128, 128).centerCrop().error(R.drawable.cafe24h_icon).into(source_icon);
        }

        title.setText(news.getTitle());
        time.setText(Utils.calcTime(news.getTime()));
        description.setText(news.getDescription());
    }
}
