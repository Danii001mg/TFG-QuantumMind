package android.example.quantummind.presentation;

import android.content.Intent;
import android.example.quantummind.R;
import android.example.quantummind.domain.controllers.LoginController;
import android.example.quantummind.domain.entities.User;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private LoginController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        controller = new LoginController(this);
    }

    public void createAccount(android.view.View view) {
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.registerPasswordEditText);

        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please, fill all the fields.", Toast.LENGTH_LONG).show();
            return;
        }

        controller.register(name, email, password, new LoginController.Callback() {
            @Override
            public void onSuccess(User user) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
