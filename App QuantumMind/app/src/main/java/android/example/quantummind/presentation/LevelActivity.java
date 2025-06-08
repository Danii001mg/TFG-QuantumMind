package android.example.quantummind.presentation;

import android.content.Intent;
import android.example.quantummind.R;
import android.example.quantummind.domain.*;
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
    private TextView questionCounter;
    private TextView questionText;
    private RadioGroup answersGroup;
    private GridLayout questionButtons;
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
    private int attempts = 0;
    private static boolean mentalMarathonUnlocked = false;
    private static Set<String> completedLevelsInSession = new HashSet<>();
    private static long sessionStartTime = 0L;
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;

    private List<Sheet> sheetList = new ArrayList<>();
    private Questionary questionary;
    private Question currentQuestion;
    private List<Answer> userAnswers = new ArrayList<>();
    private Map<Integer, Integer> selectedAnswers = new HashMap<>();
    private Map<Integer, List<Integer>> radioButtonIdsForQuestions = new HashMap<>();
    private Map<Integer, String> questionStatusMap = new HashMap<>();
    private int lastSelectedButtonIndex = -1;

    private LevelController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        long currentTime = System.currentTimeMillis();
        if (sessionStartTime == 0L || (currentTime - sessionStartTime) > SESSION_TIMEOUT) {
            sessionStartTime = currentTime;
            completedLevelsInSession.clear();
            mentalMarathonUnlocked = false;
            Log.d("LevelActivity", "Nueva sesión iniciada");
        }

        controller = new LevelController();

        initUIComponents();

        lessonId = getIntent().getStringExtra("LESSON_ID");
        Log.d("LevelActivity", ">> Received LESSON_ID = " + lessonId);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        checkFirstStepAchievement();

        if (lessonId == null || lessonId.isEmpty()) {
            Toast.makeText(this, "No lesson ID provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setLevelName(lessonId);

        loadSheetsAndQuestions();

        loadUserProgress();

        setupButtonListeners();

        controller.getDb().collection("userProgress")
                .document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w("FirestoreListener", "Listen failed.", e);
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Long curr = documentSnapshot.getLong("attempts");
                        if (curr != null) {
                            attempts = curr.intValue();
                            Log.d("FirestoreListener", "Attempts updated to: " + attempts);
                        }
                    }
                });
    }

    private void initUIComponents() {
        lessonTextView = findViewById(R.id.lessonText);
        titleTextView = findViewById(R.id.title);
        lessonImageView = findViewById(R.id.lessonImage);
        lessonVideoView = findViewById(R.id.lessonVideo);
        nextButton = findViewById(R.id.bottomRightButton);
        previousButton = findViewById(R.id.bottomLeftButton);
        questionText = findViewById(R.id.questionText);
        answersGroup = findViewById(R.id.answersGroup);
        questionCounter = findViewById(R.id.questionCounter);
        questionButtons = findViewById(R.id.questionGrid);
        backToLessonButton = findViewById(R.id.backToLesson);
        backToQuestionsButton = findViewById(R.id.backToQuestions);
        backToMapButton = findViewById(R.id.backToMap);
        progressBarLayout = findViewById(R.id.progressBar);
        transparentBackground = findViewById(R.id.transparent_background);
        gradeText = findViewById(R.id.gradeText);
        timeText = findViewById(R.id.timeText);
    }

    private boolean isActiveSession() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - sessionStartTime) <= SESSION_TIMEOUT;
    }

    private void setLevelName(String lessonId) {
        String levelNameText;
        switch (lessonId) {
            case "lesson1":
                levelNameText = "Quantum Computing Fundamentals";
                break;
            case "lesson2":
                levelNameText = "Quantum Circuits";
                break;
            case "lesson3":
                levelNameText = "Quantum Algorithms I";
                break;
            case "lesson4":
                levelNameText = "Quantum Algorithms II";
                break;
            case "lesson5":
                levelNameText = "Qiskit";
                break;
            case "lesson6":
                levelNameText = "Complex Programs in Qiskit";
                break;
            default:
                levelNameText = "";
                break;
        }
        ((TextView) findViewById(R.id.levelName)).setText(levelNameText);
    }

    private void loadSheetsAndQuestions() {
        controller.fetchSheets(lessonId, new LevelController.SheetsCallback() {
            @Override
            public void onSuccess(List<Sheet> sheets) {
                sheetList = sheets;
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

        controller.fetchQuestions(lessonId, new LevelController.QuestionsCallback() {
            @Override
            public void onSuccess(Questionary qy) {
                questionary = qy;
                totalNumberOfQuestions = questionary.getQuestions().size();
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

        String sheetText = sheet.getText();
        if (sheetText != null) {
            sheetText = sheetText.replace("||", "\n");
        }
        lessonTextView.setText(sheetText);
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
            String frameVideo = "<html><body><iframe width=\"match_parent\" height=\"wrap_content\" "
                    + "src=\"" + videoUrl + "\" frameborder=\"0\" allowfullscreen></iframe></body></html>";
            lessonVideoView.loadData(frameVideo, "text/html", "utf-8");
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
        if (questions == null
                || questions.isEmpty()
                || questionIndex < 0
                || questionIndex >= questions.size())
        {
            Toast.makeText(this, "No questions available for this lesson.", Toast.LENGTH_LONG).show();
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

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        currentQuestion = questions.get(questionIndex);
        questionText.setText(currentQuestion.getQuestionText());

        String imgUrl = currentQuestion.getImage();
        ImageView questionImageView = findViewById(R.id.questionImage);
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
        if (isNew) {
            radioIds = new ArrayList<>();
        }
        int idx = 0;
        for (String option : currentQuestion.getAnswerOptions()) {
            RadioButton rb = new RadioButton(this);
            if (isNew) {
                int newId = View.generateViewId();
                rb.setId(newId);
                radioIds.add(newId);
            } else {
                rb.setId(radioIds.get(idx));
                idx++;
            }
            rb.setText(option);
            answersGroup.addView(rb);
        }
        if (isNew) {
            radioButtonIdsForQuestions.put(questionIndex, radioIds);
        }

        if (selectedAnswers.containsKey(questionIndex)) {
            int savedRbId = selectedAnswers.get(questionIndex);
            RadioButton rb = findViewById(savedRbId);
            if (rb != null) rb.setChecked(true);
        }

        questionCounter.setText((questionIndex + 1) + "/" + totalNumberOfQuestions);

        answersGroup.setOnCheckedChangeListener((group, checkedId) -> {
            try {
                handleAnswerSelection(group, checkedId);
            } catch (Exception e) {
                Log.e("LevelActivity", "Error handling answer selection", e);
            }
        });

        updateButtonColor(questionIndex);

        if (isReviewMode) {
            disableAnswerGroup();
        }
    }

    private void handleAnswerSelection(RadioGroup group, int checkedId) {
        if (checkedId == -1) {
            return;
        }

        int selectedAnswerIndex = group.indexOfChild(findViewById(checkedId));
        selectedAnswers.put(currentQuestionIndex, checkedId);

        String questionId = currentQuestion.getId();
        Answer answer = new Answer(userId, questionId, selectedAnswerIndex);
        if (userAnswers.size() > currentQuestionIndex) {
            userAnswers.set(currentQuestionIndex, answer);
        } else {
            userAnswers.add(answer);
        }

        updateScoreOnTheFly(currentQuestion.getCorrectAnswerIndex(), selectedAnswerIndex);
        questionStatusMap.put(currentQuestionIndex, "answered");
        updateButtonColor(currentQuestionIndex);

        if (currentQuestionIndex == 0 && !firstStepAchievementUnlocked) {
            controller.unlockAchievement(userId,
                    "first_step",
                    "First Step",
                    new LevelController.AchievementCallback() {
                        @Override
                        public void onUnlocked(String achievementName) {
                            firstStepAchievementUnlocked = true;
                            Toast.makeText(LevelActivity.this,
                                    "Achievement acquired!!: " + achievementName,
                                    Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(String errorMessage) {
                            Log.e("LevelActivity", "Error unlocking first step: " + errorMessage);
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
                int selectedIdx = answersGroup.indexOfChild(findViewById(rbId));
                int correctIdx = questionary.getQuestions()
                        .get(questionIndex)
                        .getCorrectAnswerIndex();
                if (selectedIdx == correctIdx) {
                    btn.setBackgroundResource(R.drawable.round_button_correct);
                } else {
                    btn.setBackgroundResource(R.drawable.round_button_incorrect);
                }
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
                Button prevBtn = (Button) questionButtons.getChildAt(lastSelectedButtonIndex);
                String prevStatus = questionStatusMap.get(lastSelectedButtonIndex);
                if ("answered".equals(prevStatus)) {
                    prevBtn.setBackgroundResource(R.drawable.round_button_answered);
                } else if ("skipped".equals(prevStatus)) {
                    prevBtn.setBackgroundResource(R.drawable.round_button_skipped);
                } else {
                    prevBtn.setBackgroundResource(R.drawable.round_button);
                }
            }
            lastSelectedButtonIndex = questionIndex;
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
        if (currentSheetIndex == 0) {
            previousButton.setVisibility(View.GONE);
        }
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

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        loadQuestionIntoView(currentQuestionIndex);

        for (int i = 0; i < questionButtons.getChildCount(); i++) {
            updateButtonColor(i);
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
        List<Question> questions = questionary.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            if (selectedAnswers.containsKey(i)) {
                int rbId = selectedAnswers.get(i);
                int idx = radioButtonIdsForQuestions.get(i).indexOf(rbId);
                if (idx == questions.get(i).getCorrectAnswerIndex()) {
                    correctAnswersCount++;
                }
            }
        }
        percentage = (double) correctAnswersCount / totalNumberOfQuestions * 100;

        long endTime = System.currentTimeMillis();
        long timeTaken = (endTime - startTime) / 1000;

        controller.saveUserAnswers(userAnswers, new LevelController.SaveAnswersCallback() {
            @Override
            public void onSuccess() {
                Log.d("LevelActivity", "All answers saved");
            }
            @Override
            public void onError(String errorMessage) {
                Log.e("LevelActivity", "Error saving user answers: " + errorMessage);
            }
        });

        controller.saveUserProgress(userId, lessonId, percentage, new LevelController.ProgressCallback() {
            @Override
            public void onSuccess() {
                Log.d("LevelActivity", "Progress saved for " + lessonId);
                checkGeniusAchievement();
            }
            @Override
            public void onError(String errorMessage) {
                Log.e("LevelActivity", "Error saving progress: " + errorMessage);
            }
        });

        if (percentage >= 30) {
            if (isActiveSession()) {
                completedLevelsInSession.add(lessonId);
                Log.d("LevelActivity", "Levels completed in session: " + completedLevelsInSession.size());

                if (completedLevelsInSession.size() == 6 && !mentalMarathonUnlocked) {
                    mentalMarathonUnlocked = true;
                    controller.unlockAchievement(userId,
                            "mental_marathon",
                            "Mental Marathon",
                            new LevelController.AchievementCallback() {
                                @Override
                                public void onUnlocked(String achievementName) {
                                    Toast.makeText(LevelActivity.this,
                                            "Achievement acquired!!: " + achievementName,
                                            Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onError(String errorMessage) {
                                    Log.e("LevelActivity", "Error unlocking mental_marathon: " + errorMessage);
                                }
                            });
                }
            } else {
                sessionStartTime = System.currentTimeMillis();
                completedLevelsInSession.clear();
                completedLevelsInSession.add(lessonId);
                mentalMarathonUnlocked = false;
                Log.d("LevelActivity", "Session expired, resetting...");
            }
        }

        checkKnowledgeMasterAchievement();
        if (completedLevelsInSession.size() == 6 && !mentalMarathonUnlocked) {
            mentalMarathonUnlocked = true;
            controller.unlockAchievement(userId,
                    "mental_marathon",
                    "Mental Marathon",
                    new LevelController.AchievementCallback() {
                        @Override
                        public void onUnlocked(String achievementName) {
                            Toast.makeText(LevelActivity.this,
                                    "Achievement acquired!!: " + achievementName,
                                    Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(String errorMessage) {
                            Log.e("LevelActivity", "Error unlocking mental_marathon: " + errorMessage);
                        }
                    });
        }

        blockButtons();

        transparentBackground.setVisibility(View.VISIBLE);
        gradeText.setText(String.format("Your score is %d/10", correctAnswersCount));
        if (correctAnswersCount >= 3) {
            timeText.setText(String.format("You've completed the test in %d seconds", timeTaken));
        } else {
            timeText.setText("You failed the test. Try it again!");
        }
        gradeText.setVisibility(View.VISIBLE);
        timeText.setVisibility(View.VISIBLE);

        if (percentage == 100) {
            controller.unlockAchievement(userId,
                    "question_hunter",
                    "Question Hunter",
                    new LevelController.AchievementCallback() {
                        @Override
                        public void onUnlocked(String achievementName) {
                            Toast.makeText(LevelActivity.this,
                                    "Achievement acquired!!: " + achievementName,
                                    Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(String errorMessage) {
                            Log.e("LevelActivity", "Error unlocking question_hunter: " + errorMessage);
                        }
                    });
        }

        View rootView = findViewById(android.R.id.content);
        Toast.makeText(this, "Touch the screen to go to Review Mode!", Toast.LENGTH_SHORT).show();
        rootView.setOnClickListener(v -> enterReviewMode());
    }

    private int findFirstUnansweredQuestion() {
        for (int i = 0; i < questionary.getQuestions().size(); i++) {
            String status = questionStatusMap.get(i);
            if ("skipped".equals(status)) {
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
            if (child instanceof RadioButton) {
                child.setEnabled(false);
            }
        }
    }

    private void enterReviewMode() {
        isReviewMode = true;

        backToLessonButton.setVisibility(View.GONE);
        backToMapButton.setVisibility(View.VISIBLE);
        transparentBackground.setVisibility(View.GONE);
        questionText.setVisibility(View.VISIBLE);
        answersGroup.setVisibility(View.VISIBLE);
        backToLessonButton.setVisibility(View.VISIBLE);
        progressBarLayout.setVisibility(View.VISIBLE);
        questionButtons.setVisibility(View.VISIBLE);

        enableQuestionButtonsInReview();

        for (int i = 0; i < totalNumberOfQuestions; i++) {
            loadQuestionIntoView(i);
            disableAnswerGroup();
        }

        currentQuestionIndex = 0;
        loadQuestionIntoView(currentQuestionIndex);

        backToMapButton.setOnClickListener(v -> {
            Intent intent = new Intent(LevelActivity.this, MainActivity.class);
            switch (lessonId) {
                case "lesson1":
                    intent.putExtra("LEVEL1_SCORE", percentage);
                    break;
                case "lesson2":
                    intent.putExtra("LEVEL2_SCORE", percentage);
                    break;
                case "lesson3":
                    intent.putExtra("LEVEL3_SCORE", percentage);
                    break;
                case "lesson4":
                    intent.putExtra("LEVEL4_SCORE", percentage);
                    break;
                case "lesson5":
                    intent.putExtra("LEVEL5_SCORE", percentage);
                    break;
                case "lesson6":
                    intent.putExtra("LEVEL6_SCORE", percentage);
                    break;
            }
            startActivity(intent);
            finish();
        });
    }

    private void enableQuestionButtonsInReview() {
        for (int i = 0; i < questionButtons.getChildCount(); i++) {
            int idx = i;
            Button btn = (Button) questionButtons.getChildAt(i);
            btn.setEnabled(true);
            btn.setOnClickListener(v -> loadQuestionIntoView(idx));
        }
    }

    private void disableAnswerGroup() {
        answersGroup.setOnCheckedChangeListener(null);
        for (int i = 0; i < answersGroup.getChildCount(); i++) {
            View child = answersGroup.getChildAt(i);
            if (child instanceof RadioButton) {
                child.setEnabled(false);
            }
        }
    }

    private void loadUserProgress() {
        controller.getDb().collection("userProgress")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long curr = documentSnapshot.getLong("attempts");
                        if (curr != null) attempts = curr.intValue();
                    } else {
                        attempts = 0;
                    }
                })
                .addOnFailureListener(e -> Log.e("LevelActivity", "Error loading attempts", e));
    }

    private void checkKnowledgeMasterAchievement() {
        controller.getDb().collection("userProgress")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean all = true;
                        for (int i = 1; i <= 6; i++) {
                            Boolean lvlC = documentSnapshot.getBoolean("level" + i + "_completed");
                            if (lvlC == null || !lvlC) {
                                all = false;
                                break;
                            }
                        }
                        if (all) {
                            controller.unlockAchievement(userId,
                                    "knowledge_master",
                                    "Knowledge Master",
                                    new LevelController.AchievementCallback() {
                                        @Override
                                        public void onUnlocked(String achievementName) {
                                            Toast.makeText(LevelActivity.this,
                                                    "Achievement acquired!!: " + achievementName,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        @Override
                                        public void onError(String errorMessage) {
                                            Log.e("LevelActivity", "Error unlocking knowledge_master: " + errorMessage);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("LevelActivity", "Error checking knowledge master", e));
    }

    private void checkGeniusAchievement() {
        controller.getDb().collection("userProgress")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean allHigh = true;
                        for (int i = 1; i <= 6; i++) {
                            Double sc = documentSnapshot.getDouble("level" + i + "_score");
                            if (sc == null || sc < 80.0) {
                                allHigh = false;
                                break;
                            }
                        }
                        if (allHigh) {
                            controller.unlockAchievement(userId,
                                    "genius",
                                    "Genius",
                                    new LevelController.AchievementCallback() {
                                        @Override
                                        public void onUnlocked(String achievementName) {
                                            Toast.makeText(LevelActivity.this,
                                                    "Achievement acquired!!: " + achievementName,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        @Override
                                        public void onError(String errorMessage) {
                                            Log.e("LevelActivity", "Error unlocking genius: " + errorMessage);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("LevelActivity", "Error checking genius achievement", e));
    }

    private void checkFirstStepAchievement() {
        controller.getDb().collection("userAchievements")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean firstUnlocked = documentSnapshot.getBoolean("first_step");
                        if (firstUnlocked != null && firstUnlocked) {
                            firstStepAchievementUnlocked = true;
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("LevelActivity", "Error checking first_step achievement", e));
    }

    private void openZoomActivity(String imageUrl) {
        Intent i = new Intent(LevelActivity.this, ImageZoomActivity.class);
        i.putExtra("imageUrl", imageUrl);
        startActivity(i);
    }

    private void setupButtonListeners() {
        nextButton.setOnClickListener(v -> {
            try {
                handleNextSheet();
            } catch (Exception e) {
                Log.e("LevelActivity", "Error handling next button", e);
            }
        });
        previousButton.setOnClickListener(v -> {
            try {
                handlePreviousSheet();
            } catch (Exception e) {
                Log.e("LevelActivity", "Error handling previous button", e);
            }
        });

        backToLessonButton.setOnClickListener(v -> {
            backToLessonUsed = true;
            questionText.setVisibility(View.GONE);
            answersGroup.setVisibility(View.GONE);
            backToLessonButton.setVisibility(View.GONE);
            progressBarLayout.setVisibility(View.GONE);
            questionButtons.setVisibility(View.GONE);
            showSheet(sheetList.get(currentSheetIndex));
            backToQuestionsButton.setVisibility(View.VISIBLE);
        });
        backToQuestionsButton.setOnClickListener(v -> {
            showQuestions();
            backToQuestionsButton.setVisibility(View.GONE);
        });

        answersGroup.setOnCheckedChangeListener((group, checkedId) -> {
            try {
                handleAnswerSelection(group, checkedId);
            } catch (Exception e) {
                Log.e("LevelActivity", "Error handling answer selection", e);
            }
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
}
