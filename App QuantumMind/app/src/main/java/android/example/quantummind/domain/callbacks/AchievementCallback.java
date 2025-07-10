package android.example.quantummind.domain.callbacks;

import android.example.quantummind.domain.AchievementItem;

public interface AchievementCallback {
    void onSuccess(AchievementItem[] achievements);

    void onUnlocked(String achievementName);

    void onError(String errorMessage);
}
