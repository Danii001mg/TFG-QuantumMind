package android.example.quantummind.persistence;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import android.content.Context;
import android.example.quantummind.domain.callbacks.QuestionCallback;
import android.example.quantummind.domain.entities.Question;
import android.example.quantummind.domain.entities.Questionary;

import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    private final FirebaseFirestore firestore;

    public QuestionDAO(Context context) {
        firestore = DatabaseManager.getInstance(context).getFirestore();
    }

    public void fetchQuestions(@NonNull String lessonId, final QuestionCallback callback) {
        firestore.collection("questions")
                .whereEqualTo("lessonId", lessonId)
                .orderBy("id")
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    List<Question> questions = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Question question = doc.toObject(Question.class);
                        if (question != null) {
                            questions.add(question);
                        }
                    }
                    callback.onSuccess(new Questionary(lessonId, questions));
                })
                .addOnFailureListener(e -> callback.onError("Error fetching questions: " + e.getMessage()));
    }

}
