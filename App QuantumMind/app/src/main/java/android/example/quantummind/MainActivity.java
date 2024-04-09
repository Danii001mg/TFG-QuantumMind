package android.example.quantummind;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ImageView level1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView profileImage = findViewById(R.id.userProfileImage);
        ImageView settingsButton = findViewById(R.id.settingsButton);
        ImageView achievementsButton = findViewById(R.id.achievementsButton);
        level1 = findViewById(R.id.level1);

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
            Intent intent = new Intent (MainActivity.this, Level1Activity.class);
            intent.putExtra("LESSON_ID", "lesson0");
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
            updateLevelIconAndColor(score);
        }


    }
    private void updateLevelIconAndColor(double score) {
        ImageView level1Points = findViewById(R.id.level1_points);

        if (score >= 80) {
            // Usuario obtuvo 3 estrellas
            level1Points.setImageResource(R.drawable.ic_stars);
            level1.setColorFilter(Color.GREEN);
        } else if (score >= 60) {
            // Usuario obtuvo 2 estrellas
            level1Points.setImageResource(R.drawable.ic_two_stars);
            level1.setColorFilter(Color.GREEN);
        } else if (score >= 30) {
            // Usuario obtuvo 1 estrella
            level1Points.setImageResource(R.drawable.ic_one_star);
            level1.setColorFilter(Color.GREEN);
        } else {
            // Usuario no alcanzó la puntuación mínima
            level1Points.setImageResource(0);
        }
    }

    private void checkUserProgress(String userId) {
        DocumentReference userProgressRef = db.collection("userProgress").document(userId);

        userProgressRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean levelCompleted = documentSnapshot.getBoolean("level1_completed");
                Double levelScore = documentSnapshot.getDouble("level1_score");

                if (levelCompleted != null && levelCompleted) {
                    updateLevelIconAndColor(levelScore != null ? levelScore : 0.0);
                }
            } else {
                Log.d("Progress", "No progress document found for user.");
            }
        }).addOnFailureListener(e -> Log.w("Progress", "Error getting user progress.", e));
    }

}
