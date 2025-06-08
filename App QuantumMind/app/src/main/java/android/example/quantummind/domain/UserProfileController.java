package android.example.quantummind.domain;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class UserProfileController {

    private final FirebaseAuth auth;
    private final FirebaseStorage storage;

    public interface ProfileCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    public interface ImageUploadCallback {
        void onUploadSuccess(@NonNull Uri photoUri);
        void onUploadFailure(@NonNull String errorMessage);
    }

    public UserProfileController() {
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public void getUserProfile(@NonNull ProfileCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            User user = User.fromFirebaseUser(firebaseUser);
            callback.onSuccess(user);
        } else {
            callback.onError("User not logged in.");
        }
    }

    public void uploadProfileImage(@NonNull Uri imageUri, @NonNull ImageUploadCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onUploadFailure("User not logged in.");
            return;
        }

        String uid = firebaseUser.getUid();
        StorageReference profileImagesRef = storage
                .getReference()
                .child("profileImages/" + uid + ".jpg");

        profileImagesRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        profileImagesRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri)
                                            .build();

                                    firebaseUser.updateProfile(request)
                                            .addOnSuccessListener(aVoid -> {
                                                callback.onUploadSuccess(uri);
                                            })
                                            .addOnFailureListener(e -> {
                                                callback.onUploadFailure("Failed to update photo: " + e.getMessage());
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    callback.onUploadFailure("Failed to get download URL: " + e.getMessage());
                                })
                )
                .addOnFailureListener(e -> {
                    callback.onUploadFailure("Failed to upload image: " + e.getMessage());
                });
    }


    public void logout() {
        auth.signOut();
    }
}
