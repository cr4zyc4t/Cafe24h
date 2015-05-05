package cr4zyc4t.cafe24h;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cr4zyc4t.cafe24h.adapter.ListNews_Adapter;
import cr4zyc4t.cafe24h.model.News;
import cr4zyc4t.cafe24h.util.Configs;
import cr4zyc4t.cafe24h.util.Utils;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListNewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListNewsFragment extends Fragment implements ListNews_Adapter.NewsClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String CATEGORY_ID = "category_id";
    private static final String COLOR = "color";

    private int category_id;
    private int own_color;

    private final int GRID_COLUMN = 2;
    private List<News> listNews = new ArrayList<News>();
    private ListNews_Adapter adapter;
    private RecyclerView newsContainer;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int current_offset = 0;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Category_id.
     * @return A new instance of fragment ListNewsFragment.
     */
    public static ListNewsFragment newInstance(int param1, int param2) {
        ListNewsFragment fragment = new ListNewsFragment();
        Bundle args = new Bundle();
        args.putInt(CATEGORY_ID, param1);
        args.putInt(COLOR, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ListNewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category_id = getArguments().getInt(CATEGORY_ID);
            own_color = getArguments().getInt(COLOR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_news, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newsContainer = (RecyclerView) view.findViewById(R.id.list_news_container);

        if (Utils.isTablet(getResources())) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), GRID_COLUMN);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (adapter.getItemViewType(position) == ListNews_Adapter.VIEW_TYPES.PROGRESS) {
                        return GRID_COLUMN;
                    }
                    return 1;
                }
            });
            newsContainer.setLayoutManager(gridLayoutManager);
        } else {
            newsContainer.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        adapter = new ListNews_Adapter(listNews, getActivity());
        adapter.setNewsClickListener(this);
        newsContainer.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(own_color);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshNews();
            }
        });

        getNews();
    }

    @Override
    public void NewsClicked(int position) {
        News clicked_item = listNews.get(position);
        Log.i("Action", " Click " + clicked_item.getTitle());

        Intent readnews = new Intent(getActivity(), ReadActivity.class);
        readnews.putExtra("news", clicked_item);
        readnews.putExtra("color", own_color);

        ListNews_Adapter.ItemViewHolder viewHolder = (ListNews_Adapter.ItemViewHolder) newsContainer.findViewHolderForItemId(clicked_item.getId());
        if ((viewHolder != null) && (!Utils.isTablet(this.getResources()))) {
            Pair<View, String> titlePair = Pair.create(viewHolder.getTitle(), "title");
            Pair<View, String> timePair = Pair.create(viewHolder.getTimestamp(), "time");
            Pair<View, String> iconPair = Pair.create(viewHolder.getIcon(), "icon");
            Pair<View, String> sourcePair = Pair.create(viewHolder.getSource_icon(), "source");

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), titlePair, timePair, iconPair, sourcePair);
            ActivityCompat.startActivity(getActivity(), readnews, options.toBundle());
        } else {
            startActivity(readnews);
        }
    }

    public void getNews() {
        new getNewsList().execute();
    }

    public void refreshNews() {
        current_offset = 0;
        listNews.removeAll(listNews);
        adapter.notifyDataSetChanged();
        getNews();
    }

    private class getNewsList extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!swipeRefreshLayout.isRefreshing()) {
                listNews.add(null);
                adapter.notifyItemInserted(listNews.size() - 1);
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = Configs.GET_CONTENT_BY_CATEGORY_URL + "?category_id=" + category_id + "&limit=" + Configs.NEWS_PER_LOAD + "&offset=" + current_offset;
            Log.i("Request", url);
            try {
                return Utils.StringRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            JSONArray feeds = null;
            if (s != null) {
                try {
                    JSONObject responseObj = new JSONObject(s);
                    feeds = responseObj.optJSONArray("list_content");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if ((feeds != null) && (feeds.length() > 0)) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    listNews.remove(listNews.size() - 1);
                    adapter.notifyItemRemoved(listNews.size());
                }

                for (int i = 0; i < feeds.length(); i++) {
                    JSONObject feed = feeds.optJSONObject(i);
                    if (feed != null) {
                        News news = new News(feed.optString("title"), feed.optString("icon"), feed.optString("source_icon"), feed.optString("time"), feed.optString("description"), feed.optInt("id"));
                        listNews.add(news);
                        current_offset++;
                        adapter.notifyItemInserted(listNews.size() - 1);
                    }
                }
            }

        }
    }
}
