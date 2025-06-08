package android.example.quantummind.domain;

import android.example.quantummind.R;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private List<AchievementItem> achievementList;

    public AchievementAdapter(List<AchievementItem> achievementList) {
        this.achievementList = achievementList;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.achievement_item, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        AchievementItem achievement = achievementList.get(position);

        holder.achievementIcon.setImageResource(achievement.getIconId());

        if (!achievement.isUnlocked()) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.achievementIcon.setColorFilter(filter);
        } else {
            holder.achievementIcon.clearColorFilter();
        }

        holder.achievementTitle.setText(achievement.getTitle());
        holder.achievementDescription.setText(achievement.getDescription());
    }

    @Override
    public int getItemCount() {
        return achievementList.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {

        ImageView achievementIcon;
        TextView achievementTitle;
        TextView achievementDescription;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            achievementIcon = itemView.findViewById(R.id.achievementIcon);
            achievementTitle = itemView.findViewById(R.id.achievementTitle);
            achievementDescription = itemView.findViewById(R.id.achievementDescription);
        }
    }
}
