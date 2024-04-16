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

        String[] achievements = new String[] {"Achievement 1", "Achievement 2", "Achievement 3"};

        achievementsListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, achievements));
    }
}