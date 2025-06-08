package android.example.quantummind.domain;

public class AchievementItem {
    private int iconId;
    private String title;
    private String description;
    private boolean isUnlocked;

    public AchievementItem(int iconId, String title, String description, boolean isUnlocked) {
        this.iconId = iconId;
        this.title = title;
        this.description = description;
        this.isUnlocked = isUnlocked;
    }

    public int getIconId() {
        return iconId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUnlocked() { return isUnlocked; }

    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
}
