package android.example.quantummind.domain.controllers;

import android.content.Context;

import androidx.annotation.Nullable;

import android.example.quantummind.persistence.DatabaseManager;
import android.example.quantummind.domain.entities.User;
import android.example.quantummind.persistence.UserDAO;

public class LoginController {

    public interface Callback {
        void onSuccess(@Nullable User user);
        void onError(String msg);
    }

    private final UserDAO userDAO;

    public LoginController(Context ctx) {
        DatabaseManager db = DatabaseManager.getInstance(ctx);
        this.userDAO = new UserDAO(db);
    }

    public void login(String email, String password, Callback cb) {
        userDAO.login(email, password, new UserDAO.AuthCallback() {
            @Override public void onSuccess(@Nullable User user) { cb.onSuccess(user); }
            @Override public void onError(String error) { cb.onError(error); }
        });
    }

    public void register(String displayName, String email, String password, Callback cb) {
        userDAO.register(displayName, email, password, new UserDAO.AuthCallback() {
            @Override public void onSuccess(@Nullable User user) { cb.onSuccess(user); }
            @Override public void onError(String error) { cb.onError(error); }
        });
    }

    public void resetPassword(String email, Callback cb) {
        userDAO.resetPassword(email, new UserDAO.AuthCallback() {
            @Override public void onSuccess(@Nullable User user) { cb.onSuccess(null); }
            @Override public void onError(String error) { cb.onError(error); }
        });
    }


    public void saveCredentials(String email, String password) {
        userDAO.saveCredentials(email, password);
    }

    public void clearCredentials() {
        userDAO.clearCredentials();
    }

    @Nullable
    public String[] loadCredentials() {
        return userDAO.loadCredentials();
    }
}
