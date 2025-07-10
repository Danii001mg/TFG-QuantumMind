package android.example.quantummind.domain.callbacks;

import android.example.quantummind.domain.entities.UserAnswers;

public interface UserAnswersCallback {
    void onSuccess(UserAnswers answers);
    void onError(String errorMessage);
}
