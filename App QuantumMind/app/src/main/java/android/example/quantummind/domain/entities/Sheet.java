package android.example.quantummind.domain.entities;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Sheet {
    private String id;
    private String lessonId;
    private String text;
    private String image;
    private String title;
    private String video;

    public Sheet() { }

    public Sheet(String id, String lessonId, String text, String image, String title, String video) {
        this.id = id;
        this.lessonId = lessonId;
        this.text = text;
        this.image = image;
        this.title = title;
        this.video = video;
    }

    public static Sheet fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        String id = doc.getString("id");
        String lessonId = doc.getString("lessonId");
        String text = doc.getString("text");
        String image = doc.getString("image");
        String title = doc.getString("title");
        String video = doc.getString("video");
        return new Sheet(id, lessonId, text, image, title, video);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("lessonId", lessonId);
        m.put("text", text);
        m.put("image", image);
        m.put("title", title);
        m.put("video", video);
        return m;
    }

    public String getId() {
        return id;
    }
    public String getLessonId() {
        return lessonId;
    }
    public String getText() {
        return text;
    }
    public String getImage() {
        return image;
    }
    public String getTitle() {
        return title;
    }
    public String getVideo() {
        return video;
    }
}
