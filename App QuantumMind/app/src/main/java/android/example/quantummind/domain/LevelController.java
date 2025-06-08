package android.example.quantummind.domain;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelController {

    protected final FirebaseFirestore db;
    protected final FirebaseAuth auth;

    public LevelController() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public interface SheetsCallback {
        void onSuccess(List<Sheet> sheets);
        void onError(String errorMessage);
    }

    public void fetchSheets(@NonNull String lessonId, @NonNull SheetsCallback callback) {
        db.collection("sheets")
                .whereEqualTo("lessonId", lessonId)
                .orderBy("id")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Sheet> result = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Sheet s = Sheet.fromDocument(doc);
                        if (s != null) result.add(s);
                    }
                    callback.onSuccess(result);
                })
                .addOnFailureListener(e -> {
                    Log.w("LevelController", "Error getting sheets for " + lessonId, e);
                    callback.onError("Error loading lesson material: " + e.getMessage());
                });
    }

    public interface QuestionsCallback {
        void onSuccess(Questionary questionary);
        void onError(String errorMessage);
    }

    public void fetchQuestions(@NonNull String lessonId, @NonNull QuestionsCallback callback) {
        db.collection("questions")
                .whereEqualTo("lessonId", lessonId)
                .orderBy("id")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Question> questions = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Question q = doc.toObject(Question.class);
                        if (q != null) questions.add(q);
                    }
                    Questionary questionary = new Questionary(lessonId, questions);
                    callback.onSuccess(questionary);
                })
                .addOnFailureListener(e -> {
                    Log.w("LevelController", "Error getting questions for " + lessonId, e);
                    callback.onError("Error loading questions: " + e.getMessage());
                });
    }

    public interface SaveAnswersCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public void saveUserAnswers(@NonNull List<Answer> answers, @NonNull SaveAnswersCallback callback) {
        WriteBatch batch = db.batch();
        CollectionReference answersRef = db.collection("userAnswers");
        for (Answer answer : answers) {
            DocumentReference newDoc = answersRef.document();
            batch.set(newDoc, answer.toMap());
        }
        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e("LevelController", "Error saving userAnswers", e);
                    callback.onError("Error saving answers: " + e.getMessage());
                });
    }

    public interface ProgressCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public void saveUserProgress(@NonNull String userId,
                                 @NonNull String lessonId,
                                 double newScore,
                                 @NonNull ProgressCallback callback) {
        DocumentReference userProgressRef = db.collection("userProgress").document(userId);

        String levelCompletedField;
        String levelScoreField;
        switch (lessonId) {
            case "lesson1":
                levelCompletedField = "level1_completed";
                levelScoreField     = "level1_score";
                break;
            case "lesson2":
                levelCompletedField = "level2_completed";
                levelScoreField     = "level2_score";
                break;
            case "lesson3":
                levelCompletedField = "level3_completed";
                levelScoreField     = "level3_score";
                break;
            case "lesson4":
                levelCompletedField = "level4_completed";
                levelScoreField     = "level4_score";
                break;
            case "lesson5":
                levelCompletedField = "level5_completed";
                levelScoreField     = "level5_score";
                break;
            case "lesson6":
                levelCompletedField = "level6_completed";
                levelScoreField     = "level6_score";
                break;
            default:
                callback.onError("Unknown lessonId: " + lessonId);
                return;
        }

        userProgressRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put(levelCompletedField, true);

                    if (documentSnapshot.exists()) {
                        Double oldScore = documentSnapshot.getDouble(levelScoreField);
                        if (oldScore == null || newScore > oldScore) {
                            if (oldScore != null && newScore > oldScore) {
                                unlockAchievement(userId,
                                        "always_improving",
                                        "Always Improving",
                                        null);
                            }
                            updateMap.put(levelScoreField, newScore);
                        } else {
                            updateMap.remove(levelScoreField);
                        }
                    } else {
                        updateMap.put(levelScoreField, newScore);
                    }

                    userProgressRef.set(updateMap, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> {
                                Log.e("LevelController", "Error updating userProgress", e);
                                callback.onError("Error saving progress: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("LevelController", "Error fetching userProgress", e);
                    callback.onError("Error reading previous progress: " + e.getMessage());
                });
    }

    public interface AchievementCallback {
        void onUnlocked(String achievementName);
        void onError(String errorMessage);
    }

    public void unlockAchievement(@NonNull String userId,
                                  @NonNull String achievementKey,
                                  @NonNull String achievementName,
                                  AchievementCallback callback) {
        DocumentReference achRef = db.collection("userAchievements").document(userId);
        achRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean already = false;
                    if (documentSnapshot.exists()) {
                        Boolean was = documentSnapshot.getBoolean(achievementKey);
                        if (was != null && was) {
                            already = true;
                        }
                    }
                    if (already) {
                        return;
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put(achievementKey, true);
                    achRef.set(data, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                if (callback != null) {
                                    callback.onUnlocked(achievementName);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("LevelController", "Error unlocking achievement " + achievementName, e);
                                if (callback != null) {
                                    callback.onError("Error unlocking " + achievementName + ": " + e.getMessage());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("LevelController", "Error checking achievement " + achievementName, e);
                    if (callback != null) {
                        callback.onError("Error checking achievement: " + e.getMessage());
                    }
                });
    }
}
