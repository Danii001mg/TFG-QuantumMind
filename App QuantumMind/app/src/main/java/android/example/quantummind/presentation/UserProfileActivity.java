package android.example.quantummind.presentation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.example.quantummind.R;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import android.example.quantummind.domain.User;
import android.example.quantummind.domain.UserProfileController;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView profileNameTextView, profileEmailTextView;
    private LinearLayout logOutOption, editOption;

    private UserProfileController controller;
    private User currentUser;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        controller = new UserProfileController();

        profileImageView   = findViewById(R.id.profileImageView);
        profileNameTextView  = findViewById(R.id.profileNameTextView);
        profileEmailTextView = findViewById(R.id.profileEmailTextView);
        logOutOption = findViewById(R.id.logoutOption);
        editOption   = findViewById(R.id.editOption);

        loadUserProfile();

        editOption.setOnClickListener(v ->
                startActivity(new Intent(UserProfileActivity.this, EditProfileActivity.class))
        );

        logOutOption.setOnClickListener(v -> showLogOutDialog());

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profileImageView.setImageURI(uri);
                        uploadNewProfileImage(uri);
                    }
                }
        );

        profileImageView.setOnClickListener(v ->
                pickImageLauncher.launch("image/*")
        );
    }

    private void loadUserProfile() {
        controller.getUserProfile(new UserProfileController.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                String name = user.getDisplayName() != null ? user.getDisplayName() : "";
                profileNameTextView.setText(name);

                String email = user.getEmail() != null ? user.getEmail() : "";
                profileEmailTextView.setText(email);

                String photoUrl = user.getPhotoUrl();
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(UserProfileActivity.this)
                            .load(photoUrl)
                            .into(profileImageView);
                } else {
                    Glide.with(UserProfileActivity.this)
                            .load(R.drawable.ic_profile_picture_placeholder)
                            .into(profileImageView);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(UserProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadNewProfileImage(Uri imageUri) {
        controller.uploadProfileImage(imageUri, new UserProfileController.ImageUploadCallback() {
            @Override
            public void onUploadSuccess(Uri photoUri) {
                Toast.makeText(UserProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                if (currentUser != null) {
                    currentUser.setPhotoUrl(photoUri.toString());
                }
            }

            @Override
            public void onUploadFailure(String errorMessage) {
                Toast.makeText(UserProfileActivity.this, "Upload failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLogOutDialog() {
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", null)
                .show();
    }

    private void performLogout() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        controller.logout();

        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
