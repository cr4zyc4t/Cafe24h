package cr4zyc4t.cafe24h;

import android.content.Intent;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cr4zyc4t.cafe24h.model.News;
import cr4zyc4t.cafe24h.model.VolleySingleton;
import cr4zyc4t.cafe24h.util.Utils;
import cr4zyc4t.cafe24h.widget.ObservableScrollView;


public class ReadNewsActivity extends AppCompatActivity implements ObservableScrollView.OnScrollListener {
    private final String FETCH_NEWS_URL = "http://content.amobi.vn/api/cafe24h/contentdetail";

    private ProgressBar progressBar;
    private Button buttonRetry;
    private WebView body;
    private ObservableScrollView scrollView;
    private ImageView icon;
    private View stickyHeader;
    private View stickyHeaderElevation;
    private View placeholder;
    private int placeholder_top;
    private News news;
    private int own_color;

    private boolean is_header_has_shadow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_sticky);

        news = (News) getIntent().getSerializableExtra("news");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        own_color = getIntent().getIntExtra("color", getResources().getColor(R.color.primary));
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Utils.setStyleColor(own_color, this);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Utils.tintColor(own_color, this));
        }

        stickyHeader = findViewById(R.id.sticky_header_container);
        placeholder = findViewById(R.id.placeholder);
        stickyHeaderElevation = findViewById(R.id.sticky_header_elevation);

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

//        new getContentAsync().execute(news.getId());
        getContentDetail(news.getId());

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
            stickyHeader.setTranslationY(Math.max(placeholder_top, scrollY));

            if (placeholder_top < scrollY) {
                if (!is_header_has_shadow) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        stickyHeader.setElevation(getResources().getDimension(R.dimen.news_header_elevation));
                    } else {
                        stickyHeaderElevation.setBackgroundResource(R.drawable.sticky_header_elevation);
                    }

                    is_header_has_shadow = true;
                }
            } else {
                if (is_header_has_shadow) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        stickyHeader.setElevation(0);
                    } else {
                        stickyHeaderElevation.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }

                    is_header_has_shadow = false;
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

    private void getContentDetail(int content_id) {
        buttonRetry.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        String url = FETCH_NEWS_URL + "?content_id=" + content_id;

        JsonObjectRequest request = new JsonObjectRequest(JsonObjectRequest.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("Request", "Response " + response.toString());
                try {
                    body.loadData(response.getJSONObject("content_detail").getString("content"), "text/html; charset=utf-8", "UTF-8");

                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    buttonRetry.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                buttonRetry.setVisibility(View.VISIBLE);
                Log.e("Request", error.getMessage());
            }
        });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
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

            View firstColumn = findViewById(R.id.first_column);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(icon_width, FrameLayout.LayoutParams.MATCH_PARENT);
            firstColumn.setLayoutParams(layoutParams);

            View firstColumnPlaceholder = findViewById(R.id.first_column_placeholder);
            if (firstColumnPlaceholder != null) {
                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(icon_width, LinearLayout.LayoutParams.MATCH_PARENT);
                firstColumnPlaceholder.setLayoutParams(layoutParams2);
            }
        }
        LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(icon_width, (int) (icon_width * 0.5f));
        icon.setLayoutParams(iconLayoutParams);

        Picasso.with(this).load(news.getIcon()).resize(icon_width, (int) (icon_width * 0.5f)).centerCrop().placeholder(R.drawable.placeholder).into(icon);
        if (URLUtil.isValidUrl(news.getSource_icon())) {
            Picasso.with(this).load(news.getSource_icon()).resize(128, 128).centerCrop().error(R.drawable.cafe24h_icon).into(source_icon);
        }

        title.setText(news.getTitle());
        time.setText(Utils.calcTime(news.getTime()));
        description.setText(news.getDescription());

        source_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent filter = new Intent(ReadNewsActivity.this, SourceFilterActivity.class);
                filter.putExtra("color", own_color);
                filter.putExtra("source_id", news.getSource_id());
                filter.putExtra("source", news.getSource());
                startActivity(filter);
                finish();
            }
        });
    }
}
