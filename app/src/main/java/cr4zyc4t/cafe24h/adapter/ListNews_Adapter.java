package cr4zyc4t.cafe24h.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import cr4zyc4t.cafe24h.R;
import cr4zyc4t.cafe24h.model.News;
import cr4zyc4t.cafe24h.util.Utils;

/**
 * Created by Admin on 2015-04-24.
 */
public class ListNews_Adapter extends RecyclerView.Adapter<ListNews_Adapter.ItemViewHolder> {

    private List<News> listNews;
    private Context mContext;
    private NewsClickListener newsClickListener;
    private int current_column = 1;

    public class VIEW_TYPES {
        public static final int ITEM = 0;
        public static final int PROGRESS = 1;
    }

    public ListNews_Adapter(List<News> listNews, Context mContext) {
        this.listNews = listNews;
        this.mContext = mContext;
        setHasStableIds(true);
    }

    public void setCurrent_column(int current_column) {
        this.current_column = current_column;
    }

    public NewsClickListener getNewsClickListener() {
        return newsClickListener;
    }

    public void setNewsClickListener(NewsClickListener newsClickListener) {
        this.newsClickListener = newsClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return listNews.get(position) != null ? VIEW_TYPES.ITEM : VIEW_TYPES.PROGRESS;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == VIEW_TYPES.ITEM) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_item, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bottom_progressbar, viewGroup, false);
        }

        ItemViewHolder view_holder = new ItemViewHolder(view, viewType);
        return view_holder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder view_holder, int i) {
        News item = listNews.get(i);

        if (view_holder.getItemViewType() == VIEW_TYPES.ITEM) {

            view_holder.title.setText(item.getTitle());
            view_holder.description.setText(item.getDescription());
            view_holder.timestamp.setText(item.getTime());

            if (URLUtil.isValidUrl(item.getIcon())) {
//                int icon_width = Utils.getScreenWidth(mContext) - mContext.getResources().getDimensionPixelSize(R.dimen.card_horizontal_margin) - mContext.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
                int icon_width = Utils.getScreenWidth(mContext);
                icon_width = icon_width / current_column;
                Picasso.with(mContext).load(item.getIcon()).resize(icon_width, icon_width / 2).centerCrop().into(view_holder.icon);
            } else {
                view_holder.icon.setVisibility(View.GONE);
            }

            if (URLUtil.isValidUrl(item.getSource_icon())) {
                Picasso.with(mContext).load(item.getSource_icon()).resize(mContext.getResources().getDimensionPixelSize(R.dimen.source_icon_size), mContext.getResources().getDimensionPixelSize(R.dimen.source_icon_size)).centerCrop().error(R.drawable.cafe24h_icon).into(view_holder.source_icon);
            } else {
                Log.e("Picasso", item.getId() + ": " + item.getSource_icon());
                Picasso.with(mContext).load(R.drawable.cafe24h_icon).into(view_holder.source_icon);
            }

            view_holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (newsClickListener != null) {
                        newsClickListener.NewsClicked(view_holder.getAdapterPosition());
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return (listNews != null ? listNews.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return listNews.get(position) != null ? listNews.get(position).getId() : -1;
    }

    public interface NewsClickListener {
        void NewsClicked(int position);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected ImageView icon, source_icon;
        protected TextView title, description, timestamp;
        protected View parent;
        protected ProgressBar progressBar;

        public ItemViewHolder(View itemView, int viewType) {
            super(itemView);
            this.parent = itemView;
            if (viewType == VIEW_TYPES.ITEM) {
                this.icon = (ImageView) itemView.findViewById(R.id.icon);
                this.source_icon = (ImageView) itemView.findViewById(R.id.source_icon);
                this.title = (TextView) itemView.findViewById(R.id.title);
                this.description = (TextView) itemView.findViewById(R.id.description);
                this.timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            } else {
                this.progressBar = (ProgressBar) itemView.findViewById(R.id.bottom_progress_bar);
            }
        }

        public ProgressBar getProgressBar() {
            return progressBar;
        }

        public View getIcon() {
            return icon;
        }

        public View getSourceIcon() {
            return source_icon;
        }

        public View getTitle() {
            return title;
        }

        public View getTimestamp() {
            return timestamp;
        }

        public View getSource_icon() {
            return source_icon;
        }
    }
}
