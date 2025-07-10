package android.example.quantummind.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DatabaseManager {
    private static DatabaseManager instance;

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final SharedPreferences credentialsPrefs;
    private final SharedPreferences checkboxPrefs;

    private DatabaseManager(Context context) {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        credentialsPrefs = context.getApplicationContext()
                .getSharedPreferences("credentials", Context.MODE_PRIVATE);
        checkboxPrefs   = context.getApplicationContext()
                .getSharedPreferences("checkbox", Context.MODE_PRIVATE);
    }

    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public SharedPreferences getCredentialsPrefs() {
        return credentialsPrefs;
    }

    public SharedPreferences getCheckboxPrefs() {
        return checkboxPrefs;
    }
}
