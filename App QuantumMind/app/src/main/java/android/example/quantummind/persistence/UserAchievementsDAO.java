package android.example.quantummind.persistence;

import android.content.Context;
import android.example.quantummind.R;
import android.example.quantummind.domain.AchievementItem;
import android.example.quantummind.domain.entities.UserAchievements;
import android.example.quantummind.domain.callbacks.AchievementCallback;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class UserAchievementsDAO {

    private final FirebaseFirestore firestore;

    public UserAchievementsDAO(Context context) {
        firestore = DatabaseManager.getInstance(context).getFirestore();
    }

    public void getUserAchievements(@NonNull String userId, @NonNull AchievementCallback callback) {
        firestore.collection("userAchievements")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    // Construir los logros aunque el documento esté vacío o no exista
                    AchievementItem[] achievements = new AchievementItem[7];
                    achievements[0] = new AchievementItem(
                            R.drawable.ic_first_badge,
                            "First step",
                            "Complete your first question",
                            doc.getBoolean("first_step") != null && doc.getBoolean("first_step")
                    );
                    achievements[1] = new AchievementItem(
                            R.drawable.ic_10_badge,
                            "Question hunter",
                            "Answer correctly all the questions from a level",
                            doc.getBoolean("question_hunter") != null && doc.getBoolean("question_hunter")
                    );
                    achievements[2] = new AchievementItem(
                            R.drawable.ic_6levels_badge,
                            "Knowledge master",
                            "Complete all the levels",
                            doc.getBoolean("knowledge_master") != null && doc.getBoolean("knowledge_master")
                    );
                    achievements[3] = new AchievementItem(
                            R.drawable.ic_complete_levels_badge,
                            "Genius",
                            "Get a score of 8/10 or more in every level",
                            doc.getBoolean("genius") != null && doc.getBoolean("genius")
                    );
                    achievements[4] = new AchievementItem(
                            R.drawable.ic_1min_badge,
                            "Fast and furious",
                            "Complete a questionnaire in less than 1 minute",
                            doc.getBoolean("fast_and_furious") != null && doc.getBoolean("fast_and_furious")
                    );
                    achievements[5] = new AchievementItem(
                            R.drawable.ic_marathon_badge,
                            "Mental marathon",
                            "Complete all the levels in one session",
                            doc.getBoolean("mental_marathon") != null && doc.getBoolean("mental_marathon")
                    );
                    achievements[6] = new AchievementItem(
                            R.drawable.ic_improve_badge,
                            "Always improving",
                            "Improve your score in a level",
                            doc.getBoolean("always_improving") != null && doc.getBoolean("always_improving")
                    );

                    callback.onSuccess(achievements);
                })
                .addOnFailureListener(e -> callback.onError("Error fetching achievements: " + e.getMessage()));
    }
}
