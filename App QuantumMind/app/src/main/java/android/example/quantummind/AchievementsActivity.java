package android.example.quantummind;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

public class AchievementsActivity extends AppCompatActivity {

    ListView achievementsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        achievementsListView = findViewById(R.id.achievementsListView);

        // Ejemplo de datos de logros, reemplázalos con tus datos reales
        String[] achievements = new String[] {"Logro 1", "Logro 2", "Logro 3"};

        // Usando un ArrayAdapter simple para mostrar los logros
        achievementsListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, achievements));
    }
}