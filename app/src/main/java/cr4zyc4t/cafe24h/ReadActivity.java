package cr4zyc4t.cafe24h;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cr4zyc4t.cafe24h.model.News;
import cr4zyc4t.cafe24h.util.Utils;
import cr4zyc4t.cafe24h.widget.ObservableScrollView;


public class ReadActivity extends AppCompatActivity implements ObservableScrollView.OnScrollListener {
    private final String FETCH_NEWS_URL = "http://content.amobi.vn/api/cafe24h/contentdetail";

    private ProgressBar progressBar;
    private Button buttonRetry;
    private WebView body;
    private ActionBar actionBar;
    private ObservableScrollView scrollView;

    private int mMinRawY = 0;
    private int mState = STATE.ONSCREEN;
    private int mQuickReturnHeight;
    private int mMaxScrollY;

    private Toolbar toolbar;
    private ImageView icon;
    private ScrollSettleHandler mScrollSettleHandler = new ScrollSettleHandler();

    private static class STATE {
        public static final int ONSCREEN = 0;
        public static final int OFFSCREEN = 1;
        public static final int RETURNING = 2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        //Initial Values
        final News news = (News) getIntent().getSerializableExtra("news");
        int style_color = getIntent().getIntExtra("color", getResources().getColor(R.color.primary));
        Utils.setStyleColor(style_color, this);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        icon = (ImageView) findViewById(R.id.icon);
        ImageView source_icon = (ImageView) findViewById(R.id.source_icon);
        TextView title = (TextView) findViewById(R.id.title);
        TextView time = (TextView) findViewById(R.id.timestamp);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        buttonRetry = (Button) findViewById(R.id.buttonRetry);
        body = (WebView) findViewById(R.id.body);

        Log.i("Icon", "Width " + icon.getWidth());

        scrollView = (ObservableScrollView) findViewById(R.id.scrollView);
        scrollView.setOnScrollListener(this);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                onScrollChanged(scrollView.getScrollY());
                mMaxScrollY = scrollView.computeVerticalScrollRange()
                        - scrollView.getHeight();
                mQuickReturnHeight = toolbar.getHeight();
            }
        });

        ViewCompat.setTransitionName(title, "title");
        ViewCompat.setTransitionName(time, "time");
        ViewCompat.setTransitionName(icon, "icon");
        ViewCompat.setTransitionName(source_icon, "source");

        final int icon_width = Utils.getScreenWidth(this);
        Picasso.with(this).load(news.getIcon()).resize(icon_width, icon_width / 2).centerCrop().into(icon);
        if (URLUtil.isValidUrl(news.getSource_icon())) {
            Picasso.with(this).load(news.getSource_icon()).resize(128, 128).centerCrop().error(R.drawable.cafe24h_icon).into(source_icon);
        }

        title.setText(news.getTitle());
        time.setText(news.getTime());


        new getContentAsync().execute(new String[]{"" + news.getId()});

        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new getContentAsync().execute(new String[]{"" + news.getId()});
            }
        });
    }

    private void hideActionBar() {
        if (actionBar.isShowing()) {
            actionBar.hide();
        }
    }

    private void showActionBar() {
        if (!actionBar.isShowing()) {
            actionBar.show();
        }
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
        scrollY = Math.min(mMaxScrollY, scrollY);

        mScrollSettleHandler.onScroll(scrollY);

        int rawY = icon.getTop() - scrollY;
        int translationY = 0;

        switch (mState) {
            case STATE.OFFSCREEN:
                if (rawY <= mMinRawY) {
                    mMinRawY = rawY;
                } else {
                    mState = STATE.RETURNING;
                }
                translationY = rawY;
                break;

            case STATE.ONSCREEN:
                if (rawY < -mQuickReturnHeight) {
                    mState = STATE.OFFSCREEN;
                    mMinRawY = rawY;
                }
                translationY = rawY;
                break;

            case STATE.RETURNING:
                translationY = (rawY - mMinRawY) - mQuickReturnHeight;
                if (translationY > 0) {
                    translationY = 0;
                    mMinRawY = rawY - mQuickReturnHeight;
                }

                if (rawY > 0) {
                    mState = STATE.ONSCREEN;
                    translationY = rawY;
                }

                if (translationY < -mQuickReturnHeight) {
                    mState = STATE.OFFSCREEN;
                    mMinRawY = rawY;
                }
                break;
        }
        toolbar.animate().cancel();
        toolbar.setTranslationY(translationY + scrollY);
    }

    @Override
    public void onDownMotionEvent() {
        mScrollSettleHandler.setSettleEnabled(false);
    }

    @Override
    public void onUpOrCancelMotionEvent() {
        mScrollSettleHandler.setSettleEnabled(true);
        mScrollSettleHandler.onScroll(scrollView.getScrollY());
    }

    private class ScrollSettleHandler extends Handler {
        private static final int SETTLE_DELAY_MILLIS = 100;

        private int mSettledScrollY = Integer.MIN_VALUE;
        private boolean mSettleEnabled;

        public void onScroll(int scrollY) {
            if (mSettledScrollY != scrollY) {
                // Clear any pending messages and post delayed
                removeMessages(0);
                sendEmptyMessageDelayed(0, SETTLE_DELAY_MILLIS);
                mSettledScrollY = scrollY;
            }
        }

        public void setSettleEnabled(boolean settleEnabled) {
            mSettleEnabled = settleEnabled;
        }

        @Override
        public void handleMessage(Message msg) {
            // Handle the scroll settling.
            if (STATE.RETURNING == mState && mSettleEnabled) {
                int mDestTranslationY;
                if (mSettledScrollY - toolbar.getTranslationY() > mQuickReturnHeight / 2) {
                    mState = STATE.OFFSCREEN;
                    mDestTranslationY = Math.max(
                            mSettledScrollY - mQuickReturnHeight,
                            icon.getTop());
                } else {
                    mDestTranslationY = mSettledScrollY;
                }

                mMinRawY = icon.getTop() - mQuickReturnHeight - mDestTranslationY;
                toolbar.animate().translationY(mDestTranslationY);
            }
            mSettledScrollY = Integer.MIN_VALUE; // reset
        }
    }

    class getContentAsync extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            buttonRetry.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpGet request = new HttpGet(FETCH_NEWS_URL + "?content_id=" + params[0]);
//
//            try {
//                HttpResponse httpResponse = httpClient.execute(request);
//                InputStream inputStream = httpResponse.getEntity().getContent();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                StringBuilder stringBuilder = new StringBuilder();
//                String bufferedStrChunk = null;
//                while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
//                    stringBuilder.append(bufferedStrChunk);
//                }
//                return stringBuilder.toString();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
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
}
