package android.example.quantummind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void createAccount(View view) {
        EditText nameEditText = findViewById(R.id.nameEditText); // Asegúrate de tener este EditText en tu layout
        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.registerPasswordEditText);

        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validación básica
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos.", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Usuario registrado exitosamente
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        // Verifica si el usuario se obtuvo correctamente
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name) // Aquí se establece el nombre del usuario
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // El perfil del usuario se ha actualizado con éxito
                                            Log.d("RegisterActivity", "Nombre de usuario actualizado.");

                                            // Inicia sesión automáticamente al usuario y redirige a MainActivity
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish(); // Cierra la actividad actual para que el usuario no pueda regresar
                                        } else {
                                            // Manejo de errores, en caso de que falle la actualización del perfil
                                            if (profileTask.getException() != null) {
                                                Toast.makeText(RegisterActivity.this, "Error al actualizar el perfil: " + profileTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            // Manejo de errores, en caso de que el objeto user sea null
                            Toast.makeText(RegisterActivity.this, "Error al obtener información del usuario.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Si el registro falla, muestra un mensaje al usuario
                        if (task.getException() != null) {
                            Toast.makeText(RegisterActivity.this, "Fallo en el registro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }



}

