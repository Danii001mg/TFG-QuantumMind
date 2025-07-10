package android.example.quantummind.presentation;

import android.content.Intent;
import android.example.quantummind.R;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.example.quantummind.domain.controllers.MainController;
import android.example.quantummind.domain.entities.User;
import android.example.quantummind.domain.entities.UserProgress;
import android.example.quantummind.domain.controllers.MainController.UserCallback;
import android.example.quantummind.domain.controllers.MainController.ProgressCallback;

public class MainActivity extends AppCompatActivity {

    private ImageView level1, level2, level3, level4, level5, level6;
    private ImageView level1Points, level2Points, level3Points, level4Points, level5Points, level6Points;
    private ImageView profileImageView, achievementsButton, rankingButton;
    private boolean isLevel1Unlocked = false;
    private boolean isLevel2Unlocked = false;
    private boolean isLevel3Unlocked = false;
    private boolean isLevel4Unlocked = false;
    private boolean isLevel5Unlocked = false;
    private boolean isLevel6Unlocked = false;

    private MainController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controller = new MainController(this);

        profileImageView = findViewById(R.id.userProfileImage);
        TextView profileNameTextView = findViewById(R.id.userProfileName);
        achievementsButton = findViewById(R.id.achievementsButton);
        rankingButton = findViewById(R.id.rankingButton);

        level1 = findViewById(R.id.level1);
        level2 = findViewById(R.id.level2);
        level3 = findViewById(R.id.level3);
        level4 = findViewById(R.id.level4);
        level5 = findViewById(R.id.level5);
        level6 = findViewById(R.id.level6);

        level1Points = findViewById(R.id.level1_points);
        level2Points = findViewById(R.id.level2_points);
        level3Points = findViewById(R.id.level3_points);
        level4Points = findViewById(R.id.level4_points);
        level5Points = findViewById(R.id.level5_points);
        level6Points = findViewById(R.id.level6_points);

        profileImageView.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class))
        );

        achievementsButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AchievementsActivity.class))
        );

        rankingButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RankingActivity.class))
        );

        level1.setOnClickListener(v -> {
            if (isLevel1Unlocked) {
                openLevel("lesson1");
            } else {
                showLockedMessage();
            }
        });
        level2.setOnClickListener(v -> {
            if (isLevel2Unlocked) {
                openLevel("lesson2");
            } else {
                showLockedMessage();
            }
        });
        level3.setOnClickListener(v -> {
            if (isLevel3Unlocked) {
                openLevel("lesson3");
            } else {
                showLockedMessage();
            }
        });
        level4.setOnClickListener(v -> {
            if (isLevel4Unlocked) {
                openLevel("lesson4");
            } else {
                showLockedMessage();
            }
        });
        level5.setOnClickListener(v -> {
            if (isLevel5Unlocked) {
                openLevel("lesson5");
            } else {
                showLockedMessage();
            }
        });
        level6.setOnClickListener(v -> {
            if (isLevel6Unlocked) {
                openLevel("lesson6");
            } else {
                showLockedMessage();
            }
        });


        loadUserDataAndProgress(profileNameTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.reload()
                    .addOnSuccessListener(aVoid -> {
                        Uri newPhoto = firebaseUser.getPhotoUrl();
                        String newName = firebaseUser.getDisplayName();

                        if (newPhoto != null && !newPhoto.toString().isEmpty()) {
                            Glide.with(this)
                                    .load(newPhoto)
                                    .into(profileImageView);
                        }

                        if (newName != null) {
                            TextView profileNameTextView = findViewById(R.id.userProfileName);
                            profileNameTextView.setText(newName);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w("MainActivity", "Cant reload user information", e);
                    });

            controller.getUserProgress(firebaseUser.getUid(), new ProgressCallback() {
                @Override
                public void onSuccess(UserProgress progress) {
                    applyProgressToUI(progress);
                }
                @Override
                public void onError(String errorMessage) {
                    Log.w("MainActivity", "Error refreshing progress", new Throwable(errorMessage));
                }
            });
        }
    }


    private void openLevel(String levelId) {
        Intent intent = new Intent(MainActivity.this, LevelActivity.class);
        intent.putExtra("LESSON_ID", levelId);
        startActivity(intent);
    }

    private void loadUserDataAndProgress(TextView profileNameTextView) {
        controller.getCurrentUser(new UserCallback() {
            @Override
            public void onSuccess(User user) {
                String photoUrl = user.getPhotoUrl();
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(MainActivity.this)
                            .load(photoUrl)
                            .into(profileImageView);
                }
                profileNameTextView.setText(user.getDisplayName() != null ? user.getDisplayName() : "");

                String userId = user.getUid();
                controller.getUserProgress(userId, new ProgressCallback() {
                    @Override
                    public void onSuccess(UserProgress progress) {
                        applyProgressToUI(progress);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                redirectToLogin();
            }
        });
    }

    private void applyProgressToUI(UserProgress p) {
        ImageView[] icons  = {level1, level2, level3, level4, level5, level6};
        ImageView[] points = {level1Points, level2Points, level3Points, level4Points, level5Points, level6Points};
        for (int i = 0; i < icons.length; i++) {
            icons[i].clearColorFilter();
            points[i].setImageResource(0);
        }

        updateLevelIconAndColor(p.getLevel1Score(), level1, level1Points);
        isLevel1Unlocked = true;

        if (p.isLevel1Completed() && p.getLevel1Score() >= 30) {
            updateLevelIconAndColor(p.getLevel2Score(), level2, level2Points);
            isLevel2Unlocked = true;
        }

        if (p.isLevel2Completed() && p.getLevel2Score() >= 30) {
            updateLevelIconAndColor(p.getLevel3Score(), level3, level3Points);
            updateLevelIconAndColor(p.getLevel4Score(), level4, level4Points);
            isLevel3Unlocked = true;
            isLevel4Unlocked = true;
        }

        if (p.isLevel3Completed() && p.getLevel3Score() >= 30
                && p.isLevel4Completed() && p.getLevel4Score() >= 30) {
            updateLevelIconAndColor(p.getLevel5Score(), level5, level5Points);
            isLevel5Unlocked = true;
        }

        if (p.isLevel5Completed() && p.getLevel5Score() >= 30) {
            updateLevelIconAndColor(p.getLevel6Score(), level6, level6Points);
            isLevel6Unlocked = true;
        }
    }


    private void updateLevelIconAndColor(double score, ImageView levelImage, ImageView levelPoints) {
        if (score >= 100) {
            levelPoints.setImageResource(R.drawable.ic_stars);
            levelImage.setColorFilter(Color.GREEN);
        } else if (score >= 80) {
            levelPoints.setImageResource(R.drawable.ic_stars);
            levelImage.setColorFilter(Color.rgb(255, 128, 0)); // Orange
        } else if (score >= 60) {
            levelPoints.setImageResource(R.drawable.ic_two_stars);
            levelImage.setColorFilter(Color.rgb(255, 128, 0));
        } else if (score >= 30) {
            levelPoints.setImageResource(R.drawable.ic_one_star);
            levelImage.setColorFilter(Color.rgb(255, 128, 0));
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showLockedMessage() {
        Toast.makeText(MainActivity.this,
                "This level is locked. Please complete the previous levels first.",
                Toast.LENGTH_SHORT).show();
    }

}