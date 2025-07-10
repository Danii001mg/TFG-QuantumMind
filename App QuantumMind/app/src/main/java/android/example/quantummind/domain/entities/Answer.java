package android.example.quantummind.domain.entities;

import java.util.HashMap;
import java.util.Map;
public class Answer {
    private String userId;
    private String questionId;
    private int selectedAnswerIndex;

    public Answer() { }

    public Answer(String userId, String questionId, int selectedAnswerIndex) {
        this.userId = userId;
        this.questionId = questionId;
        this.selectedAnswerIndex = selectedAnswerIndex;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", userId);
        m.put("questionId", questionId);
        m.put("selectedAnswerIndex", selectedAnswerIndex);
        return m;
    }

    public String getUserId() {
        return userId;
    }
    public String getQuestionId() {
        return questionId;
    }
    public int getSelectedAnswerIndex() {
        return selectedAnswerIndex;
    }
}
