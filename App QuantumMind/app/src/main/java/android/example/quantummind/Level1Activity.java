package android.example.quantummind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Level1Activity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Question currentQuestion;
    private String userId;
    private int currentQuestionNumber = 1;
    private int totalNumberOfQuestions = 0;
    private int correctAnswersCount = 0;
    private TextView questionCounter;
    private TextView percentageCorrect;
    private ProgressBar questionProgressBar;
    private TextView correctAnswerText;
    private TextView questionText;
    private RadioGroup answersGroup;
    private List<Question> questionsList = new ArrayList<>();
    private int currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level1);

        String lessonId = getIntent().getStringExtra("LESSON_ID");

        TextView lessonTextView = findViewById(R.id.lessonText);
        ScrollView scrollView = findViewById(R.id.scrollview);
        ImageView button = findViewById(R.id.bottomRightButton);
        questionText = findViewById(R.id.questionText);
        answersGroup = findViewById(R.id.answersGroup);
        Button acceptButton = findViewById(R.id.acceptButton);
        LinearLayout progressBar = findViewById(R.id.progressBar);
        Button checkButton = findViewById(R.id.checkButton);
        questionCounter = findViewById(R.id.questionCounter);
        percentageCorrect = findViewById(R.id.percentageCorrect);
        questionProgressBar = findViewById(R.id.questionProgressBar);
        correctAnswerText = findViewById(R.id.correctAnswerText);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (lessonId != null && !lessonId.isEmpty()) {
            loadLessonContent(lessonTextView);
            loadQuestionsForLesson(lessonId);
        } else {
            Toast.makeText(this, "No lesson ID provided", Toast.LENGTH_LONG).show();
            finish();
        }

        button.setOnClickListener(v -> {
            scrollView.setVisibility(View.GONE);
            button.setVisibility(View.GONE);

            questionText.setVisibility(View.VISIBLE);
            answersGroup.setVisibility(View.VISIBLE);
            acceptButton.setVisibility(View.VISIBLE);
            checkButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        });

        acceptButton.setEnabled(false);

        checkButton.setOnClickListener(v -> {
            checkAnswer(answersGroup);
            acceptButton.setEnabled(true);
        });

        acceptButton.setOnClickListener(v -> {
            currentQuestionIndex++;
            if (currentQuestionIndex < questionsList.size()) {
                loadQuestionIntoView(currentQuestionIndex);
            } else {
                finishLevel();
            }
            acceptButton.setEnabled(false);
        });

    }

    private void checkAnswer(RadioGroup answersGroup) {
        int selectedAnswerIndex = answersGroup.indexOfChild(findViewById(answersGroup.getCheckedRadioButtonId()));

        int radioButtonID = answersGroup.getCheckedRadioButtonId();
        if (radioButtonID != -1) {
            selectedAnswerIndex = answersGroup.indexOfChild(findViewById(radioButtonID));
        } else {
            Toast.makeText(Level1Activity.this, "Please select an answer.", Toast.LENGTH_SHORT).show();
        }

        if (selectedAnswerIndex == currentQuestion.getCorrectAnswerIndex()) {
            correctAnswersCount++;
            correctAnswerText.setVisibility(View.GONE);

        } else {
            String correctAnswer = currentQuestion.getAnswerOptions().get(currentQuestion.getCorrectAnswerIndex());
            correctAnswerText.setText("La respuesta correcta es: " + correctAnswer);
            correctAnswerText.setVisibility(View.VISIBLE);
        }

        updatePercentageCorrect();
        updateProgressBar();

        Map<String, Object> userAnswer = new HashMap<>();
        userAnswer.put("userId", userId);
        userAnswer.put("questionId", currentQuestion.getId());
        userAnswer.put("selectedAnswerIndex", selectedAnswerIndex);

        db.collection("userAnswers").add(userAnswer)
                .addOnSuccessListener(documentReference -> Toast.makeText(Level1Activity.this, "Answer saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(Level1Activity.this, "Failed to save answer", Toast.LENGTH_SHORT).show());
    }

    private void updatePercentageCorrect() {
        if (totalNumberOfQuestions > 0) {
            double percentage = (double) correctAnswersCount / totalNumberOfQuestions * 100;
            percentageCorrect.setText(String.format("%.0f%%", percentage));
        }
    }

    private void updateProgressBar() {
        if (totalNumberOfQuestions > 0) {
            int progress = (currentQuestionNumber - 1) * 100 / totalNumberOfQuestions;
            questionProgressBar.setProgress(progress);

            int secondaryProgress = correctAnswersCount * 100 / totalNumberOfQuestions;
            questionProgressBar.setSecondaryProgress(secondaryProgress);
        }
    }

    private void finishLevel() {
        double percentage = (double) correctAnswersCount / totalNumberOfQuestions * 100;

        if (userId != null) {
            saveUserProgress(userId, percentage);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("LEVEL1_SCORE", percentage);
        startActivity(intent);
        finish();
    }

    private void loadQuestionsForLesson(String lessonId) {
        db.collection("questions")
                .whereEqualTo("lessonId", lessonId)
                .orderBy("id")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        questionsList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Question question = document.toObject(Question.class);
                            questionsList.add(question);
                        }
                        totalNumberOfQuestions = questionsList.size();
                        questionCounter.setText("1/" + totalNumberOfQuestions);
                        loadQuestionIntoView(0); // Carga la primera pregunta
                    } else {
                        Log.w("QuestionLoad", "Error getting questions.", task.getException());
                    }
                });
    }

    private void loadQuestionIntoView(int questionIndex) {
        if (questionIndex < questionsList.size()) {
            currentQuestion = questionsList.get(questionIndex);
            questionText.setText(currentQuestion.getQuestionText());
            answersGroup.removeAllViews();

            for (String answer : currentQuestion.getAnswerOptions()) {
                RadioButton rb = new RadioButton(this);
                rb.setId(View.generateViewId());
                rb.setText(answer);
                answersGroup.addView(rb);
            }

            questionCounter.setText((currentQuestionIndex + 1) + "/" + totalNumberOfQuestions);
            updateProgressBar();
        } else {
            finishLevel();
        }
    }

    private void loadLessonContent(TextView lessonTextView) {
        DocumentReference lessonRef = db.collection("lessons").document("example");
        lessonRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Lesson lesson = document.toObject(Lesson.class);
                    lessonTextView.setText(lesson.getContent());
                } else {
                    lessonTextView.setText("Lesson content not found.");
                }
            } else {
                lessonTextView.setText("Error loading lesson content.");
            }
        });
    }


    private void saveUserProgress(String userId, double score) {
        DocumentReference userProgressRef = db.collection("userProgress").document(userId);
        Map<String, Object> levelProgress = new HashMap<>();
        levelProgress.put("level1_completed", true);
        levelProgress.put("level1_score", score);

        userProgressRef.set(levelProgress, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Progress", "Level progress updated."))
                .addOnFailureListener(e -> Log.w("Progress", "Error updating level progress.", e));
    }

}
