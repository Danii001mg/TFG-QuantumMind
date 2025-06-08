package android.example.quantummind.presentation;

import android.content.Intent;
import android.example.quantummind.R;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.bumptech.glide.Glide;

public class ImageZoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_zoom);

        PhotoView photoView = findViewById(R.id.photoView);

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");

        Glide.with(this)
                .load(imageUrl)
                .into(photoView);
    }
}
