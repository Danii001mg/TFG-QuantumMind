package android.example.quantummind.presentation;

import android.content.Intent;
import android.example.quantummind.domain.AchievementAdapter;
import android.example.quantummind.domain.AchievementItem;
import android.example.quantummind.domain.MainController;
import android.example.quantummind.domain.AchievementCallback;
import android.example.quantummind.R;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private RecyclerView achievementsRecyclerView;
    private AchievementAdapter achievementAdapter;
    private List<AchievementItem> achievementList = new ArrayList<>();
    private MainController mainController;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        mainController = new MainController();
        ImageView profileImage = findViewById(R.id.userProfileImage);
        ImageView homeButton = findViewById(R.id.homeButton);
        ImageView rankingButton = findViewById(R.id.rankingButton);

        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(AchievementsActivity.this, MainActivity.class));
        });

        rankingButton.setOnClickListener(v -> {
            startActivity(new Intent(AchievementsActivity.this, RankingActivity.class));
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Uri photoUrl = user.getPhotoUrl();
            String name = user.getDisplayName();

            if (photoUrl != null) {
                Glide.with(this).load(photoUrl).into(profileImage);
            }

            TextView profileNameTextView = findViewById(R.id.userProfileName);
            profileNameTextView.setText(name);

            userId = user.getUid();
            loadAchievements(userId);
        }

        achievementsRecyclerView = findViewById(R.id.achievementsRecyclerView);
        achievementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        achievementAdapter = new AchievementAdapter(achievementList);
        achievementsRecyclerView.setAdapter(achievementAdapter);
    }

    private void loadAchievements(String userId) {
        mainController.getUserAchievements(userId, new AchievementCallback() {
            @Override
            public void onSuccess(AchievementItem[] achievements) {
                achievementList.clear();
                for (AchievementItem achievement : achievements) {
                    achievementList.add(achievement);
                }
                achievementAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("AchievementsActivity", errorMessage);
            }
        });
    }
}
