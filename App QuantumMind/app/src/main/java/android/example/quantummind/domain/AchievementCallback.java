package android.example.quantummind.domain;

public interface AchievementCallback {
    void onSuccess(AchievementItem[] achievements);
    void onError(String errorMessage);
}
