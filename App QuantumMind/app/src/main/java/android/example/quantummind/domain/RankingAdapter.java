package android.example.quantummind.domain;

import android.example.quantummind.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<RankingItem> rankingItems;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public RankingAdapter(List<RankingItem> rankingItems) {
        this.rankingItems = rankingItems;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ranking_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ranking_item, parent, false);
            return new RankingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RankingViewHolder) {
            RankingItem item = rankingItems.get(position - 1);
            ((RankingViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return rankingItems.size() + 1;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView positionTextView, usernameTextView, percentageTextView, scoreTextView;

        public RankingViewHolder(View itemView) {
            super(itemView);
            positionTextView = itemView.findViewById(R.id.positionTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            percentageTextView = itemView.findViewById(R.id.percentageTextView);
            scoreTextView = itemView.findViewById(R.id.scoreTextView);
        }

        void bind(RankingItem item) {
            positionTextView.setText(String.valueOf(item.getPosition()));
            usernameTextView.setText(item.getUsername());
            percentageTextView.setText(String.format("%d%%", item.getPercentageCompleted()));
            scoreTextView.setText(String.format("%.2f", item.getScore()));
        }
    }
}
