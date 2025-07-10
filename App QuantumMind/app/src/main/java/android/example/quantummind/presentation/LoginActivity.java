package android.example.quantummind.presentation;

import android.content.Intent;
import android.example.quantummind.R;
import android.example.quantummind.domain.controllers.LoginController;
import android.example.quantummind.domain.entities.User;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

        emailEditText      = findViewById(R.id.emailEditText);
        passwordEditText   = findViewById(R.id.passwordEditText);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        loginController = new LoginController(this);

        loadCredentialsIfNeeded();

        forgotPasswordText.setOnClickListener(v -> resetPassword());
    }

    private void loadCredentialsIfNeeded() {
        String[] creds = loginController.loadCredentials();
        if (creds != null) {
            emailEditText.setText(creds[0]);
            passwordEditText.setText(creds[1]);
            rememberMeCheckBox.setChecked(true);
        }
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

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "You must send a valid email.", Toast.LENGTH_SHORT).show();
                return;
            }
            loginController.resetPassword(email, new LoginController.Callback() {
                @Override public void onSuccess(User user) {
                    Toast.makeText(
                            LoginActivity.this,
                            "An email has been sent to reset your password.",
                            Toast.LENGTH_LONG
                    ).show();
                }
                @Override public void onError(String msg) {
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
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
                    loginController.saveCredentials(email, password);
                } else {
                    loginController.clearCredentials();
                }
                startMainActivity();
            }
            @Override
            public void onError(String errorMsg) {
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }



    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void openRegister(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
