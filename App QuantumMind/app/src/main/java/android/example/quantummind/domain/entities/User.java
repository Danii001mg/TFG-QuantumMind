package android.example.quantummind.domain.entities;

import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String uid;
    private String email;
    private String displayName;
    private String photoUrl;

    public User() {}

    public User(String uid, String email, String displayName, String photoUrl) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    public static User fromFirebaseUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return null;
        String photo = (firebaseUser.getPhotoUrl() != null)
                ? firebaseUser.getPhotoUrl().toString()
                : null;
        return new User(
                firebaseUser.getUid(),
                firebaseUser.getEmail(),
                firebaseUser.getDisplayName(),
                photo
        );
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("uid", uid);
        m.put("email", email);
        m.put("displayName", displayName);
        if (photoUrl != null) {
            m.put("photoUrl", photoUrl);
        }
        return m;
    }

    // Getters y setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
