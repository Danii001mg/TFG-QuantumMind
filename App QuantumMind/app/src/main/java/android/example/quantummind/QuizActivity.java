package android.example.quantummind;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private List<Question> questionBank;
    private List<Question> questionsDisplayed = new ArrayList<>();
    private Question currentQuestion;
    private int score;
    private Handler questionHandler = new Handler();
    private Runnable questionRunnable;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Usuario no autenticado, redirige a LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_quiz);
        score = 0;
        initializeQuestions();
        displayNextQuestion();
        retrieveUserProgress();
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (questionRunnable != null) {
            questionHandler.removeCallbacks(questionRunnable);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("score", score);
        outState.putParcelableArrayList("questionBank", new ArrayList<>(questionBank));
        outState.putParcelableArrayList("questionsDisplayed", new ArrayList<>(questionsDisplayed));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        score = savedInstanceState.getInt("score");
        questionBank = savedInstanceState.getParcelableArrayList("questionBank");
        questionsDisplayed = savedInstanceState.getParcelableArrayList("questionsDisplayed");
        displayNextQuestion();
    }


    private void initializeQuestions() {
        questionBank = new ArrayList<>();
        questionBank.add(new Question("¿Sobre qué trata este TFG?", Arrays.asList("Lenguaje cuántico", "Programación en Python", "Punteros", "Arduino"), "Lenguaje cuántico"));
        questionBank.add(new Question("¿En que lenguaje se programa este TFG?", Arrays.asList("Python", "Java","C++", "JavaScript"), "Java"));
        questionBank.add(new Question("¿En que IDE estamos trabajando?", Arrays.asList("Eclipse", "VS Code", "XCode", "Android Studio"), "Android Studio"));
}
    private void displayNextQuestion() {
        if (!questionBank.isEmpty()) {
            int randomIndex = new Random().nextInt(questionBank.size());
            currentQuestion = questionBank.remove(randomIndex); // Elimina la pregunta seleccionada
            questionsDisplayed.add(currentQuestion); // Añade a la lista de preguntas mostradas

            displayQuestion(currentQuestion);
        } else {
            showScoreAndReset();
        }
    }

    private void displayQuestion(Question question) {
        TextView questionTextView = findViewById(R.id.questionTextView);
        questionTextView.setText(question.getQuestionText());

        List<String> choices = question.getChoices();
        Button answerButton1 = findViewById(R.id.answerButton1);
        answerButton1.setText(choices.get(0));
        Button answerButton2 = findViewById(R.id.answerButton2);
        answerButton2.setText(choices.get(1));
        Button answerButton3 = findViewById(R.id.answerButton3);
        answerButton3.setText(choices.get(2));
        Button answerButton4 = findViewById(R.id.answerButton4);
        answerButton4.setText(choices.get(3));

    }

    private void setButtonsEnabled(boolean enabled) {
        findViewById(R.id.answerButton1).setEnabled(enabled);
        findViewById(R.id.answerButton2).setEnabled(enabled);
        findViewById(R.id.answerButton3).setEnabled(enabled);
        findViewById(R.id.answerButton4).setEnabled(enabled);
    }

    private void showScoreAndReset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Partida Finalizada");
        builder.setMessage("Has acertado " + score + " de " + questionsDisplayed.size() + " preguntas.");
        builder.setPositiveButton("Jugar de nuevo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetGame();
            }
        });
        builder.setNegativeButton("Volver al inicio", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Finaliza QuizActivity y vuelve a MainActivity
            }
        });
        builder.show();
    }

    private void resetGame() {
        questionBank.addAll(questionsDisplayed);
        questionsDisplayed.clear();
        score = 0;
        displayNextQuestion();
    }

    public void questionAnsweredCorrectly(Question question) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userProgressRef = db.collection("userProgress").document(userId);

        Map<String, Object> questionData = new HashMap<>();
        questionData.put("questionId", question.getId()); // Asegúrate de que tu clase Question tenga un ID único
        questionData.put("answeredCorrectly", true);
        questionData.put("timestamp", FieldValue.serverTimestamp());

        userProgressRef.collection("answeredQuestions").add(questionData)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "DocumentSnapshot written with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding document", e));
    }

    public void retrieveUserProgress() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("userProgress").document(userId).collection("answeredQuestions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("Firestore", document.getId() + " => " + document.getData());
                            // Aquí puedes actualizar la UI con el progreso del usuario o realizar otras acciones
                        }
                    } else {
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }


    public void checkAnswer(String selectedAnswer) {
        setButtonsEnabled(false);
        questionHandler.removeCallbacks(questionRunnable);

        if (currentQuestion.getCorrectAnswer().equals(selectedAnswer)) {
            score++;
            Toast.makeText(this, "¡Respuesta correcta!", Toast.LENGTH_SHORT).show();
            questionAnsweredCorrectly(currentQuestion);
        } else {
            Toast.makeText(this, "Respuesta incorrecta.", Toast.LENGTH_SHORT).show();
        }

        questionRunnable = new Runnable() {
            @Override
            public void run() {
                displayNextQuestion();
                setButtonsEnabled(true);
            }
        };

        questionHandler.postDelayed(questionRunnable, 2000); // 2 segundos
    }




    public void onAnswerButtonClick(View view) {
        Button button = (Button) view;
        String selectedAnswer = button.getText().toString();
        checkAnswer(selectedAnswer);
    }

}
