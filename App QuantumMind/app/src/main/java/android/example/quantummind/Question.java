package android.example.quantummind;

import java.util.List;

public class Question {
    private String id;
    private String lessonId;
    private String questionText;
    private List<String> answerOptions;
    private int correctAnswerIndex; // Index of the correct answer in the list of options
    private String nextQuestionId;

    public Question() {
        // Empty constructor needed for Firebase
    }

    public Question(String id, String lessonId, String questionText, List<String> answerOptions, int correctAnswerIndex, String nextQuestionId) {
        this.id = id;
        this.lessonId = lessonId;
        this.questionText = questionText;
        this.answerOptions = answerOptions;
        this.correctAnswerIndex = correctAnswerIndex;
        this.nextQuestionId = nextQuestionId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getAnswerOptions() {
        return answerOptions;
    }

    public void setAnswerOptions(List<String> answerOptions) {
        this.answerOptions = answerOptions;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getNextQuestionId() { return nextQuestionId; }

    public void setNextQuestionId(String nextQuestionId) {
        this.nextQuestionId = nextQuestionId;
    }
}
