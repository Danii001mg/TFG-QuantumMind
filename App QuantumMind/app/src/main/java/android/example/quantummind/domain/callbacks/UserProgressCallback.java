package android.example.quantummind.domain.callbacks;

import android.example.quantummind.domain.entities.UserProgress;

public interface UserProgressCallback {
    void onSuccess(UserProgress progress);
    void onError(String errorMessage);
}
