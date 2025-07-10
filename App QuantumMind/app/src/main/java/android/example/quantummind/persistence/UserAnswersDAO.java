// UserAnswersDAO.java
package android.example.quantummind.persistence;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import android.example.quantummind.domain.controllers.LessonController;
import android.example.quantummind.domain.callbacks.UserAnswersCallback;
import android.example.quantummind.domain.entities.Answer;
import android.example.quantummind.domain.entities.UserAnswers;
import android.content.Context;
import android.util.Log;

import java.util.List;

public class UserAnswersDAO {

    private final FirebaseFirestore firestore;

    public UserAnswersDAO(Context context) {
        firestore = DatabaseManager.getInstance(context).getFirestore();
    }

    public void getUserAnswers(@NonNull String userId, @NonNull UserAnswersCallback callback) {
        firestore.collection("userAnswers")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        UserAnswers answers = doc.toObject(UserAnswers.class);
                        callback.onSuccess(answers);
                    } else {
                        callback.onError("Answers not found.");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Error fetching answers: " + e.getMessage()));
    }

    public void saveUserAnswers(List<Answer> answers, LessonController.SaveAnswersCallback cb) {
        WriteBatch batch = firestore.batch();
        CollectionReference ref = firestore.collection("userAnswers");
        for (Answer a : answers) {
            batch.set(ref.document(), a.toMap());
        }
        batch.commit()
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e("LevelDAO", "saveAnswers failed", e);
                    cb.onError(e.getMessage());
                });
    }
}
