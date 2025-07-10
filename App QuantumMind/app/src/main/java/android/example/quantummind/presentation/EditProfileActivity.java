package android.example.quantummind.presentation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.example.quantummind.R;
import android.example.quantummind.domain.entities.User;
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

import android.example.quantummind.domain.controllers.UserProfileController;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private EditText userNameEditText;
    private EditText userEmailEditText;
    private EditText userPasswordEditText;
    private Button saveChangesButton;

    private FirebaseAuth auth;
    private UserProfileController controller;
    private Uri selectedImageUri;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        controller = new UserProfileController(this);

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

        profileImageView.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        findViewById(R.id.editUsernameIcon).setOnClickListener(v -> enableEditing(userNameEditText));
        findViewById(R.id.editEmailIcon).setOnClickListener(v -> enableEditing(userEmailEditText));
        findViewById(R.id.editPasswordIcon).setOnClickListener(v -> enableEditing(userPasswordEditText));

        saveChangesButton.setOnClickListener(v -> saveChanges());
    }

    private void loadUserProfileIntoViews() {
        controller.getUserProfile(new UserProfileController.ProfileCallback() {
            @Override
            public void onSuccess(User user) {
                userNameEditText.setText(user.getDisplayName());
                userEmailEditText.setText(user.getEmail());

                String photoUrl = user.getPhotoUrl();
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Uri photoUri = Uri.parse(photoUrl);
                    Glide.with(EditProfileActivity.this).load(photoUri).into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.ic_profile_picture_placeholder);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
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
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "User is not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = userNameEditText.getText().toString().trim();
        String newEmail = userEmailEditText.getText().toString().trim();
        String newPassword = userPasswordEditText.getText().toString().trim();

        String oldEmail = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
        String oldName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "";

        boolean nameChanged = !newName.isEmpty() && !newName.equals(oldName);
        boolean emailChanged = !newEmail.isEmpty() && !newEmail.equals(oldEmail);
        boolean passwordChanged = !newPassword.isEmpty();
        boolean imageChanged = selectedImageUri != null;

        if (!nameChanged && !emailChanged && !passwordChanged && !imageChanged) {
            Toast.makeText(this, "No changes detected.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageChanged) {
            controller.uploadProfileImage(selectedImageUri, new UserProfileController.ImageUploadCallback() {
                @Override
                public void onUploadSuccess(Uri photoUri) {
                    Toast.makeText(EditProfileActivity.this, "Profile image updated.", Toast.LENGTH_SHORT).show();
                    proceedWithNameEmailPasswordUpdates(newName, newEmail, newPassword, nameChanged, emailChanged, passwordChanged);
                }

                @Override
                public void onUploadFailure(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } else if (emailChanged || passwordChanged) {
            promptForReauthentication(newName, newEmail, newPassword, nameChanged, emailChanged, passwordChanged);
        } else if (nameChanged) {
            controller.updateDisplayName(newName, new UserProfileController.ProfileCallback() {
                @Override
                public void onSuccess(User user) {
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
            promptForReauthentication(newName, newEmail, newPassword, nameChanged, emailChanged, passwordChanged);
        }
        else if (nameChanged) {
            controller.updateDisplayName(newName, new UserProfileController.ProfileCallback() {
                @Override
                public void onSuccess(User user) {
                    Toast.makeText(EditProfileActivity.this, "Display name updated.", Toast.LENGTH_SHORT).show();
                    redirectToUserProfile();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            redirectToUserProfile();
        }
    }


    private void promptForReauthentication(String newName, String newEmail, String newPassword,
                                           boolean nameChanged, boolean emailChanged, boolean passwordChanged) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter your current password");

        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Authentication Required")
                .setMessage("To update your email or password, please enter your current password:")
                .setView(input)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String currentPassword = input.getText().toString().trim();
                    if (currentPassword.isEmpty()) {
                        Toast.makeText(EditProfileActivity.this,
                                "Current password cannot be empty.",
                                Toast.LENGTH_SHORT).show();
                        promptForReauthentication(newName, newEmail, newPassword, nameChanged, emailChanged, passwordChanged);
                        return;
                    }

                    saveChangesButton.setEnabled(false);
                    saveChangesButton.setText("Authenticating...");

                    controller.reauthenticateUser(currentPassword, new UserProfileController.ReauthCallback() {
                        @Override
                        public void onReauthSuccess() {
                            saveChangesButton.setEnabled(true);
                            saveChangesButton.setText("Save Changes");

                            updateEmailAndPasswordThenName(newName, newEmail, newPassword, nameChanged, emailChanged, passwordChanged);
                        }

                        @Override
                        public void onReauthFailure(String errorMessage) {
                            saveChangesButton.setEnabled(true);
                            saveChangesButton.setText("Save Changes");

                            Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();

                            if (errorMessage.contains("incorrect") || errorMessage.contains("invalid")) {
                                promptForReauthentication(newName, newEmail, newPassword, nameChanged, emailChanged, passwordChanged);
                            }
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                    saveChangesButton.setEnabled(true);
                    saveChangesButton.setText("Save Changes");
                })
                .setCancelable(false)
                .show();
    }

    private void updateEmailAndPasswordThenName(String newName, String newEmail, String newPassword,
                                                boolean nameChanged, boolean emailChanged, boolean passwordChanged) {
        if (emailChanged) {
            controller.updateEmail(newEmail, new UserProfileController.ProfileCallback() {
                @Override
                public void onSuccess(User user) {
                    if (passwordChanged) {
                        controller.updatePassword(newPassword, new UserProfileController.ProfileCallback() {
                            @Override
                            public void onSuccess(User user) {
                                if (nameChanged) {
                                    controller.updateDisplayName(newName, new UserProfileController.ProfileCallback() {
                                        @Override
                                        public void onSuccess(User user) {
                                            finalizeWithLogout("Email and password updated successfully. Please log in again.");
                                        }

                                        @Override
                                        public void onError(String errorMessage) {
                                            Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                            finalizeWithLogout("Email and password updated successfully. Please log in again.");
                                        }
                                    });
                                } else {
                                    finalizeWithLogout("Email and password updated successfully. Please log in again.");
                                }
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                finalizeWithLogout("Email updated successfully. Please verify your new email and log in again.");
                            }
                        });
                    } else if (nameChanged) {
                        controller.updateDisplayName(newName, new UserProfileController.ProfileCallback() {
                            @Override
                            public void onSuccess(User user) {
                                finalizeWithLogout("Email updated successfully. Please verify your new email and log in again.");
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                finalizeWithLogout("Email updated successfully. Please verify your new email and log in again.");
                            }
                        });
                    } else {
                        finalizeWithLogout("Email updated successfully. Please verify your new email and log in again.");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } else if (passwordChanged) {
            controller.updatePassword(newPassword, new UserProfileController.ProfileCallback() {
                @Override
                public void onSuccess(User user) {
                    if (nameChanged) {
                        controller.updateDisplayName(newName, new UserProfileController.ProfileCallback() {
                            @Override
                            public void onSuccess(User user) {
                                finalizeWithLogout("Password updated successfully. Please log in again.");
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                finalizeWithLogout("Password updated successfully. Please log in again.");
                            }
                        });
                    } else {
                        finalizeWithLogout("Password updated successfully. Please log in again.");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } else if (nameChanged) {
            controller.updateDisplayName(newName, new UserProfileController.ProfileCallback() {
                @Override
                public void onSuccess(User user) {
                    Toast.makeText(EditProfileActivity.this, "Display name updated successfully.", Toast.LENGTH_SHORT).show();
                    redirectToUserProfile();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    redirectToUserProfile();
                }
            });
        }
    }

    private void finalizeWithLogout(String message) {
        SharedPreferences prefs = getSharedPreferences("checkbox", MODE_PRIVATE);
        prefs.edit().putBoolean("manualLoginRequired", true).apply();

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        auth.signOut();
        redirectToLogin();
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
