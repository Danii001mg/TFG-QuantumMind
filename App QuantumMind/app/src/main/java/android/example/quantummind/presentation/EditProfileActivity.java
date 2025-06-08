package android.example.quantummind.presentation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.example.quantummind.R;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.example.quantummind.domain.EditProfileController;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private EditText userNameEditText;
    private EditText userEmailEditText;
    private EditText userPasswordEditText;
    private Button saveChangesButton;

    private FirebaseAuth auth;
    private EditProfileController  controller;
    private Uri selectedImageUri;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        controller = new EditProfileController();

        initializeUI();
        loadUserProfileIntoViews();
    }
    private void initializeUI() {
        profileImageView = findViewById(R.id.profileImageView);
        userNameEditText = findViewById(R.id.usernameEditText);
        userEmailEditText = findViewById(R.id.emailEditText);
        userPasswordEditText = findViewById(R.id.passwordEditText);
        saveChangesButton = findViewById(R.id.saveButton);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::onImageSelected
        );

        profileImageView.setOnClickListener(v ->
                pickImageLauncher.launch("image/*")
        );

        findViewById(R.id.editUsernameIcon).setOnClickListener(v -> enableEditing(userNameEditText));
        findViewById(R.id.editEmailIcon).setOnClickListener(v -> enableEditing(userEmailEditText));
        findViewById(R.id.editPasswordIcon).setOnClickListener(v -> enableEditing(userPasswordEditText));

        saveChangesButton.setOnClickListener(v -> saveChanges());
    }

    private void loadUserProfileIntoViews() {
        FirebaseUser firebaseUser = controller.getCurrentUser();
        if (firebaseUser == null) {
            redirectToLogin();
            return;
        }

        String currentName = firebaseUser.getDisplayName();
        userNameEditText.setText(currentName != null ? currentName : "");

        String currentEmail = firebaseUser.getEmail();
        userEmailEditText.setText(currentEmail != null ? currentEmail : "");

        Uri photoUri = firebaseUser.getPhotoUrl();
        if (photoUri != null) {
            Glide.with(this)
                    .load(photoUri)
                    .into(profileImageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_profile_picture_placeholder)
                    .into(profileImageView);
        }

        userNameEditText.setEnabled(false);
        userEmailEditText.setEnabled(false);
        userPasswordEditText.setEnabled(false);
    }

    private void onImageSelected(Uri uri) {
        if (uri != null) {
            selectedImageUri = uri;
            Glide.with(this).load(uri).into(profileImageView);
        }
    }

    private void enableEditing(EditText editText) {
        editText.setEnabled(true);
        editText.requestFocus();

        if (editText == userPasswordEditText) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        editText.setSelection(editText.getText().length());
    }

    private void saveChanges() {
        FirebaseUser firebaseUser = controller.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName     = userNameEditText.getText().toString().trim();
        String newEmail    = userEmailEditText.getText().toString().trim();
        String newPassword = userPasswordEditText.getText().toString().trim();

        String oldEmail = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
        String oldName  = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "";

        boolean nameChanged     = !newName.isEmpty() && !newName.equals(oldName);
        boolean emailChanged    = !newEmail.isEmpty() && !newEmail.equals(oldEmail);
        boolean passwordChanged = !newPassword.isEmpty();
        boolean imageChanged    = (selectedImageUri != null);

        if (!nameChanged && !emailChanged && !passwordChanged && !imageChanged) {
            Toast.makeText(this, "No changes detected.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageChanged) {
            controller.uploadProfileImage(selectedImageUri, new EditProfileController.ImageUploadCallback() {
                @Override
                public void onUploadSuccess(Uri photoUri) {
                    Toast.makeText(EditProfileActivity.this, "Profile image updated.", Toast.LENGTH_SHORT).show();
                    proceedWithNameEmailPasswordUpdates(
                            newName, newEmail, newPassword,
                            nameChanged, emailChanged, passwordChanged
                    );
                }

                @Override
                public void onUploadFailure(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
        else if (emailChanged || passwordChanged) {
            promptForReauthentication(
                    newName, newEmail, newPassword,
                    nameChanged, emailChanged, passwordChanged
            );
        }
        else if (nameChanged) {
            controller.updateDisplayName(newName, new EditProfileController.Callback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(EditProfileActivity.this, "Display name updated.", Toast.LENGTH_SHORT).show();
                    redirectToUserProfile();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void proceedWithNameEmailPasswordUpdates(
            String newName, String newEmail, String newPassword,
            boolean nameChanged, boolean emailChanged, boolean passwordChanged
    ) {
        if (emailChanged || passwordChanged) {
            promptForReauthentication(
                    newName, newEmail, newPassword,
                    nameChanged, emailChanged, passwordChanged
            );
        }
        else if (nameChanged) {
            controller.updateDisplayName(newName, new EditProfileController.Callback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(EditProfileActivity.this, "Display name updated.", Toast.LENGTH_SHORT).show();
                    redirectToUserProfile();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            redirectToUserProfile();
        }
    }

    private void promptForReauthentication(
            String newName, String newEmail, String newPassword,
            boolean nameChanged, boolean emailChanged, boolean passwordChanged
    ) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Authentication Required")
                .setMessage("Please enter your current password to proceed:")
                .setView(input)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String currentPassword = input.getText().toString().trim();
                    if (currentPassword.isEmpty()) {
                        Toast.makeText(EditProfileActivity.this,
                                "Current password cannot be empty.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    controller.reauthenticateUser(currentPassword, new EditProfileController.ReauthCallback() {
                        @Override
                        public void onReauthSuccess() {
                            updateEmailAndPasswordThenName(
                                    newName, newEmail, newPassword,
                                    nameChanged, emailChanged, passwordChanged
                            );
                        }

                        @Override
                        public void onReauthFailure(String errorMessage) {
                            Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void updateEmailAndPasswordThenName(
            String newName, String newEmail, String newPassword,
            boolean nameChanged, boolean emailChanged, boolean passwordChanged
    ) {
        EditProfileController.Callback afterEmailCallback = new EditProfileController.Callback() {
            @Override
            public void onSuccess() {
                if (passwordChanged) {
                    controller.updatePassword(newPassword, new EditProfileController.Callback() {
                        @Override
                        public void onSuccess() {
                            if (nameChanged) {
                                controller.updateDisplayName(newName, new EditProfileController.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        finalizeAfterEmailPasswordName(emailChanged, nameChanged);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                finalizeAfterEmailPasswordName(emailChanged, nameChanged);
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else if (nameChanged) {
                    controller.updateDisplayName(newName, new EditProfileController.Callback() {
                        @Override
                        public void onSuccess() {
                            finalizeAfterEmailPasswordName(emailChanged, nameChanged);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    finalizeAfterEmailPasswordName(emailChanged, nameChanged);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        };

        if (emailChanged) {
            controller.updateEmail(newEmail, afterEmailCallback);
        }
        else if (passwordChanged) {
            controller.updatePassword(newPassword, new EditProfileController.Callback() {
                @Override
                public void onSuccess() {
                    if (nameChanged) {
                        controller.updateDisplayName(newName, new EditProfileController.Callback() {
                            @Override
                            public void onSuccess() {
                                finalizeAfterEmailPasswordName(false, true);
                            }
                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        finalizeAfterEmailPasswordName(false, false);
                    }
                }
                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
        else if (nameChanged) {
            controller.updateDisplayName(newName, new EditProfileController.Callback() {
                @Override
                public void onSuccess() {
                    finalizeAfterEmailPasswordName(false, true);
                }
                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void finalizeAfterEmailPasswordName(boolean emailChanged, boolean nameChanged) {
        if (emailChanged) {
            SharedPreferences prefs = getSharedPreferences("checkbox", MODE_PRIVATE);
            prefs.edit().putBoolean("manualLoginRequired", true).apply();

            Toast.makeText(this,
                    "Email updated. Please confirm in your email inbox and log in again.",
                    Toast.LENGTH_LONG).show();

            auth.signOut();
            redirectToLogin();
        } else {
            Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
            redirectToUserProfile();
        }
    }

    private void redirectToUserProfile() {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
