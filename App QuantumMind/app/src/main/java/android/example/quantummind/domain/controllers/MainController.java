package android.example.quantummind.domain.controllers;

import android.content.Context;
import android.example.quantummind.domain.AchievementItem;
import android.example.quantummind.domain.RankingItem;
import android.example.quantummind.domain.callbacks.AchievementCallback;
import android.example.quantummind.domain.callbacks.RankingCallback;
import android.example.quantummind.domain.callbacks.UserAnswersCallback;
import android.example.quantummind.domain.callbacks.UserProgressCallback;
import android.example.quantummind.domain.entities.User;
import android.example.quantummind.domain.entities.UserAnswers;
import android.example.quantummind.domain.entities.UserProgress;
import android.example.quantummind.persistence.UserAchievementsDAO;
import android.example.quantummind.persistence.UserAnswersDAO;
import android.example.quantummind.persistence.UserProgressDAO;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MainController {

    private final UserProgressDAO userProgressDAO;
    private final UserAnswersDAO userAnswersDAO;
    private final UserAchievementsDAO userAchievementsDAO;

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    public interface ProgressCallback {
        void onSuccess(UserProgress progress);
        void onError(String errorMessage);
    }

    public interface AnswersCallback {
        void onSuccess(UserAnswers answers);
        void onError(String errorMessage);
    }


    public MainController(Context context) {
        userProgressDAO = new UserProgressDAO(context);
        userAnswersDAO = new UserAnswersDAO(context);
        userAchievementsDAO = new UserAchievementsDAO(context);
    }

    public void getCurrentUser(@NonNull UserCallback callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            User user = User.fromFirebaseUser(firebaseUser);
            callback.onSuccess(user);
        } else {
            callback.onError("User not authenticated.");
        }
    }

    public void getUserProgress(String userId, ProgressCallback callback) {
        userProgressDAO.getUserProgress(userId, new UserProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                callback.onSuccess(progress);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
    

    public void getUserAchievements(String userId, AchievementCallback callback) {
        userAchievementsDAO.getUserAchievements(userId, new AchievementCallback() {
            @Override
            public void onSuccess(AchievementItem[] achievements) {
                callback.onSuccess(achievements);
            }

            public void onUnlocked(String achievementName) {
                callback.onUnlocked(achievementName);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }


    public void getRanking(RankingCallback callback) {
        userProgressDAO.getRanking(new RankingCallback() {
            @Override
            public void onSuccess(List<RankingItem> rankingItems) {
                callback.onSuccess(rankingItems);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

}
