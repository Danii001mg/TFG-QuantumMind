package android.example.quantummind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelActivity extends AppCompatActivity {

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
    private Button acceptButton, backToLesson, backToQuestions;
    private TextView title;
    private ImageView lessonImage;
    private WebView lessonVideo;
    private TextView lessonTextView;
    private ImageView nextButton;
    private ImageView previousButton;
    private LinearLayout progressBar;
    private Button checkButton;
    private List<Question> questionsList = new ArrayList<>();
    private List<DocumentSnapshot> sheetDocuments = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int currentSheetIndex = 0;
    private String lessonId;
    private boolean backToLessonUsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level1);

        lessonId = getIntent().getStringExtra("LESSON_ID");

        lessonTextView = findViewById(R.id.lessonText);
        nextButton = findViewById(R.id.bottomRightButton);
        previousButton = findViewById(R.id.bottomLeftButton);
        questionText = findViewById(R.id.questionText);
        answersGroup = findViewById(R.id.answersGroup);
        acceptButton = findViewById(R.id.acceptButton);
        progressBar = findViewById(R.id.progressBar);
        checkButton = findViewById(R.id.checkButton);
        questionCounter = findViewById(R.id.questionCounter);
        percentageCorrect = findViewById(R.id.percentageCorrect);
        questionProgressBar = findViewById(R.id.questionProgressBar);
        correctAnswerText = findViewById(R.id.correctAnswerText);
        title = findViewById(R.id.title);
        lessonImage = findViewById(R.id.lessonImage);
        lessonVideo = findViewById(R.id.lessonVideo);
        backToLesson = findViewById(R.id.backToLesson);
        backToQuestions = findViewById(R.id.backToQuestions);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (lessonId != null && !lessonId.isEmpty()) {
            loadSheetsForLesson(lessonId);
            loadQuestionsForLesson(lessonId);
        } else {
            Toast.makeText(this, "No lesson ID provided", Toast.LENGTH_LONG).show();
            finish();
        }

        nextButton.setOnClickListener(v -> {
            if (currentSheetIndex < sheetDocuments.size() - 1) {
                currentSheetIndex++;
                showSheet(sheetDocuments.get(currentSheetIndex));
                previousButton.setVisibility(View.VISIBLE);
            } else {
                showQuestions();
            }
        });

        previousButton.setOnClickListener(v -> {
            if (currentSheetIndex > 0) {
                currentSheetIndex--;
                showSheet(sheetDocuments.get(currentSheetIndex));
            }
            if (currentSheetIndex == 0) {
                previousButton.setVisibility(View.GONE);
            }
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

        backToLesson.setOnClickListener(v -> {
            backToLessonUsed = true;
            questionText.setVisibility(View.GONE);
            answersGroup.setVisibility(View.GONE);
            acceptButton.setVisibility(View.GONE);
            checkButton.setVisibility(View.GONE);
            backToLesson.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            correctAnswerText.setVisibility(View.GONE);
            showSheet(sheetDocuments.get(currentSheetIndex)); // Returns to the current lesson sheet
            backToQuestions.setVisibility(View.VISIBLE); // Shows the button to go back to questions
        });

        backToQuestions.setOnClickListener(v -> {
            showQuestions(); // Returns to the current question
            backToQuestions.setVisibility(View.GONE); // Hides itself as it's now irrelevant
        });
    }

    private void checkAnswer(RadioGroup answersGroup) {
        int selectedAnswerIndex = answersGroup.indexOfChild(findViewById(answersGroup.getCheckedRadioButtonId()));

        int radioButtonID = answersGroup.getCheckedRadioButtonId();
        if (radioButtonID != -1) {
            selectedAnswerIndex = answersGroup.indexOfChild(findViewById(radioButtonID));
        } else {
            Toast.makeText(LevelActivity.this, "Please select an answer.", Toast.LENGTH_SHORT).show();
        }

        if (selectedAnswerIndex == currentQuestion.getCorrectAnswerIndex()) {
            correctAnswersCount++;
            correctAnswerText.setVisibility(View.GONE);

        } else {
            String correctAnswer = currentQuestion.getAnswerOptions().get(currentQuestion.getCorrectAnswerIndex());
            correctAnswerText.setText("The correct answer is: " + correctAnswer);
            correctAnswerText.setVisibility(View.VISIBLE);
        }

        updatePercentageCorrect();
        updateProgressBar();

        Map<String, Object> userAnswer = new HashMap<>();
        userAnswer.put("userId", userId);
        userAnswer.put("questionId", currentQuestion.getId());
        userAnswer.put("selectedAnswerIndex", selectedAnswerIndex);

        db.collection("userAnswers").add(userAnswer)
                .addOnSuccessListener(documentReference -> Toast.makeText(LevelActivity.this, "Answer saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(LevelActivity.this, "Failed to save answer", Toast.LENGTH_SHORT).show());
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
        if (lessonId.equals("lesson1")) {
            intent.putExtra("LEVEL1_SCORE", percentage);
        } else if (lessonId.equals("lesson2")) {
            intent.putExtra("LEVEL2_SCORE", percentage);
        }
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

    private void loadSheetsForLesson(String lessonId) {
        db.collection("sheets")
                .whereEqualTo("lessonId", lessonId)
                .orderBy("id")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sheetDocuments.clear();
                        sheetDocuments.addAll(task.getResult().getDocuments());
                        if (!sheetDocuments.isEmpty()) {
                            showSheet(sheetDocuments.get(0));
                        }
                    } else {
                        Log.w("SheetLoad", "Error getting sheets.", task.getException());
                    }
                });
    }

    private void showSheet(DocumentSnapshot sheetDocument) {
        lessonTextView.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        String sheetText = sheetDocument.getString("text");
        String sheetTitle = sheetDocument.getString("title");
        String imageUrl = sheetDocument.getString("image");
        String videoUrl = sheetDocument.getString("video");

        sheetText = sheetText.replace("||", "\n");
        lessonTextView.setText(sheetText);
        title.setText(sheetTitle);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(lessonImage);
            lessonImage.setVisibility(View.VISIBLE);
        } else {
            lessonImage.setVisibility(View.GONE);
        }

        if (videoUrl != null && !videoUrl.isEmpty()) {
            String frameVideo = "<html><body><iframe width=\"match_parent\" height=\"wrap_content\" src=\"" + videoUrl + "\" frameborder=\"0\" allowfullscreen></iframe></body></html>";
            lessonVideo.setVisibility(View.VISIBLE);
            lessonVideo.getSettings().setJavaScriptEnabled(true);
            lessonVideo.loadData(frameVideo, "text/html", "utf-8");
        } else {
            lessonVideo.setVisibility(View.GONE);
        }

        previousButton.setVisibility(currentSheetIndex > 0 ? View.VISIBLE : View.GONE);
        if (backToLessonUsed) {
            nextButton.setVisibility(currentSheetIndex < sheetDocuments.size() - 1 ? View.VISIBLE : View.GONE);
        }
    }

    private void saveUserProgress(String userId, double score) {
        DocumentReference userProgressRef = db.collection("userProgress").document(userId);
        Map<String, Object> levelProgress = new HashMap<>();

        if (lessonId.equals("lesson1")) {
            levelProgress.put("level1_completed", true);
            levelProgress.put("level1_score", score);
        } else if (lessonId.equals("lesson2")) {
            levelProgress.put("level2_completed", true);
            levelProgress.put("level2_score", score);
        }

        userProgressRef.set(levelProgress, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Progress", "Level progress updated."))
                .addOnFailureListener(e -> Log.w("Progress", "Error updating level progress.", e));
    }

    private void showQuestions() {
        lessonTextView.setVisibility(View.GONE);
        title.setVisibility(View.GONE);
        lessonImage.setVisibility(View.GONE);
        lessonVideo.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        previousButton.setVisibility(View.GONE);
        backToQuestions.setVisibility(View.GONE);

        questionText.setVisibility(View.VISIBLE);
        answersGroup.setVisibility(View.VISIBLE);
        acceptButton.setVisibility(View.VISIBLE);
        checkButton.setVisibility(View.VISIBLE);
        backToLesson.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        loadQuestionsForLesson(lessonId);
    }
}
