package android.example.quantummind.persistence;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import android.content.Context;
import android.example.quantummind.domain.entities.Sheet;
import android.example.quantummind.domain.callbacks.SheetsCallback;

import java.util.ArrayList;
import java.util.List;

public class SheetsDAO {
    private final FirebaseFirestore firestore;

    public SheetsDAO(Context context) {
        firestore = DatabaseManager.getInstance(context).getFirestore();
    }

    public void fetchSheets(@NonNull String lessonId, final SheetsCallback callback) {
        firestore.collection("sheets")
                .whereEqualTo("lessonId", lessonId)
                .orderBy("id")
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    List<Sheet> sheets = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Sheet sheet = Sheet.fromDocument(doc);
                        if (sheet != null) {
                            sheets.add(sheet);
                        }
                    }
                    callback.onSuccess(sheets);
                })
                .addOnFailureListener(e -> callback.onError("Error fetching sheets: " + e.getMessage()));
    }
}
