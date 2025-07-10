package android.example.quantummind.presentation;

import android.content.Intent;
import android.example.quantummind.R;
import android.example.quantummind.domain.*;
import android.example.quantummind.domain.callbacks.AchievementCallback;
import android.example.quantummind.domain.callbacks.QuestionCallback;
import android.example.quantummind.domain.callbacks.SheetsCallback;
import android.example.quantummind.domain.controllers.LessonController;
import android.example.quantummind.domain.entities.Answer;
import android.example.quantummind.domain.entities.Question;
import android.example.quantummind.domain.entities.Questionary;
import android.example.quantummind.domain.entities.Sheet;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.*;

public class LevelActivity extends AppCompatActivity {

    private TextView lessonTextView, titleTextView;
    private ImageView lessonImageView;
    private WebView lessonVideoView;
    private ImageView nextButton, previousButton;
    private TextView questionCounter, questionText;
    private RadioGroup answersGroup;
    private GridLayout questionButtons;
    private ImageView questionImageView;

    private Button backToLessonButton, backToQuestionsButton, backToMapButton;
    private LinearLayout progressBarLayout;
    private FrameLayout transparentBackground;
    private TextView gradeText, timeText;

    private String lessonId;
    private String userId;
    private int currentSheetIndex = 0;
    private int currentQuestionIndex = 0;
    private int totalNumberOfQuestions = 0;
    private int correctAnswersCount = 0;
    private long startTime = 0L;
    private boolean firstStepAchievementUnlocked = false;
    private boolean isReviewMode = false;
    private boolean backToLessonUsed = false;
    private double percentage = 0.0;

    private final List<Sheet> sheetList = new ArrayList<>();
    private Questionary questionary;
    private Question currentQuestion;
    private final List<Answer> userAnswers = new ArrayList<>();
    private final Map<Integer, Integer> selectedAnswers = new HashMap<>();
    private final Map<Integer, List<Integer>> radioButtonIdsForQuestions = new HashMap<>();
    private final Map<Integer, String> questionStatusMap = new HashMap<>();
    private int lastSelectedButtonIndex = -1;
    private static long sessionStartTime = 0L;
    private static final long SESSION_TIMEOUT = 30*60_000; // 30 min
    private static final Set<String> completedThisSession = new HashSet<>();


    private LessonController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        long now = System.currentTimeMillis();
        if (sessionStartTime == 0L || now - sessionStartTime > SESSION_TIMEOUT) {
            sessionStartTime = now;
            completedThisSession.clear();
        }


        controller = new LessonController(this);

        initUIComponents();

