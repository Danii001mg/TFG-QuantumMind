package android.example.quantummind.domain;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class LoginController {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    public interface Callback {
        void onSuccess(User user);
        void onError(String errorMsg);
    }

    public LoginController() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void login(final String email, final String password, final Callback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser == null) {
                            callback.onError("Unexpected error: null user after authentication.");
                            return;
                        }
                        User domainUser = User.fromFirebaseUser(firebaseUser);
                        String uid = domainUser.getUid();

                        firestore.collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    boolean needSave =
                                            !documentSnapshot.exists() ||
                                                    !documentSnapshot.contains("displayName");

                                    if (needSave) {
                                        Map<String, Object> userData = domainUser.toMap();
                                        firestore.collection("users")
                                                .document(uid)
                                                .set(userData)
                                                .addOnSuccessListener(aVoid -> {
                                                    callback.onSuccess(domainUser);
                                                })
                                                .addOnFailureListener(e -> {
                                                    callback.onError("Error saving user on Firestore: "
                                                            + e.getMessage());
                                                });
                                    } else {
                                        callback.onSuccess(domainUser);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    callback.onError("Error reading Firestore: " + e.getMessage());
                                });

                    } else {
                        String message = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Unknown authentication error";
                        callback.onError(message);
                    }
                });
    }
}
