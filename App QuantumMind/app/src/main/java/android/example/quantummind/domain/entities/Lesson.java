package android.example.quantummind.domain.entities;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Lesson {
    private String id;
    private String title;

    public Lesson() { }

    public Lesson(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public static Lesson fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        String id = doc.getString("id");
        String title = doc.getString("title");
        return new Lesson(id, title);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("title", title);
        return m;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
