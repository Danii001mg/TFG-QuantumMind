package android.example.quantummind.presentation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.example.quantummind.domain.LoginController;
import android.example.quantummind.R;
import android.example.quantummind.domain.User;
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

    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordText;

    private LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText       = findViewById(R.id.emailEditText);
        passwordEditText    = findViewById(R.id.passwordEditText);
        rememberMeCheckBox  = findViewById(R.id.rememberMeCheckBox);
        forgotPasswordText  = findViewById(R.id.forgotPasswordText);

        loginController = new LoginController();

        loadCredentialsIfNeeded();

        forgotPasswordText.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this,
                android.R.style.Theme_Material_Dialog_Alert
        );
        builder.setTitle("Type your email");

        final EditText input = new EditText(this);
        input.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );
        builder.setView(input);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                FirebaseAuth.getInstance()
                        .sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(
                                        LoginActivity.this,
                                        "An email has been sent to reset your password.",
                                        Toast.LENGTH_LONG
                                ).show();
                            } else {
                                Toast.makeText(
                                        LoginActivity.this,
                                        "Error sending email.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
            } else {
                Toast.makeText(
                        this,
                        "You must send a valid email.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadCredentialsIfNeeded() {
        SharedPreferences prefsCheckbox = getSharedPreferences("checkbox", MODE_PRIVATE);
        boolean rememberCredentials = prefsCheckbox.getBoolean("rememberCredentials", false);

        if (rememberCredentials) {
            SharedPreferences creds = getSharedPreferences("credentials", MODE_PRIVATE);
            String email = creds.getString("email", "");
            String password = creds.getString("password", "");

            emailEditText.setText(email);
            passwordEditText.setText(password);
            rememberMeCheckBox.setChecked(true);
        }
    }


    public void login(View view) {
        String email    = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "You must enter an email and a password.", Toast.LENGTH_SHORT).show();
            return;
        }

        loginController.login(email, password, new LoginController.Callback() {
            @Override
            public void onSuccess(User user) {
                if (rememberMeCheckBox.isChecked()) {
                    saveCredentials(email, password);
                } else {
                    clearCredentials();
                }
                startMainActivity();
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences creds = getSharedPreferences("credentials", MODE_PRIVATE);
        SharedPreferences.Editor editorCreds = creds.edit();
        editorCreds.putString("email", email);
        editorCreds.putString("password", password);
        editorCreds.apply();

        SharedPreferences prefsCheckbox = getSharedPreferences("checkbox", MODE_PRIVATE);
        SharedPreferences.Editor editorBox = prefsCheckbox.edit();
        editorBox.putBoolean("rememberCredentials", true);
        editorBox.apply();
    }

    private void clearCredentials() {
        SharedPreferences creds = getSharedPreferences("credentials", MODE_PRIVATE);
        creds.edit().clear().apply();

        SharedPreferences prefsCheckbox = getSharedPreferences("checkbox", MODE_PRIVATE);
        prefsCheckbox.edit().putBoolean("rememberCredentials", false).apply();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void openRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
