package android.example.quantummind.domain.entities;

public class UserAnswers {

    private String questionId;
    private int selectedAnswerIndex;
    private String userId;

    public UserAnswers() {
    }

    public UserAnswers(String questionId, int selectedAnswerIndex, String userId) {
        this.questionId = questionId;
        this.selectedAnswerIndex = selectedAnswerIndex;
        this.userId = userId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public int getSelectedAnswerIndex() {
        return selectedAnswerIndex;
    }

    public void setSelectedAnswerIndex(int selectedAnswerIndex) {
        this.selectedAnswerIndex = selectedAnswerIndex;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
