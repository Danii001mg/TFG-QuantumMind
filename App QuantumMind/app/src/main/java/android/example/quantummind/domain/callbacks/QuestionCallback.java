package android.example.quantummind.domain.callbacks;

import android.example.quantummind.domain.entities.Questionary;

public interface QuestionCallback {
    void onSuccess(Questionary questionary);
    void onError(String errorMessage);
}
