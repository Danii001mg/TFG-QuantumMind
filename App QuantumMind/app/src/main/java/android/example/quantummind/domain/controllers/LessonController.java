package android.example.quantummind.domain.controllers;

import android.content.Context;
import android.example.quantummind.domain.callbacks.AchievementCallback;
import android.example.quantummind.domain.callbacks.QuestionCallback;
import android.example.quantummind.domain.callbacks.SheetsCallback;
import android.example.quantummind.domain.entities.Answer;
import android.example.quantummind.persistence.QuestionDAO;
import android.example.quantummind.persistence.SheetsDAO;
import android.example.quantummind.persistence.UserAnswersDAO;
import android.example.quantummind.persistence.UserProgressDAO;

import java.util.List;

public class LessonController {

    private final SheetsDAO sheetsDAO;
    private final QuestionDAO questionsDAO;
    private final UserProgressDAO userProgressDAO;
    private final UserAnswersDAO userAnswersDAO;

    public LessonController(Context ctx) {
        this.sheetsDAO = new SheetsDAO(ctx);
        this.questionsDAO = new QuestionDAO(ctx);
        this.userProgressDAO = new UserProgressDAO(ctx);
        this.userAnswersDAO = new UserAnswersDAO(ctx);
    }

    public interface SaveAnswersCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface ProgressCallback {
        void onSuccess();
        void onError(String errorMessage);
    }


    public void fetchSheets(String lessonId, SheetsCallback callback) {
        sheetsDAO.fetchSheets(lessonId, callback);
    }

    public void fetchQuestions(String lessonId, QuestionCallback callback) {
        questionsDAO.fetchQuestions(lessonId, callback);
    }

    public void saveUserProgress(String userId,
                                 String lessonId,
                                 double newScore,
                                 ProgressCallback cb) {
        userProgressDAO.saveUserProgress(userId, lessonId, newScore, cb);
    }

    public void unlockAchievement(String userId,
                                  String key,
                                  String name,
                                  AchievementCallback cb) {
        userProgressDAO.unlockAchievement(userId, key, name, cb);
    }

    public void saveUserAnswers(List<Answer> answers, SaveAnswersCallback cb){
        userAnswersDAO.saveUserAnswers(answers, cb);
    }
}
