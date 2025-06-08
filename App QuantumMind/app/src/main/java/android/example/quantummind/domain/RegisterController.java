package android.example.quantummind.domain;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterController {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public interface RegisterCallback {
        void onSuccess(User user);

        void onError(String errorMessage);
    }

    public RegisterController() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void registerUser(@NonNull String displayName,
                             @NonNull String email,
                             @NonNull String password,
                             @NonNull RegisterCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Failed to obtain newly created user.");
                        return;
                    }

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build();

                    firebaseUser.updateProfile(profileUpdates)
                            .addOnSuccessListener(aVoid -> {
                                User newUser = User.fromFirebaseUser(firebaseUser);

                                firestore.collection("users")
                                        .document(newUser.getUid())
                                        .set(newUser.toMap())
                                        .addOnSuccessListener(aVoid2 -> {
                                            Log.d("RegisterController", "User data successfully written to Firestore.");
                                            callback.onSuccess(newUser);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w("RegisterController", "Error writing user data to Firestore", e);
                                            callback.onError("Error saving user data: " + e.getMessage());
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.w("RegisterController", "Error updating display name", e);
                                callback.onError("Error updating profile: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Registration failed.";
                    Log.w("RegisterController", "Error creating user", e);
                    callback.onError("Error in register: " + errorMsg);
                });
    }
}
