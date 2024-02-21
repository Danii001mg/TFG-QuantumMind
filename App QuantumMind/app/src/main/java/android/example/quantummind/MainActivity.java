package android.example.quantummind;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Configura los elementos de navegación para abrir las actividades correspondientes
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                // Código para la sección de inicio
                return true;
            } else if (id == R.id.navigation_lessons) {
                // Código para la sección de lecciones
                return true;
            } else if (id == R.id.navigation_questions) {
                // Código para la sección de preguntas
                return true;
            } else if (id == R.id.navigation_profile) {
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                return true;
            }
            return false;
        });


        // Seleccionar el ítem de inicio por defecto
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }
}
