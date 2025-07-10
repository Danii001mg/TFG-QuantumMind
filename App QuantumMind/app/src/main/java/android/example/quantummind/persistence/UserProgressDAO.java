    package android.example.quantummind.persistence;

    import androidx.annotation.NonNull;

    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.SetOptions;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    import android.content.Context;
    import android.example.quantummind.domain.AchievementItem;
    import android.example.quantummind.domain.controllers.LessonController;
    import android.example.quantummind.domain.callbacks.AchievementCallback;
    import android.example.quantummind.domain.callbacks.RankingCallback;
    import android.example.quantummind.domain.RankingItem;
    import android.example.quantummind.domain.callbacks.UserProgressCallback;
    import android.example.quantummind.domain.entities.UserProgress;
    import android.util.Log;
    import android.widget.Toast;

    public class UserProgressDAO {
        private final FirebaseFirestore firestore;
        private final Context ctx;


        public UserProgressDAO(Context ctx) {
            firestore = DatabaseManager.getInstance(ctx).getFirestore();
            this.ctx = ctx.getApplicationContext();
        }

        public void getUserProgress(@NonNull String userId, @NonNull UserProgressCallback callback) {
            firestore.collection("userProgress")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            boolean l1c = doc.getBoolean("level1_completed") != null && doc.getBoolean("level1_completed");
                            double l1s = doc.getDouble("level1_score") != null ? doc.getDouble("level1_score") : 0.0;
                            boolean l2c = doc.getBoolean("level2_completed") != null && doc.getBoolean("level2_completed");
                            double l2s = doc.getDouble("level2_score") != null ? doc.getDouble("level2_score") : 0.0;
                            boolean l3c = doc.getBoolean("level3_completed") != null && doc.getBoolean("level3_completed");
                            double l3s = doc.getDouble("level3_score") != null ? doc.getDouble("level3_score") : 0.0;
                            boolean l4c = doc.getBoolean("level4_completed") != null && doc.getBoolean("level4_completed");
                            double l4s = doc.getDouble("level4_score") != null ? doc.getDouble("level4_score") : 0.0;
                            boolean l5c = doc.getBoolean("level5_completed") != null && doc.getBoolean("level5_completed");
                            double l5s = doc.getDouble("level5_score") != null ? doc.getDouble("level5_score") : 0.0;
                            boolean l6c = doc.getBoolean("level6_completed") != null && doc.getBoolean("level6_completed");
                            double l6s = doc.getDouble("level6_score") != null ? doc.getDouble("level6_score") : 0.0;

                            UserProgress up = new UserProgress(
                                    l1c, l1s,
                                    l2c, l2s,
                                    l3c, l3s,
                                    l4c, l4s,
                                    l5c, l5s,
                                    l6c, l6s
                            );
                            callback.onSuccess(up);
                        } else {
                            callback.onSuccess(new UserProgress());
                        }
                    })
                    .addOnFailureListener(e -> callback.onError("Error fetching user progress: " + e.getMessage()));
        }

        public void getRanking(@NonNull RankingCallback callback) {
            firestore.collection("userProgress")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<RankingItem> rankingItems = new ArrayList<>();
                        for (DocumentSnapshot document : querySnapshot) {
                            String uid = document.getId();
                            double score = calculateScore(document);
                            int completed = getCompletedLevels(document);
                            double pct = (completed / 6.0) * 100;

                            firestore.collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        String name = userDoc.getString("displayName");
                                        if (name == null || name.isEmpty()) {
                                            name = uid;
                                        }
                                        rankingItems.add(new RankingItem(
                                                0,
                                                name,
                                                (int)Math.round(pct),
                                                score
                                        ));
                                        if (rankingItems.size() == querySnapshot.size()) {
                                            callback.onSuccess(rankingItems);
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onError("Error fetching ranking data: " + e.getMessage());
                    });
        }

        private double calculateScore(DocumentSnapshot doc) {
            int total = 6;
            double sum = 0;
            for (int i = 1; i <= total; i++) {
                Boolean c = doc.getBoolean("level" + i + "_completed");
                Double s = doc.getDouble("level" + i + "_score");
                if (c != null && c && s != null) sum += s;
            }
            Long att = doc.getLong("attempts");
            double avg = total > 0 ? sum / total : 0;
            double pct = (getCompletedLevels(doc) / (double) total) * 100;
            double sc = ((avg * total + pct) / (total + 1)) - ((att != null ? att : 0) * 0.1);
            return Math.round(sc * 100) / 100.0;
        }

        private int getCompletedLevels(DocumentSnapshot doc) {
            int c = 0;
            for (int i = 1; i <= 6; i++) {
                Boolean v = doc.getBoolean("level" + i + "_completed");
                if (v != null && v) c++;
            }
            return c;
        }

        public void saveUserProgress(String userId,
                                     String lessonId,
                                     double newScore,
                                     LessonController.ProgressCallback cb) {

            String scoreField = lessonId.replace("lesson","level") + "_score";
            String compField  = lessonId.replace("lesson","level") + "_completed";

            firestore.collection("userProgress")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(ds -> {
                        Double oldScore = ds.getDouble(scoreField);
                        boolean improved  = oldScore != null && newScore > oldScore;
                        boolean completed = newScore >= 30.0;  // ≥30% = completado

                        Map<String,Object> upd = new HashMap<>();
                        if (completed) upd.put(compField, true);
                        if (oldScore == null || newScore > oldScore) {
                            upd.put(scoreField, newScore);
                        }

                        ds.getReference()
                                .set(upd, SetOptions.merge())
                                .addOnSuccessListener(v -> {
                                    if (improved) {
                                        unlockAchievement(userId,
                                                "always_improving",
                                                "Always Improving",
                                                new AchievementCallback()   {
                                                    @Override
                                                    public void onUnlocked(String achievementName) {
                                                        Toast.makeText(ctx,
                                                                "¡Achievement unlocked: " + achievementName + "!",
                                                                Toast.LENGTH_SHORT).show();
                                                    }

                                                    @Override
                                                    public void onSuccess(AchievementItem[] achievements) {
                                                        for (AchievementItem achievement : achievements) {
                                                            Log.d("Achievement", "Achievement unlocked: " + achievement.getTitle());
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(String error) {
                                                        Log.e("LevelDAO", "Error unlocking always_improving", new Throwable(error));
                                                    }
                                                });
                                    }

                                    firestore.collection("userProgress")
                                            .document(userId)
                                            .get()
                                            .addOnSuccessListener(all -> {
                                                boolean allDone = true;
                                                boolean allHigh = true;
                                                for (int i = 1; i <= 6; i++) {
                                                    Boolean done = all.getBoolean("level"+i+"_completed");
                                                    Double sc   = all.getDouble("level"+i+"_score");
                                                    if (done==null || !done) { allDone = false; }
                                                    if (sc==null   || sc < 80.0) { allHigh = false; }
                                                }

                                                if (allDone) {
                                                    unlockAchievement(userId,
                                                            "knowledge_master",
                                                            "Knowledge Master",
                                                            new AchievementCallback() {
                                                                @Override
                                                                public void onUnlocked(String name) {
                                                                    Toast.makeText(ctx,
                                                                            "¡Achievement unlocked: " + name + "!",
                                                                            Toast.LENGTH_SHORT).show();
                                                                }

                                                                @Override
                                                                public void onSuccess(AchievementItem[] achievements) {
                                                                    for (AchievementItem achievement : achievements) {
                                                                        Log.d("Achievement", "Achievement unlocked: " + achievement.getTitle());
                                                                    }
                                                                }

                                                                @Override
                                                                public void onError(String err) {
                                                                    Log.e("LevelDAO","Error unlocking knowledge_master", new Throwable(err));
                                                                }
                                                            });
                                                }
                                                if (allHigh) {
                                                    unlockAchievement(userId,
                                                            "genius",
                                                            "Genius",
                                                            new AchievementCallback() {
                                                                @Override
                                                                public void onUnlocked(String name) {
                                                                    Toast.makeText(ctx,
                                                                            "¡Achievement unlocked: " + name + "!",
                                                                            Toast.LENGTH_SHORT).show();
                                                                }

                                                                @Override
                                                                public void onSuccess(AchievementItem[] achievements) {
                                                                    for (AchievementItem achievement : achievements) {
                                                                        Log.d("Achievement", "Achievement unlocked: " + achievement.getTitle());
                                                                    }
                                                                }

                                                                @Override
                                                                public void onError(String err) {
                                                                    Log.e("LevelDAO","Error unlocking genius", new Throwable(err));
                                                                }
                                                            });
                                                }

                                            });

                                    cb.onSuccess();
                                })
                                .addOnFailureListener(e -> cb.onError(e.getMessage()));
                    })
                    .addOnFailureListener(e -> cb.onError(e.getMessage()));
        }

        public void unlockAchievement(String userId,
                                      String key,
                                      String name,
                                      AchievementCallback cb) {
            DocumentReference ar = firestore.collection("userAchievements").document(userId);
            ar.get()
                    .addOnSuccessListener(ds -> {
                        Boolean already = ds.getBoolean(key);
                        if (Boolean.TRUE.equals(already)) return;
                        Map<String,Object> m = new HashMap<>();
                        m.put(key, true);
                        ar.set(m, SetOptions.merge())
                                .addOnSuccessListener(v -> {
                                    if (cb != null) cb.onUnlocked(name);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("LevelDAO", "unlock failed", e);
                                    if (cb != null) cb.onError(e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LevelDAO", "checkAch failed", e);
                        if (cb != null) cb.onError(e.getMessage());
                    });
        }
    }
