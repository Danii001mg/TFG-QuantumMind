package android.example.quantummind;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView profileNameTextView, profileEmailTextView;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        profileImageView = findViewById(R.id.profileImageView);
        profileNameTextView = findViewById(R.id.profileNameTextView);
        profileEmailTextView = findViewById(R.id.profileEmailTextView);
        LinearLayout settingsOption = findViewById(R.id.settingsOption);
        LinearLayout logOutOption = findViewById(R.id.logoutOption);
        LinearLayout editOption = findViewById(R.id.editOption);

        // Cargar información del usuario
        loadUserProfile();

        editOption.setOnClickListener(view -> startActivity(new Intent(UserProfileActivity.this, EditProfileActivity.class)));
        settingsOption.setOnClickListener(view -> startActivity(new Intent(UserProfileActivity.this, SettingsActivity.class)));
        logOutOption.setOnClickListener(view -> showLogOutDialog());

        mGetContent = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profileImageView.setImageURI(uri);
                        uploadImageToFirebaseStorage(uri);
                    }
                });

    }

    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            profileNameTextView.setText(user.getDisplayName());
            profileEmailTextView.setText(user.getEmail());
            if (user.getPhotoUrl() != null) {
                Glide.with(this /* context */)
                        .load(user.getPhotoUrl())
                        .into(profileImageView);
            }
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        if (imageUri != null) {
            StorageReference storageRef = storage.getReference();
            StorageReference profileImagesRef = storageRef.child("profileImages/" + auth.getCurrentUser().getUid() + ".jpg");

            UploadTask uploadTask = profileImagesRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> profileImagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(uri)
                        .build();
                auth.getCurrentUser().updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(UserProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                            }
                        });
            })).addOnFailureListener(e -> Toast.makeText(UserProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void showLogOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> logOut())
                .setNegativeButton("No", null)
                .show();
    }

    private void logOut() {
        auth.signOut();
        startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
        finish();
    }
}
