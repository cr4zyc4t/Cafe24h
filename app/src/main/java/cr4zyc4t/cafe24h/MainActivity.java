package cr4zyc4t.cafe24h;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import cr4zyc4t.cafe24h.util.Configs;
import cr4zyc4t.cafe24h.util.Utils;


public class MainActivity extends AppCompatActivity {
    private String Response;
    private boolean splashFinish = false;
    private AsyncTask<Void, Void, String> asyncTask;
    private Handler counter;
    private Runnable finishSplash;
    private boolean isBackTwice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        asyncTask = new fetchCategories();
        asyncTask.execute();

        counter = new Handler();
        finishSplash = new Runnable() {
            @Override
            public void run() {
                splashFinish = true;
                NextActivity();
            }
        };
        counter.postDelayed(finishSplash, Configs.SPLASH_TIME);
    }

    private void NextActivity() {
        if ((Response != null) && splashFinish) {
            Intent listNews = new Intent(MainActivity.this, ListNewsActivity.class);
            listNews.putExtra("serverResponse", Response);
            startActivity(listNews);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (isBackTwice) {
            asyncTask.cancel(true);
            counter.removeCallbacks(finishSplash);

            super.onBackPressed();
        } else {
            isBackTwice = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
        }
    }

    private class fetchCategories extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = Configs.GET_CATEGORY_URL;
            Log.i("Resquest", url);
            try {
                return Utils.StringRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Response = result;
                NextActivity();
            } else {
                Toast.makeText(MainActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
