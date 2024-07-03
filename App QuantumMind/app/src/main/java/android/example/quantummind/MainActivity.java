package android.example.quantummind;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ImageView level1;
    private ImageView level2;
    private ImageView level1Points;
    private ImageView level2Points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView profileImage = findViewById(R.id.userProfileImage);
        ImageView settingsButton = findViewById(R.id.settingsButton);
        ImageView achievementsButton = findViewById(R.id.achievementsButton);
        level1 = findViewById(R.id.level1);
        level2 = findViewById(R.id.level2);
        level1Points = findViewById(R.id.level1_points);
        level2Points = findViewById(R.id.level2_points);

        profileImage.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
        });

        settingsButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        achievementsButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AchievementsActivity.class));
        });

        level1.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LevelActivity.class);
            intent.putExtra("LESSON_ID", "lesson1");
            startActivity(intent);
        });

        level2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LevelActivity.class);
            intent.putExtra("LESSON_ID", "lesson2");
            startActivity(intent);
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Uri photoUrl = user.getPhotoUrl();
            String name = user.getDisplayName();

            ImageView profileImageView = findViewById(R.id.userProfileImage);
            if (photoUrl != null) {
                Glide.with(this).load(photoUrl).into(profileImageView);
            }

            TextView profileNameTextView = findViewById(R.id.userProfileName);
            profileNameTextView.setText(name);

            checkUserProgress(user.getUid());
        }

        if (getIntent().hasExtra("LEVEL1_SCORE")) {
            double score = getIntent().getDoubleExtra("LEVEL1_SCORE", 0.0);
            updateLevelIconAndColor(score, "level1");
        }

        if (getIntent().hasExtra("LEVEL2_SCORE")) {
            double score = getIntent().getDoubleExtra("LEVEL2_SCORE", 0.0);
            updateLevelIconAndColor(score, "level2");
        }
    }

    private void updateLevelIconAndColor(double score, String level) {
        ImageView levelPoints;
        ImageView levelImage;

        if ("level1".equals(level)) {
            levelPoints = level1Points;
            levelImage = level1;
        } else if ("level2".equals(level)) {
            levelPoints = level2Points;
            levelImage = level2;
        } else {
            return;
        }

        if (score >= 80) {
            // 3 estrellas
            levelPoints.setImageResource(R.drawable.ic_stars);
            levelImage.setColorFilter(Color.GREEN);
        } else if (score >= 60) {
            // 2 estrellas
            levelPoints.setImageResource(R.drawable.ic_two_stars);
            levelImage.setColorFilter(Color.GREEN);
        } else if (score >= 30) {
            // 1 estrella
            levelPoints.setImageResource(R.drawable.ic_one_star);
            levelImage.setColorFilter(Color.GREEN);
        } else {
            // No hay puntos mínimos
            levelPoints.setImageResource(0);
            levelImage.clearColorFilter();
        }
    }

    private void checkUserProgress(String userId) {
        DocumentReference userProgressRef = db.collection("userProgress").document(userId);

        userProgressRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Información del nivel 1
                Boolean level1Completed = documentSnapshot.getBoolean("level1_completed");
                Double level1Score = documentSnapshot.getDouble("level1_score");

                if (level1Completed != null && level1Completed) {
                    updateLevelIconAndColor(level1Score != null ? level1Score : 0.0, "level1");
                    level2.setEnabled(true);
                    level2.clearColorFilter();
                } else {
                    level2.setEnabled(false);
                    level2.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                }

                // Información del nivel 2
                Boolean level2Completed = documentSnapshot.getBoolean("level2_completed");
                Double level2Score = documentSnapshot.getDouble("level2_score");

                if (level2Completed != null && level2Completed) {
                    updateLevelIconAndColor(level2Score != null ? level2Score : 0.0, "level2");
                }
            } else {
                Log.d("Progress", "No progress document found for user.");
            }
        }).addOnFailureListener(e -> Log.w("Progress", "Error getting user progress.", e));
    }
}
