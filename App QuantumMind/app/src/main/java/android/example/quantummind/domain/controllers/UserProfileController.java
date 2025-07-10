package android.example.quantummind.domain.controllers;

import android.content.Context;
import android.example.quantummind.persistence.DatabaseManager;
import android.net.Uri;
import android.example.quantummind.domain.entities.User;
import android.example.quantummind.persistence.UserDAO;

public class UserProfileController {

    private final UserDAO dao;

    public interface ProfileCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    public interface ImageUploadCallback {
        void onUploadSuccess(Uri photoUri);
        void onUploadFailure(String errorMessage);
    }

    public interface ReauthCallback {
        void onReauthSuccess();
        void onReauthFailure(String errorMessage);
    }


    public UserProfileController(Context context) {
        this.dao = new UserDAO(DatabaseManager.getInstance(context));
    }

    public void getUserProfile(final ProfileCallback callback) {
        dao.getUserProfile(new UserDAO.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void uploadProfileImage(Uri imageUri, final ImageUploadCallback callback) {
        dao.uploadProfileImage(imageUri, new UserDAO.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onUploadSuccess(Uri.parse(user.getPhotoUrl()));
            }

            @Override
            public void onError(String errorMessage) {
                callback.onUploadFailure(errorMessage);
            }
        });
    }

    public void updateDisplayName(String newName, ProfileCallback callback) {
        dao.updateDisplayName(newName, new UserDAO.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void updateEmail(String newEmail, ProfileCallback callback) {
        dao.updateEmail(newEmail, new UserDAO.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void updatePassword(String newPassword, ProfileCallback callback) {
        dao.updatePassword(newPassword, new UserDAO.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void reauthenticateUser(String currentPassword, final ReauthCallback callback) {
        dao.reauthenticate(currentPassword, new UserDAO.ReauthCallback() {
            @Override
            public void onSuccess() {
                callback.onReauthSuccess();
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onReauthFailure(errorMessage);
            }
        });
    }

    public void logout() {
        dao.logout();
    }

}
