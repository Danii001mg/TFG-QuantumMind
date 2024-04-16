package android.example.quantummind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        checkManualLoginRequired();

        forgotPasswordText.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Introduce your email");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString();
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "An email has been sent to reset your password.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error while sending the reset email.", Toast.LENGTH_LONG).show();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void checkManualLoginRequired() {
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        boolean manualLoginRequired = preferences.getBoolean("manualLoginRequired", false);

        if (!manualLoginRequired) {
            loadCredentials();
        } else {
            clearCredentials();
        }
    }

    private void loadCredentials() {
        SharedPreferences preferences = getSharedPreferences("credentials", MODE_PRIVATE);
        String email = preferences.getString("email", "");
        String password = preferences.getString("password", "");
        boolean isChecked = preferences.getBoolean("remember", false);

        emailEditText.setText(email);
        passwordEditText.setText(password);
        rememberMeCheckBox.setChecked(isChecked);
    }

    public void login(View view) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (rememberMeCheckBox.isChecked()) {
            saveCredentials(email, password);
        } else {
            clearCredentials();
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("manualLoginRequired", false);
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        loginFailed();
                    }
                });
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences preferences = getSharedPreferences("credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    private void clearCredentials() {
        SharedPreferences preferences = getSharedPreferences("credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    public void openRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void loginFailed() {
        Toast.makeText(this, "Username or password incorrect", Toast.LENGTH_LONG).show();
    }
}