        lessonId = getIntent().getStringExtra("LESSON_ID");
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (lessonId == null || lessonId.isEmpty()) {
            Toast.makeText(this, "No lesson ID provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setLevelName(lessonId);
        loadSheetsAndQuestions();
        setupButtonListeners();
    }

    private void initUIComponents() {
        lessonTextView        = findViewById(R.id.lessonText);
        titleTextView         = findViewById(R.id.title);
        lessonImageView       = findViewById(R.id.lessonImage);
        lessonVideoView       = findViewById(R.id.lessonVideo);
        nextButton            = findViewById(R.id.bottomRightButton);
        previousButton        = findViewById(R.id.bottomLeftButton);
        questionText          = findViewById(R.id.questionText);
        questionImageView = findViewById(R.id.questionImage);
        answersGroup          = findViewById(R.id.answersGroup);
        questionCounter       = findViewById(R.id.questionCounter);
        questionButtons       = findViewById(R.id.questionGrid);
        backToLessonButton    = findViewById(R.id.backToLesson);
        backToQuestionsButton = findViewById(R.id.backToQuestions);
        backToMapButton       = findViewById(R.id.backToMap);
        progressBarLayout     = findViewById(R.id.progressBar);
        transparentBackground = findViewById(R.id.transparent_background);
        gradeText             = findViewById(R.id.gradeText);
        timeText              = findViewById(R.id.timeText);
    }

    private void setLevelName(String lessonId) {
        String levelNameText;
        switch (lessonId) {
            case "lesson1": levelNameText = "Quantum Computing Fundamentals"; break;
            case "lesson2": levelNameText = "Quantum Circuits"; break;
            case "lesson3": levelNameText = "Quantum Algorithms I"; break;
            case "lesson4": levelNameText = "Quantum Algorithms II"; break;
            case "lesson5": levelNameText = "Qiskit"; break;
            case "lesson6": levelNameText = "Complex Programs in Qiskit"; break;
            default: levelNameText = ""; break;
        }
        ((TextView) findViewById(R.id.levelName)).setText(levelNameText);
    }

    private void loadSheetsAndQuestions() {
        controller.fetchSheets(lessonId, new SheetsCallback() {
            @Override
            public void onSuccess(List<Sheet> sheets) {
                sheetList.clear();
                sheetList.addAll(sheets);
                if (!sheetList.isEmpty()) {
                    showSheet(sheetList.get(0));
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(LevelActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                finish();
            }
        });

        controller.fetchQuestions(lessonId, new QuestionCallback() {
            @Override
            public void onSuccess(Questionary qy) {
                questionary = qy;
                totalNumberOfQuestions = qy.getQuestions().size();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(LevelActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }


    private void showSheet(Sheet sheet) {
        lessonTextView.setVisibility(View.VISIBLE);
        titleTextView.setVisibility(View.VISIBLE);

        String text = sheet.getText();
        if (text != null) text = text.replace("||", "\n");
        lessonTextView.setText(text);
        titleTextView.setText(sheet.getTitle());

        String imageUrl = sheet.getImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            lessonImageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUrl).into(lessonImageView);
            lessonImageView.setOnClickListener(v -> openZoomActivity(imageUrl));
        } else {
            lessonImageView.setVisibility(View.GONE);
        }

        String videoUrl = sheet.getVideo();
        if (videoUrl != null && !videoUrl.isEmpty()) {
            lessonVideoView.setVisibility(View.VISIBLE);
            lessonVideoView.getSettings().setJavaScriptEnabled(true);
            String frame = "<html><body><iframe width=\"match_parent\" height=\"wrap_content\""
                    + " src=\"" + videoUrl + "\" frameborder=\"0\" allowfullscreen></iframe></body></html>";
            lessonVideoView.loadData(frame, "text/html", "utf-8");
        } else {
            lessonVideoView.setVisibility(View.GONE);
        }

        previousButton.setVisibility(currentSheetIndex > 0 ? View.VISIBLE : View.GONE);
        if (backToLessonUsed) {
            nextButton.setVisibility(currentSheetIndex < sheetList.size() - 1 ? View.VISIBLE : View.GONE);
        }
    }

    private void loadQuestionIntoView(int questionIndex) {
        List<Question> questions = questionary.getQuestions();
        if (questions == null || questions.isEmpty()
                || questionIndex < 0 || questionIndex >= questions.size()) {
            Toast.makeText(this, "No questions available.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        lessonTextView.setVisibility(View.GONE);
        titleTextView.setVisibility(View.GONE);
        lessonImageView.setVisibility(View.GONE);
        lessonVideoView.setVisibility(View.GONE);
        backToQuestionsButton.setVisibility(View.GONE);
        previousButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);

        questionText.setVisibility(View.VISIBLE);
        answersGroup.setVisibility(View.VISIBLE);
        backToLessonButton.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.VISIBLE);
        questionButtons.setVisibility(View.VISIBLE);

        if (startTime == 0L) startTime = System.currentTimeMillis();

        currentQuestion = questions.get(questionIndex);
        questionText.setText(currentQuestion.getQuestionText());

        String imgUrl = currentQuestion.getImage();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            questionImageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(imgUrl).into(questionImageView);
        } else {
            questionImageView.setVisibility(View.GONE);
        }

        answersGroup.setOnCheckedChangeListener(null);
        answersGroup.removeAllViews();
        List<Integer> radioIds = radioButtonIdsForQuestions.get(questionIndex);
        boolean isNew = (radioIds == null);
        if (isNew) radioIds = new ArrayList<>();
        int idx = 0;
        for (String option : currentQuestion.getAnswerOptions()) {
            RadioButton rb = new RadioButton(this);
            if (isNew) {
                int newId = View.generateViewId();
                rb.setId(newId);
                radioIds.add(newId);
            } else {
                rb.setId(radioIds.get(idx++));
            }
            rb.setText(option);
            answersGroup.addView(rb);
        }
        if (isNew) radioButtonIdsForQuestions.put(questionIndex, radioIds);

        if (selectedAnswers.containsKey(questionIndex)) {
            int savedId = selectedAnswers.get(questionIndex);
            RadioButton rb = findViewById(savedId);
            if (rb != null) rb.setChecked(true);
        }

        questionCounter.setText((questionIndex + 1) + "/" + totalNumberOfQuestions);

        answersGroup.setOnCheckedChangeListener((group, checkedId) -> {
            try {
                handleAnswerSelection(group, checkedId);
            } catch (Exception e) {
                Log.e("LevelActivity", "Error selecting answer", e);
            }
        });

        updateButtonColor(questionIndex);

        if (isReviewMode) {
            disableAnswerGroup();
        }
    }

    private void handleAnswerSelection(RadioGroup group, int checkedId) {
        if (checkedId == -1) return;

        int selectedIdx = group.indexOfChild(findViewById(checkedId));
        selectedAnswers.put(currentQuestionIndex, checkedId);

        Answer answer = new Answer(userId, currentQuestion.getId(), selectedIdx);
        if (userAnswers.size() > currentQuestionIndex) {
            userAnswers.set(currentQuestionIndex, answer);
        } else {
            userAnswers.add(answer);
        }

        updateScoreOnTheFly(currentQuestion.getCorrectAnswerIndex(), selectedIdx);
        questionStatusMap.put(currentQuestionIndex, "answered");
        updateButtonColor(currentQuestionIndex);

        if (currentQuestionIndex == 0 && !firstStepAchievementUnlocked) {
            controller.unlockAchievement(userId,
                    "first_step",
                    "First Step",
                    new AchievementCallback() {
                        @Override public void onUnlocked(String achievementName) {
                            firstStepAchievementUnlocked = true;
                            Toast.makeText(LevelActivity.this,
                                    "Achievement acquired!!: " + achievementName,
                                    Toast.LENGTH_SHORT).show();
                        }

                        public void onSuccess(AchievementItem[] achievements) {
                        }

                        @Override public void onError(String errorMessage) {
                            Log.e("LevelActivity", "Error unlocking first_step: " + errorMessage);
                        }
                    });
        }

        if (selectedAnswers.size() >= totalNumberOfQuestions) {
            finishLevel();
            return;
        }

        group.postDelayed(() -> {
            currentQuestionIndex++;
            if (currentQuestionIndex < questionary.getQuestions().size()) {
                loadQuestionIntoView(currentQuestionIndex);
            } else {
                finishLevel();
            }
            group.clearCheck();
        }, 1000);
    }

    private void updateScoreOnTheFly(int correctAnswerIndex, int newAnswerIndex) {
        if (correctAnswerIndex == newAnswerIndex) {
            correctAnswersCount++;
        }
    }

    private void updateButtonColor(int questionIndex) {
        Button btn = (Button) questionButtons.getChildAt(questionIndex);

        if (isReviewMode) {
            if (selectedAnswers.containsKey(questionIndex)) {
                int rbId = selectedAnswers.get(questionIndex);
                int selIdx = answersGroup.indexOfChild(findViewById(rbId));
                int corrIdx = questionary.getQuestions()
                        .get(questionIndex)
                        .getCorrectAnswerIndex();
                btn.setBackgroundResource(selIdx == corrIdx
                        ? R.drawable.round_button_correct
                        : R.drawable.round_button_incorrect);
            }
        } else {
            String status = questionStatusMap.get(questionIndex);
            if ("answered".equals(status)) {
                btn.setBackgroundResource(R.drawable.round_button_answered_selected);
            } else if ("skipped".equals(status)) {
                btn.setBackgroundResource(R.drawable.round_button_skipped_selected);
            } else {
                btn.setBackgroundResource(R.drawable.round_button_selected);
            }
            if (lastSelectedButtonIndex != -1 && lastSelectedButtonIndex != questionIndex) {
                Button prev = (Button) questionButtons.getChildAt(lastSelectedButtonIndex);
                String prevStatus = questionStatusMap.get(lastSelectedButtonIndex);
                prev.setBackgroundResource(
                        "answered".equals(prevStatus) ? R.drawable.round_button_answered :
                                "skipped".equals(prevStatus)  ? R.drawable.round_button_skipped :
                                        R.drawable.round_button
                );
            }
            lastSelectedButtonIndex = questionIndex;
        }
    }

    private void finishLevel() {
        int firstUnanswered = findFirstUnansweredQuestion();
        if (firstUnanswered != -1) {
            currentQuestionIndex = firstUnanswered;
            loadQuestionIntoView(firstUnanswered);
            Toast.makeText(this, "You must answer all the questions!", Toast.LENGTH_SHORT).show();
            return;
        }

        correctAnswersCount = 0;
        for (int i = 0; i < questionary.getQuestions().size(); i++) {
            if (selectedAnswers.containsKey(i)) {
                int rbId = selectedAnswers.get(i);
                int idx = radioButtonIdsForQuestions.get(i).indexOf(rbId);
                if (idx == questionary.getQuestions().get(i).getCorrectAnswerIndex()) {
                    correctAnswersCount++;
                }
            }
        }
        percentage = (double) correctAnswersCount / totalNumberOfQuestions * 100;
        long timeTaken = (System.currentTimeMillis() - startTime) / 1000;

        if (timeTaken <= 60) {
            controller.unlockAchievement(
                    userId,
                    "fast_and_furious",
                    "Fast and Furious",
                    new AchievementCallback() {
                        @Override
                        public void onUnlocked(String name) {
                            Toast.makeText(LevelActivity.this,
                                    "Achievement acquired: " + name,
                                    Toast.LENGTH_SHORT).show();
                        }

                        public void onSuccess(AchievementItem[] achievements) {
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("LevelActivity","Error unlocking fast_and_furious: "+error);
                        }
                    });
        }


        controller.saveUserAnswers(userAnswers, new LessonController.SaveAnswersCallback() {
            @Override public void onSuccess() { Log.d("LevelActivity","Answers saved"); }
            @Override public void onError(String errorMessage) { Log.e("LevelActivity", errorMessage); }
        });
        controller.saveUserProgress(userId, lessonId, percentage, new LessonController.ProgressCallback() {
            @Override public void onSuccess() { Log.d("LevelActivity","Progress saved"); }
            @Override public void onError(String errorMessage) { Log.e("LevelActivity", errorMessage); }
        });

        if (percentage >= 30.0) {
            completedThisSession.add(lessonId);
            if (completedThisSession.size() == 6) {
                controller.unlockAchievement(
                        userId,
                        "mental_marathon",
                        "Mental Marathon",
                        new AchievementCallback() {
                            @Override
                            public void onUnlocked(String achievementName) {
                                Toast.makeText(LevelActivity.this,
                                                "Achievement acquired: " + achievementName,
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }

                            public void onSuccess(AchievementItem[] achievements) {
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e("LevelActivity",
                                        "Error unlocking mental_marathon: " + errorMessage);
                            }
                        }
                );
            }
        }


        blockButtons();
        transparentBackground.setVisibility(View.VISIBLE);
        gradeText.setText(String.format("Your score is %d/10", correctAnswersCount));
        timeText.setText(correctAnswersCount >= 3
                ? String.format("You've completed the test in %d seconds", timeTaken)
                : "You failed the test. Try it again!");
        gradeText.setVisibility(View.VISIBLE);
        timeText.setVisibility(View.VISIBLE);


        if (percentage == 100) {
            controller.unlockAchievement(userId,
                    "question_hunter",
                    "Question Hunter",
                    new AchievementCallback() {
                        @Override public void onUnlocked(String achievementName) {
                            Toast.makeText(LevelActivity.this,
                                    "Achievement acquired!!: " + achievementName,
                                    Toast.LENGTH_SHORT).show();
                        }

                        public void onSuccess(AchievementItem[] achievements) {
                        }

                        @Override public void onError(String errorMessage) {
                            Log.e("LevelActivity","Error unlocking question_hunter: " + errorMessage);
                        }
                    });
        }

        findViewById(android.R.id.content).setOnClickListener(v -> enterReviewMode());
    }

    private int findFirstUnansweredQuestion() {
        for (int i = 0; i < questionary.getQuestions().size(); i++) {
            if ("skipped".equals(questionStatusMap.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private void blockButtons() {
        for (int i = 0; i < questionButtons.getChildCount(); i++) {
            questionButtons.getChildAt(i).setEnabled(false);
        }
        backToLessonButton.setEnabled(false);
        nextButton.setEnabled(false);
        previousButton.setEnabled(false);
        answersGroup.setEnabled(false);
        for (int i = 0; i < answersGroup.getChildCount(); i++) {
            View child = answersGroup.getChildAt(i);
            if (child instanceof RadioButton) child.setEnabled(false);
        }
    }

    private void enterReviewMode() {
        isReviewMode = true;
        backToLessonButton.setVisibility(View.GONE);
        gradeText.setVisibility(View.GONE);
        timeText.setVisibility(View.GONE);
        backToMapButton.setVisibility(View.VISIBLE);
        transparentBackground.setVisibility(View.GONE);

        for (int i = 0; i < questionButtons.getChildCount(); i++) {
            final int idx = i;
            Button btn = (Button) questionButtons.getChildAt(i);
            btn.setEnabled(true);
            btn.setOnClickListener(v -> loadQuestionIntoView(idx));
        }
        for (int i = 0; i < totalNumberOfQuestions; i++) {
            loadQuestionIntoView(i);
            disableAnswerGroup();
        }
        currentQuestionIndex = 0;
        loadQuestionIntoView(0);

        backToMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(LevelActivity.this, MainActivity.class);
            intent.putExtra("LESSON_RETURN", lessonId);
            startActivity(intent);
            finish();
        });
    }

    private void disableAnswerGroup() {
        answersGroup.setOnCheckedChangeListener(null);
        for (int i = 0; i < answersGroup.getChildCount(); i++) {
            View child = answersGroup.getChildAt(i);
            if (child instanceof RadioButton) child.setEnabled(false);
        }
    }

    private void openZoomActivity(String imageUrl) {
        Intent i = new Intent(this, ImageZoomActivity.class);
        i.putExtra("imageUrl", imageUrl);
        startActivity(i);
    }

    private void setupButtonListeners() {
        nextButton.setOnClickListener(v -> {
            try { handleNextSheet(); }
            catch (Exception e) { Log.e("LevelActivity", e.getMessage(), e); }
        });
        previousButton.setOnClickListener(v -> {
            try { handlePreviousSheet(); }
            catch (Exception e) { Log.e("LevelActivity", e.getMessage(), e); }
        });
        backToLessonButton.setOnClickListener(v -> {
            backToLessonUsed = true;
            questionText.setVisibility(View.GONE);
            answersGroup.setVisibility(View.GONE);
            backToLessonButton.setVisibility(View.GONE);
            progressBarLayout.setVisibility(View.GONE);
            questionButtons.setVisibility(View.GONE);
            questionImageView.setVisibility(View.GONE);
            showSheet(sheetList.get(currentSheetIndex));
            backToQuestionsButton.setVisibility(View.VISIBLE);
        });
        backToQuestionsButton.setOnClickListener(v -> {
            showQuestions();
            backToQuestionsButton.setVisibility(View.GONE);
        });

        for (int i = 0; i < questionButtons.getChildCount(); i++) {
            final int idx = i;
            View btn = questionButtons.getChildAt(i);
            btn.setOnClickListener(v -> {
                String status = questionStatusMap.get(currentQuestionIndex);
                if (!"answered".equals(status) && answersGroup.getCheckedRadioButtonId() == -1) {
                    questionStatusMap.put(currentQuestionIndex, "skipped");
                    updateButtonColor(currentQuestionIndex);
                }
                currentQuestionIndex = idx;
                loadQuestionIntoView(idx);
            });
        }
    }

    private void handleNextSheet() {
        if (currentSheetIndex < sheetList.size() - 1) {
            currentSheetIndex++;
            showSheet(sheetList.get(currentSheetIndex));
            previousButton.setVisibility(View.VISIBLE);
        } else {
            loadQuestionIntoView(currentQuestionIndex);
        }
    }

    private void handlePreviousSheet() {
        if (currentSheetIndex > 0) {
            currentSheetIndex--;
            showSheet(sheetList.get(currentSheetIndex));
        }
        previousButton.setVisibility(currentSheetIndex > 0 ? View.VISIBLE : View.GONE);
        nextButton.setVisibility(View.VISIBLE);
    }

    private void showQuestions() {
        lessonTextView.setVisibility(View.GONE);
        titleTextView.setVisibility(View.GONE);
        lessonImageView.setVisibility(View.GONE);
        lessonVideoView.setVisibility(View.GONE);
        backToQuestionsButton.setVisibility(View.GONE);
        previousButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);

        questionText.setVisibility(View.VISIBLE);
        answersGroup.setVisibility(View.VISIBLE);
        backToLessonButton.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.VISIBLE);
        questionButtons.setVisibility(View.VISIBLE);

        if (startTime == 0L) startTime = System.currentTimeMillis();
        loadQuestionIntoView(currentQuestionIndex);
        for (int i = 0; i < questionButtons.getChildCount(); i++) {
            updateButtonColor(i);
        }
    }
}
