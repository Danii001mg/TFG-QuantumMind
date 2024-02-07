package android.example.quantummind;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;
import java.util.UUID;

public class Question implements Parcelable {
    private String questionText;
    private List<String> choices;
    private String correctAnswer;
    private String id;

    public Question(String questionText, List<String> choices, String correctAnswer) {
        this.id = UUID.randomUUID().toString(); // Genera un ID único
        this.questionText = questionText;
        this.choices = choices;
        this.correctAnswer = correctAnswer;
    }

    protected Question(Parcel in) {
        id = in.readString();
        questionText = in.readString();
        choices = in.createStringArrayList();
        correctAnswer = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(questionText);
        dest.writeStringList(choices);
        dest.writeString(correctAnswer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters
    public String getQuestionText() { return questionText; }
    public List<String> getChoices() { return choices; }
    public String getCorrectAnswer() { return correctAnswer; }

    public String getId() { return id; }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };
}
