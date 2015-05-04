package cr4zyc4t.cafe24h;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.IOException;

import cr4zyc4t.cafe24h.util.Configs;
import cr4zyc4t.cafe24h.util.Utils;


public class MainActivity extends AppCompatActivity {
    private String Response;
    private boolean splashFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new fetchCategories().execute();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashFinish = true;
                NextActivity();
            }
        }, Configs.SPLASH_MINTIME);
    }

    private void NextActivity() {
        if ((Response != null) && splashFinish) {
            Intent listNews = new Intent(MainActivity.this, ListNewsActivity.class);
            listNews.putExtra("serverResponse", Response);
            startActivity(listNews);
            finish();
        }
    }

    private class fetchCategories extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String url = Configs.GET_CATEGORY_URL;
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
