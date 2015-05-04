package cr4zyc4t.cafe24h;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cr4zyc4t.cafe24h.model.Category;
import cr4zyc4t.cafe24h.util.Configs;
import cr4zyc4t.cafe24h.util.Utils;


public class MainActivity extends AppCompatActivity {
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private class fetchCategories extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String url = Configs.GET_CATEGORY_URL + "?content_id=" + params[0];
            try {
                return Utils.StringRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
//            if (result != null) {
//                try {
//                    response = new JSONObject(result);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (response != null) {
//                try {
//                    body.loadData(response.getJSONObject("content_detail").getString("content"), "text/html; charset=utf-8", "UTF-8");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Toast.makeText(MainActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
//            }
        }
    }
}
