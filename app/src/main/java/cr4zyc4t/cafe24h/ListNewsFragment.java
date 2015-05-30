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
    private static final String TYPE = "type_request";
    private static final String TARGET_ID = "target_id";
    private static final String COLOR = "color";
    private static final String INITIAL_POS = "initial_position";

    private int type_request;
    private int target_id;
    private int own_color;
    private int initial_position;

    private final int GRID_COLUMN = 2;
    private List<News> listNews = new ArrayList<>();
    private ListNews_Adapter adapter;
    private RecyclerView newsContainer;
    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean isLoaded = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;

    private int current_offset = 0;
    private int current_column = 1;

    private ContentScrollListenter contentScrollListenter;
    private LinearLayoutManager layoutManager;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ListNewsFragment.
     */
    public static ListNewsFragment newInstance(int type_request, int target_id, int own_color) {
        ListNewsFragment fragment = new ListNewsFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type_request);
        args.putInt(TARGET_ID, target_id);
        args.putInt(COLOR, own_color);
        fragment.setArguments(args);
        return fragment;
    }

    public static ListNewsFragment newInstance(int type_request, int target_id, int own_color, int initial_position) {
        ListNewsFragment fragment = new ListNewsFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type_request);
        args.putInt(TARGET_ID, target_id);
        args.putInt(COLOR, own_color);
        args.putInt(INITIAL_POS, initial_position);
        fragment.setArguments(args);
        return fragment;
    }

    public ListNewsFragment() {
        // Required empty public constructor
    }

    public void setContentScrollListenter(ContentScrollListenter contentScrollListenter) {
        this.contentScrollListenter = contentScrollListenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type_request = getArguments().getInt(TYPE);
            target_id = getArguments().getInt(TARGET_ID);
            own_color = getArguments().getInt(COLOR);
            initial_position = getArguments().getInt(INITIAL_POS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_news, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newsContainer = (RecyclerView) view.findViewById(R.id.list_news_container);

        if (Utils.isLandscape(getActivity()) && (Utils.getScreenWidthInDp(view.getContext()) > Configs.LARGE_SCREEN_DP)) {
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
            current_column = GRID_COLUMN;
            layoutManager = (LinearLayoutManager) gridLayoutManager;
        } else {
            layoutManager = new LinearLayoutManager(view.getContext());
            newsContainer.setLayoutManager(layoutManager);
        }

        newsContainer.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (contentScrollListenter != null) contentScrollListenter.onContentScroll(dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                if (isLoaded) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        isLoaded = false;
                        getNews();
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (contentScrollListenter != null)
                    contentScrollListenter.onContentScrollStateChange(newState);
            }
        });

        adapter = new ListNews_Adapter(listNews, getActivity());
        adapter.setNewsClickListener(this);
        adapter.setCurrent_column(current_column);
        newsContainer.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(own_color);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNews();
            }
        });

        if (type_request == Configs.CATEGORY_TYPE) {
            int headerHeight = Utils.getToolbarHeight(view.getContext()) + view.getContext().getResources().getDimensionPixelSize(R.dimen.card_vertical_margin);
            newsContainer.setPadding(newsContainer.getPaddingLeft(), headerHeight, newsContainer.getPaddingRight(), newsContainer.getPaddingBottom());

            swipeRefreshLayout.setProgressViewOffset(false, Utils.getToolbarHeight(view.getContext()), 2 * Utils.getToolbarHeight(view.getContext()));
        }

        getNews();
    }

    @Override
    public void NewsClicked(int position) {
        News clicked_item = listNews.get(position);

        Intent readnews = new Intent(getActivity(), ReadNewsActivity.class);
        readnews.putExtra("news", clicked_item);
        readnews.putExtra("color", own_color);

        ListNews_Adapter.ItemViewHolder viewHolder = (ListNews_Adapter.ItemViewHolder) newsContainer.findViewHolderForItemId(clicked_item.getId());
        if ((viewHolder != null) && (!Utils.isLandscape(getActivity()))) {
            Pair<View, String> titlePair = Pair.create(viewHolder.getTitle(), "title");
            Pair<View, String> timePair = Pair.create(viewHolder.getTimestamp(), "time");
            Pair<View, String> iconPair = Pair.create(viewHolder.getIcon(), "icon");
            Pair<View, String> sourcePair = Pair.create(viewHolder.getSourceIcon(), "source");

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), titlePair, timePair, iconPair, sourcePair);
            ActivityCompat.startActivity(getActivity(), readnews, options.toBundle());
        } else {
            startActivity(readnews);
        }
    }

    @Override
    public void SubcategoryClicked(int position) {
        News news = listNews.get(position);

        Intent filter = new Intent(getActivity(), SubcategoryFilterActivity.class);
        filter.putExtra("color", own_color);
        filter.putExtra("subcategory", news.getSubcategory());
        filter.putExtra("subcategory_id", news.getSubcategory_id());
        startActivity(filter);
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

                swipeRefreshLayout.setEnabled(false);
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = Configs.getContentURL(type_request, target_id, Configs.NEWS_PER_LOAD, current_offset);
            Log.i("Resquest", "URL " + url);
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

            if (!swipeRefreshLayout.isEnabled()) {
                swipeRefreshLayout.setEnabled(true);
            }

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
                        News news = new News(feed.optString("title"),
                                feed.optString("icon"),
                                feed.optString("source_icon"),
                                feed.optString("time"),
                                feed.optString("description"),
                                feed.optInt("id"),
                                feed.optInt("source_id"),
                                feed.optString("source"),
                                feed.optString("subcategory"),
                                feed.optInt("subcategory_id")
                        );
                        listNews.add(news);
                        current_offset++;
                        isLoaded = true;
                        adapter.notifyItemInserted(listNews.size() - 1);
                    }
                }
                if (listNews.size() <= Configs.NEWS_PER_LOAD) {
                    if (initial_position > 0) {
                        layoutManager.scrollToPositionWithOffset(0, -initial_position);
                    }
                }
            }
        }
    }

    public interface ContentScrollListenter {
        void onContentScroll(int dy);

        void onContentScrollStateChange(int state);
    }
}
