package android.example.quantummind.presentation;

import android.content.Intent;
import android.example.quantummind.R;
import android.example.quantummind.domain.controllers.MainController;
import android.example.quantummind.domain.RankingAdapter;
import android.example.quantummind.domain.callbacks.RankingCallback;
import android.example.quantummind.domain.RankingItem;
import android.media.Image;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RankingActivity extends AppCompatActivity {
    private RecyclerView rankingRecyclerView;
    private RankingAdapter rankingAdapter;
    private List<RankingItem> rankingItems = new ArrayList<>();
    private FirebaseAuth mAuth;
    private MainController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        ImageView profileImageView = findViewById(R.id.userProfileImage);
        ImageView homeButton = findViewById(R.id.homeButton);
        ImageView achievementsButton = findViewById(R.id.achievementsButton);

        profileImageView.setOnClickListener(v -> startActivity(new Intent(RankingActivity.this, UserProfileActivity.class)));
        homeButton.setOnClickListener(v -> startActivity(new Intent(RankingActivity.this, MainActivity.class)));
        achievementsButton.setOnClickListener(v -> startActivity(new Intent(RankingActivity.this, AchievementsActivity.class)));

        controller = new MainController(this);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Uri photoUrl = user.getPhotoUrl();
            String name = user.getDisplayName();

            if (photoUrl != null) {
                Glide.with(this).load(photoUrl).into(profileImageView);
            }

            TextView profileNameTextView = findViewById(R.id.userProfileName);
            profileNameTextView.setText(name);
        }

        rankingRecyclerView = findViewById(R.id.rankingRecyclerView);
        rankingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rankingAdapter = new RankingAdapter(rankingItems);
        rankingRecyclerView.setAdapter(rankingAdapter);

        loadRankingData();
    }

    private void loadRankingData() {
        controller.getRanking(new RankingCallback() {
            @Override
            public void onSuccess(List<RankingItem> rankingItems) {
                RankingActivity.this.rankingItems.clear();
                RankingActivity.this.rankingItems.addAll(rankingItems);
                sortRankingItems();
                rankingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("RankingActivity", errorMessage);
            }
        });
    }

    private void sortRankingItems() {
        Collections.sort(rankingItems, new Comparator<RankingItem>() {
            @Override
            public int compare(RankingItem item1, RankingItem item2) {
                int comparePercentage = Integer.compare(item2.getPercentageCompleted(), item1.getPercentageCompleted());
                if (comparePercentage != 0) {
                    return comparePercentage;
                }
                return Double.compare(item2.getScore(), item1.getScore());
            }
        });

        for (int i = 0; i < rankingItems.size(); i++) {
            rankingItems.get(i).setPosition(i + 1);
        }

        rankingAdapter.notifyDataSetChanged();
    }
}

