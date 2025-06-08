package android.example.quantummind.domain;

import android.example.quantummind.R;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class MainController {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    public interface ProgressCallback {
        void onSuccess(UserProgress progress);
        void onError(String errorMessage);
    }

    public MainController() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void getCurrentUser(@NonNull UserCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            User user = User.fromFirebaseUser(firebaseUser);
            callback.onSuccess(user);
        } else {
            callback.onError("User not authenticated.");
        }
    }

    public void getUserAchievements(@NonNull String userId, @NonNull AchievementCallback callback) {
        firestore.collection("userAchievements")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc.exists()) {
                            AchievementItem[] achievements = new AchievementItem[7];
                            achievements[0] = new AchievementItem(R.drawable.ic_first_badge, "First step", "Complete your first question", doc.getBoolean("first_step") != null && doc.getBoolean("first_step"));
                            achievements[1] = new AchievementItem(R.drawable.ic_10_badge, "Question hunter", "Answer correctly all the questions from a level", doc.getBoolean("question_hunter") != null && doc.getBoolean("question_hunter"));
                            achievements[2] = new AchievementItem(R.drawable.ic_6levels_badge, "Knowledge master", "Complete all the levels", doc.getBoolean("knowledge_master") != null && doc.getBoolean("knowledge_master"));
                            achievements[3] = new AchievementItem(R.drawable.ic_complete_levels_badge, "Genius", "Get a score of 8/10 or more in every level", doc.getBoolean("genius") != null && doc.getBoolean("genius"));
                            achievements[4] = new AchievementItem(R.drawable.ic_1min_badge, "Fast and furious", "Complete a questionnaire in less than 1 minute", doc.getBoolean("fast_and_furious") != null && doc.getBoolean("fast_and_furious"));
                            achievements[5] = new AchievementItem(R.drawable.ic_marathon_badge, "Mental marathon", "Complete all the levels in one session", doc.getBoolean("mental_marathon") != null && doc.getBoolean("mental_marathon"));
                            achievements[6] = new AchievementItem(R.drawable.ic_improve_badge, "Always improving", "Improve your score in a level", doc.getBoolean("always_improving") != null && doc.getBoolean("always_improving"));
                            callback.onSuccess(achievements);
                        } else {
                            callback.onError("No achievements found for user");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError("Error fetching achievements: " + e.getMessage());
                    }
                });
    }


    public void getUserProgress(@NonNull String userId, @NonNull ProgressCallback callback) {
        firestore.collection("userProgress")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc.exists()) {
                            Boolean l1c = doc.getBoolean("level1_completed");
                            Double l1s = doc.getDouble("level1_score");
                            Boolean l2c = doc.getBoolean("level2_completed");
                            Double l2s = doc.getDouble("level2_score");
                            Boolean l3c = doc.getBoolean("level3_completed");
                            Double l3s = doc.getDouble("level3_score");
                            Boolean l4c = doc.getBoolean("level4_completed");
                            Double l4s = doc.getDouble("level4_score");
                            Boolean l5c = doc.getBoolean("level5_completed");
                            Double l5s = doc.getDouble("level5_score");
                            Boolean l6c = doc.getBoolean("level6_completed");
                            Double l6s = doc.getDouble("level6_score");

                            UserProgress progress = new UserProgress(
                                    (l1c != null && l1c), (l1s != null ? l1s : 0.0),
                                    (l2c != null && l2c), (l2s != null ? l2s : 0.0),
                                    (l3c != null && l3c), (l3s != null ? l3s : 0.0),
                                    (l4c != null && l4c), (l4s != null ? l4s : 0.0),
                                    (l5c != null && l5c), (l5s != null ? l5s : 0.0),
                                    (l6c != null && l6c), (l6s != null ? l6s : 0.0)
                            );
                            callback.onSuccess(progress);
                        } else {
                            UserProgress defaultProgress = new UserProgress(
                                    false, 0.0,
                                    false, 0.0,
                                    false, 0.0,
                                    false, 0.0,
                                    false, 0.0,
                                    false, 0.0
                            );
                            callback.onSuccess(defaultProgress);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError("Error fetching user progress: " + e.getMessage());
                    }
                });
    }

    public void getRanking(@NonNull RankingCallback callback) {
        firestore.collection("userProgress")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<RankingItem> rankingItems = new ArrayList<>();
                    final int[] position = {1};

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        if (position[0] > 100) break;

                        String userId = document.getId();
                        double score = calculateScore(document);
                        int levelsCompleted = getCompletedLevels(document);
                        int totalLevels = 6;

                        double percentageCompleted = (levelsCompleted / (double) totalLevels) * 100;

                        firestore.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String email = userDoc.getString("displayName");
                                    if (email == null || email.isEmpty()) {
                                        email = userId;
                                    }

                                    rankingItems.add(new RankingItem(0, email, (int) Math.round(percentageCompleted), score));

                                    if (rankingItems.size() == queryDocumentSnapshots.size()) {
                                        callback.onSuccess(rankingItems);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError("Error fetching ranking data: " + e.getMessage());
                });
    }

    private double calculateScore(DocumentSnapshot document) {
        int totalQuizzes = 6;
        double totalScore = 0.0;
        int attempts = 0;

        for (int i = 1; i <= totalQuizzes; i++) {
            Long levelScore = document.getLong("level" + i + "_score");
            Boolean levelCompleted = document.getBoolean("level" + i + "_completed");

            if (levelScore != null && levelCompleted != null && levelCompleted) {
                totalScore += levelScore;
            }
        }

        Long attemptCount = document.getLong("attempts");
        if (attemptCount != null) {
            attempts = attemptCount.intValue();
        }

        double averageScore = totalQuizzes > 0 ? totalScore / totalQuizzes : 0;

        double completionPercentage = (getCompletedLevels(document) / (double) totalQuizzes) * 100;

        double score = ((averageScore * totalQuizzes + completionPercentage) / (totalQuizzes + 1)) - (attempts * 0.1);

        return Math.round(score * 100.0) / 100.0;
    }

    private int getCompletedLevels(DocumentSnapshot document) {
        int completed = 0;
        for (int i = 1; i <= 6; i++) {
            Boolean levelCompleted = document.getBoolean("level" + i + "_completed");
            if (levelCompleted != null && levelCompleted) {
                completed++;
            }
        }
        return completed;
    }

}
