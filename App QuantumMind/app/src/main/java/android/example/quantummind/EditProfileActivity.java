package android.example.quantummind;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private EditText userNameEditText, userEmailEditText, userPasswordEditText;
    private Button saveChangesButton;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private ActivityResultLauncher<String> mGetContent;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        initializeUI();
        loadUserProfile();
    }

    private void initializeUI() {
        profileImageView = findViewById(R.id.profileImageView);
        userNameEditText = findViewById(R.id.usernameEditText);
        userEmailEditText = findViewById(R.id.emailEditText);
        userPasswordEditText = findViewById(R.id.passwordEditText);
        saveChangesButton = findViewById(R.id.saveButton);

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImageSelected);
        profileImageView.setOnClickListener(view -> mGetContent.launch("image/*"));

        saveChangesButton.setOnClickListener(view -> saveChanges());
        findViewById(R.id.editUsernameIcon).setOnClickListener(view -> enableEdit(userNameEditText));
        findViewById(R.id.editEmailIcon).setOnClickListener(view -> enableEdit(userEmailEditText));
        findViewById(R.id.editPasswordIcon).setOnClickListener(view -> enableEdit(userPasswordEditText));
    }

    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userNameEditText.setText(user.getDisplayName());
            userEmailEditText.setText(user.getEmail());
            Glide.with(this).load(user.getPhotoUrl() != null ? user.getPhotoUrl() : R.drawable.ic_profile_picture_placeholder).into(profileImageView);
        }
    }

    private void onImageSelected(Uri uri) {
        if (uri != null) {
            imageUri = uri;
            Glide.with(this).load(uri).into(profileImageView);
        }
    }

    private void enableEdit(EditText editText) {
        editText.setEnabled(true);
        editText.requestFocus();
        if (editText == userPasswordEditText) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        editText.setSelection(editText.getText().length());
    }

    private void saveChanges() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String newUserName = userNameEditText.getText().toString().trim();
        String newUserEmail = userEmailEditText.getText().toString().trim();
        String newUserPassword = userPasswordEditText.getText().toString().trim();

        boolean emailChanged = !newUserEmail.equals(user.getEmail()) && !newUserEmail.isEmpty();
        boolean passwordChanged = !newUserPassword.isEmpty();

        if (imageUri != null) {
            uploadImageAndSaveProfile(newUserName, newUserEmail, newUserPassword, emailChanged, passwordChanged);
        } else if (emailChanged || passwordChanged) {
            showReauthDialog(newUserName, newUserEmail, newUserPassword, emailChanged, passwordChanged);
        } else if (!newUserName.isEmpty() && !newUserName.equals(user.getDisplayName())) {
            updateUserName(newUserName);
        } else {
            Toast.makeText(this, "No changes detected.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndSaveProfile(String newUserName, String newUserEmail, String newUserPassword, boolean emailChanged, boolean passwordChanged) {
        StorageReference profileImagesRef = storage.getReference().child("profileImages/" + auth.getCurrentUser().getUid() + ".jpg");

        profileImagesRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> profileImagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
            auth.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Profile image updated.", Toast.LENGTH_SHORT).show();

                    if (emailChanged || passwordChanged) {
                        showReauthDialog(newUserName, newUserEmail, newUserPassword, emailChanged, passwordChanged);
                    }  else {
                        redirectToUserProfile();
                    }
                }
            });
        })).addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Error while uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUserName(String newUserName) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(newUserName).build();
            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Username updated.", Toast.LENGTH_SHORT).show();
                    redirectToUserProfile();
                }
            });
        }
    }

    private void showReauthDialog(String newUserName, String newUserEmail, String newUserPassword, boolean emailChanged, boolean passwordChanged) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Auth required")
                .setMessage("Please, introduce your actual password:")
                .setView(input)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String currentPassword = input.getText().toString();
                    reauthenticateUser(newUserName, newUserEmail, newUserPassword, emailChanged, passwordChanged, currentPassword);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void reauthenticateUser(String newUserName, String newUserEmail, String newUserPassword, boolean emailChanged, boolean passwordChanged, String currentPassword) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
                if (emailChanged) updateUserEmail(newUserEmail);
                if (passwordChanged) updateUserPassword(newUserPassword);


                if (!newUserName.isEmpty()) {
                    updateUserName(newUserName);
                }

                if (emailChanged || passwordChanged) {
                    forceReLogin();
                } else if (!newUserName.isEmpty()) {
                    redirectToUserProfile();
                }
            }).addOnFailureListener(e -> {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(EditProfileActivity.this, "Given password is not valid.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Re-auth Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void updateUserEmail(String newUserEmail) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.verifyBeforeUpdateEmail(newUserEmail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("manualLoginRequired", true);
                    editor.apply();

                    Toast.makeText(EditProfileActivity.this, "Email updated. Please, verify your new email", Toast.LENGTH_SHORT).show();
                    forceReLogin();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Error while updating email.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    private void updateUserPassword(String newUserPassword) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newUserPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                    forceReLogin();
                }
            });
        }
    }

    private void forceReLogin() {
        auth.signOut();
        startActivity(new Intent(EditProfileActivity.this, LoginActivity.class));
        finish();
    }

    private void redirectToUserProfile() {
        Intent intent = new Intent(EditProfileActivity.this, UserProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
