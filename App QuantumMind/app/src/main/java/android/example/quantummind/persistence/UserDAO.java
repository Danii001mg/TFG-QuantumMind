package android.example.quantummind.persistence;

import android.content.SharedPreferences;
import android.example.quantummind.domain.entities.User;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class UserDAO {

    public interface AuthCallback {
        void onSuccess(@Nullable User user);
        void onError(String errorMessage);
    }

    public interface ReauthCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    private final DatabaseManager db;
    private final FirebaseStorage storage;

    public UserDAO(DatabaseManager db) {
        this.db = db;
        this.storage = FirebaseStorage.getInstance();
    }

    public void login(String email, String password, AuthCallback cb) {
        db.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {
                    if (!authTask.isSuccessful()) {
                        String msg = authTask.getException() != null
                                ? authTask.getException().getMessage()
                                : "Unknown authentication error";
                        cb.onError(msg);
                        return;
                    }
                    FirebaseUser fUser = db.getAuth().getCurrentUser();
                    if (fUser == null) {
                        cb.onError("Unexpected error: null user after authentication.");
                        return;
                    }

                    User domainUser = User.fromFirebaseUser(fUser);
                    String uid = domainUser.getUid();

                    db.getFirestore()
                            .collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(doc -> {
                                boolean needSave = !doc.exists() || !doc.contains("displayName");
                                if (needSave) {
                                    Map<String,Object> data = domainUser.toMap();
                                    db.getFirestore()
                                            .collection("users")
                                            .document(uid)
                                            .set(data)
                                            .addOnSuccessListener(a -> cb.onSuccess(domainUser))
                                            .addOnFailureListener(e -> cb.onError("Error saving user: " + e.getMessage()));
                                } else {
                                    cb.onSuccess(domainUser);
                                }
                            })
                            .addOnFailureListener(e -> cb.onError("Error fetching user: " + e.getMessage()));
                });
    }

    public void register(String displayName, String email, String password, AuthCallback cb) {
        db.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fUser = authResult.getUser();
                    if (fUser == null) {
                        cb.onError("Failed to obtain newly created user.");
                        return;
                    }

                    fUser.updateProfile(
                            new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build()
                    ).addOnSuccessListener(aVoid -> {

                        fUser.sendEmailVerification()
                                .addOnSuccessListener(verificationVoid -> {
                                    User domainUser = User.fromFirebaseUser(fUser);
                                    Map<String, Object> data = domainUser.toMap();
                                    db.getFirestore()
                                            .collection("users")
                                            .document(domainUser.getUid())
                                            .set(data)
                                            .addOnSuccessListener(aVoid2 -> {
                                                cb.onSuccess(domainUser);
                                            })
                                            .addOnFailureListener(e -> {
                                                cb.onError("Error saving user data: " + e.getMessage());
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    cb.onError("Error sending verification email: " + e.getMessage());
                                });

                    }).addOnFailureListener(e -> {
                        cb.onError("Error updating profile: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    String msg = e.getMessage() != null ? e.getMessage() : "Registration failed.";
                    cb.onError("Error in register: " + msg);
                });
    }

    public void resetPassword(String email, AuthCallback cb) {
        db.getAuth().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cb.onSuccess(null);
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Error sending reset email";
                        cb.onError(msg);
                    }
                });
    }

    public void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = db.getCredentialsPrefs().edit();
        editor.putString("email", email)
                .putString("password", password)
                .apply();
        db.getCheckboxPrefs().edit()
                .putBoolean("rememberCredentials", true)
                .apply();
    }

    public void clearCredentials() {
        db.getCredentialsPrefs().edit().clear().apply();
        db.getCheckboxPrefs().edit()
                .putBoolean("rememberCredentials", false)
                .apply();
    }

    @Nullable
    public String[] loadCredentials() {
        boolean remember = db.getCheckboxPrefs().getBoolean("rememberCredentials", false);
        if (!remember) return null;
        String email = db.getCredentialsPrefs().getString("email", "");
        String pw    = db.getCredentialsPrefs().getString("password", "");
        return new String[]{ email, pw };
    }

    public void getUserProfile(AuthCallback cb) {
        FirebaseUser firebaseUser = db.getAuth().getCurrentUser();
        if (firebaseUser != null) {
            User user = User.fromFirebaseUser(firebaseUser);
            cb.onSuccess(user);
        } else {
            cb.onError("User not logged in.");
        }
    }

    public void uploadProfileImage(Uri imageUri, AuthCallback cb) {
        FirebaseUser firebaseUser = db.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            cb.onError("User not logged in.");
            return;
        }

        String uid = firebaseUser.getUid();
        StorageReference ref = storage
                .getReference()
                .child("profileImages/" + uid + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(task -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            firebaseUser.updateProfile(
                                    new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri)
                                            .build()
                            ).addOnSuccessListener(aVoid -> {
                                cb.onSuccess(null); // On success, you can send the updated user or any additional information
                            }).addOnFailureListener(e -> cb.onError("Failed to update photo: " + e.getMessage()));
                        })
                        .addOnFailureListener(e -> cb.onError("Failed to get download URL: " + e.getMessage()))
                )
                .addOnFailureListener(e -> cb.onError("Failed to upload image: " + e.getMessage()));
    }

    public void updateDisplayName(String newName, AuthCallback cb) {
        FirebaseUser firebaseUser = db.getAuth().getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.updateProfile(
                    new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(newName)
                            .build()
            ).addOnSuccessListener(aVoid -> {
                db.getFirestore().collection("users")
                        .document(firebaseUser.getUid())
                        .update("displayName", newName)
                        .addOnSuccessListener(a -> cb.onSuccess(null))
                        .addOnFailureListener(e -> cb.onError("Failed to update display name: " + e.getMessage()));
            }).addOnFailureListener(e -> cb.onError("Failed to update display name: " + e.getMessage()));
        } else {
            cb.onError("User not logged in.");
        }
    }

    public void updateEmail(String newEmail, AuthCallback cb) {
        FirebaseUser firebaseUser = db.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            cb.onError("User not logged in.");
            return;
        }

        if (!firebaseUser.isEmailVerified()) {
            firebaseUser.sendEmailVerification()
                    .addOnSuccessListener(aVoid -> {
                        cb.onError("Please verify your current email first. A verification email has been sent.");
                    })
                    .addOnFailureListener(e -> {
                        proceedWithEmailUpdate(firebaseUser, newEmail, cb);
                    });
            return;
        }

        proceedWithEmailUpdate(firebaseUser, newEmail, cb);
    }

    private void proceedWithEmailUpdate(FirebaseUser firebaseUser, String newEmail, AuthCallback cb) {
        firebaseUser.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener(aVoid -> {
                    db.getFirestore().collection("users")
                            .document(firebaseUser.getUid())
                            .update("email", newEmail)
                            .addOnSuccessListener(a -> {
                                firebaseUser.sendEmailVerification()
                                        .addOnSuccessListener(verification -> {
                                            cb.onSuccess(null);
                                        })
                                        .addOnFailureListener(e -> {
                                            cb.onSuccess(null);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                cb.onError("Email updated in Firebase Auth but failed to update in database: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    String errorMessage = "Failed to update email: ";
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("requires-recent-login")) {
                            errorMessage += "This operation requires recent authentication. Please try again.";
                        } else if (e.getMessage().contains("email-already-in-use")) {
                            errorMessage += "This email address is already in use by another account.";
                        } else if (e.getMessage().contains("invalid-email")) {
                            errorMessage += "The email address is not valid.";
                        } else if (e.getMessage().contains("operation-not-allowed")) {
                            errorMessage += "Email change is not enabled. Please contact support.";
                        } else {
                            errorMessage += e.getMessage();
                        }
                    } else {
                        errorMessage += "Unknown error occurred.";
                    }
                    cb.onError(errorMessage);
                });
    }


    public void updatePassword(String newPassword, AuthCallback cb) {
        FirebaseUser firebaseUser = db.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            cb.onError("User not logged in.");
            return;
        }

        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> {
                    String email = firebaseUser.getEmail();
                    if (email != null && !email.isEmpty()) {
                        boolean rememberCredentials = db.getCheckboxPrefs().getBoolean("rememberCredentials", false);
                        if (rememberCredentials) {
                            saveCredentials(email, newPassword);
                        }
                    }
                    cb.onSuccess(null);
                })
                .addOnFailureListener(e -> cb.onError("Failed to update password: " + e.getMessage()));
    }

    public void reauthenticate(String currentPassword, ReauthCallback cb) {
        FirebaseUser firebaseUser = db.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            cb.onFailure("User not authenticated.");
            return;
        }

        String email = firebaseUser.getEmail();
        if (email == null || email.isEmpty()) {
            cb.onFailure("User email not available for reauthentication.");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
        firebaseUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    cb.onSuccess();
                })
                .addOnFailureListener(e -> {
                    String errorMessage;
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        errorMessage = "The password you entered is incorrect.";
                    } else if (e.getMessage() != null) {
                        if (e.getMessage().contains("invalid-credential")) {
                            errorMessage = "Invalid credentials provided.";
                        } else if (e.getMessage().contains("user-mismatch")) {
                            errorMessage = "The credentials do not match the current user.";
                        } else if (e.getMessage().contains("user-not-found")) {
                            errorMessage = "User account not found.";
                        } else {
                            errorMessage = "Authentication failed: " + e.getMessage();
                        }
                    } else {
                        errorMessage = "Reauthentication failed. Please try again.";
                    }
                    cb.onFailure(errorMessage);
                });
    }

    public void logout() {
        db.getAuth().signOut();
    }
}
