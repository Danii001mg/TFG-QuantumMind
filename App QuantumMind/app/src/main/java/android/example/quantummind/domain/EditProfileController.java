package android.example.quantummind.domain;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class EditProfileController {

    private final FirebaseAuth auth;
    private final FirebaseStorage storage;
    private final FirebaseFirestore firestore;

    public EditProfileController() {
        this.auth = FirebaseAuth.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public interface ImageUploadCallback {
        void onUploadSuccess(@NonNull Uri photoUri);
        void onUploadFailure(@NonNull String errorMessage);
    }

    public interface Callback {
        void onSuccess();
        void onError(@NonNull String errorMessage);
    }

    public interface ReauthCallback {
        void onReauthSuccess();
        void onReauthFailure(@NonNull String errorMessage);
    }

    public void uploadProfileImage(@NonNull Uri imageUri, @NonNull ImageUploadCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            callback.onUploadFailure("User is not authenticated.");
            return;
        }

        String uid = user.getUid();
        StorageReference profileImagesRef = storage
                .getReference()
                .child("profileImages/" + uid + ".jpg");

        profileImagesRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        profileImagesRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    UserProfileChangeRequest profileUpdates =
                                            new UserProfileChangeRequest.Builder()
                                                    .setPhotoUri(downloadUri)
                                                    .build();

                                    user.updateProfile(profileUpdates)
                                            .addOnSuccessListener(aVoid ->
                                                    callback.onUploadSuccess(downloadUri))
                                            .addOnFailureListener(e ->
                                                    callback.onUploadFailure(
                                                            "Failed to update user photo: " + e.getMessage()
                                                    ));
                                })
                                .addOnFailureListener(e ->
                                        callback.onUploadFailure(
                                                "Failed to retrieve download URL: " + e.getMessage()
                                        ))
                )
                .addOnFailureListener(e ->
                        callback.onUploadFailure(
                                "Failed to upload image: " + e.getMessage()
                        ));
    }
    public void updateDisplayName(@NonNull String newDisplayName, @NonNull Callback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            callback.onError("User is not authenticated.");
            return;
        }

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .build();

        user.updateProfile(request)
                .addOnSuccessListener(aVoid -> {
                    String uid = user.getUid();
                    firestore.collection("users")
                            .document(uid)
                            .update("displayName", newDisplayName)
                            .addOnSuccessListener(aVoid2 -> {
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                callback.onError("Failed to update Firestore: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e ->
                        callback.onError("Failed to update display name: " + e.getMessage()));
    }
    public void reauthenticateUser(@NonNull String currentPassword, @NonNull ReauthCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.onReauthFailure("User is not authenticated.");
            return;
        }

        String existingEmail = user.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(existingEmail, currentPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> callback.onReauthSuccess())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        callback.onReauthFailure("Invalid password.");
                    } else {
                        callback.onReauthFailure("Reauthentication failed: " + e.getMessage());
                    }
                });
    }
    public void updateEmail(@NonNull String newEmail, @NonNull Callback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            callback.onError("User is not authenticated.");
            return;
        }

        user.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onError("Failed to send verification for new email: " + e.getMessage()));
    }

    public void updatePassword(@NonNull String newPassword, @NonNull Callback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            callback.onError("User is not authenticated.");
            return;
        }

        user.updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onError("Failed to update password: " + e.getMessage()));
    }
}
