package android.example.quantummind.domain;


public class UserProgress {
    private boolean level1Completed;
    private double level1Score;

    private boolean level2Completed;
    private double level2Score;

    private boolean level3Completed;
    private double level3Score;

    private boolean level4Completed;
    private double level4Score;

    private boolean level5Completed;
    private double level5Score;

    private boolean level6Completed;
    private double level6Score;

    public UserProgress() {
    }

    public UserProgress(
            boolean level1Completed, double level1Score,
            boolean level2Completed, double level2Score,
            boolean level3Completed, double level3Score,
            boolean level4Completed, double level4Score,
            boolean level5Completed, double level5Score,
            boolean level6Completed, double level6Score
    ) {
        this.level1Completed = level1Completed;
        this.level1Score = level1Score;
        this.level2Completed = level2Completed;
        this.level2Score = level2Score;
        this.level3Completed = level3Completed;
        this.level3Score = level3Score;
        this.level4Completed = level4Completed;
        this.level4Score = level4Score;
        this.level5Completed = level5Completed;
        this.level5Score = level5Score;
        this.level6Completed = level6Completed;
        this.level6Score = level6Score;
    }

    public boolean isLevel1Completed() {
        return level1Completed;
    }

    public void setLevel1Completed(boolean level1Completed) {
        this.level1Completed = level1Completed;
    }

    public double getLevel1Score() {
        return level1Score;
    }

    public void setLevel1Score(double level1Score) {
        this.level1Score = level1Score;
    }

    public boolean isLevel2Completed() {
        return level2Completed;
    }

    public void setLevel2Completed(boolean level2Completed) {
        this.level2Completed = level2Completed;
    }

    public double getLevel2Score() {
        return level2Score;
    }

    public void setLevel2Score(double level2Score) {
        this.level2Score = level2Score;
    }

    public boolean isLevel3Completed() {
        return level3Completed;
    }

    public void setLevel3Completed(boolean level3Completed) {
        this.level3Completed = level3Completed;
    }

    public double getLevel3Score() {
        return level3Score;
    }

    public void setLevel3Score(double level3Score) {
        this.level3Score = level3Score;
    }

    public boolean isLevel4Completed() {
        return level4Completed;
    }

    public void setLevel4Completed(boolean level4Completed) {
        this.level4Completed = level4Completed;
    }

    public double getLevel4Score() {
        return level4Score;
    }

    public void setLevel4Score(double level4Score) {
        this.level4Score = level4Score;
    }

    public boolean isLevel5Completed() {
        return level5Completed;
    }

    public void setLevel5Completed(boolean level5Completed) {
        this.level5Completed = level5Completed;
    }

    public double getLevel5Score() {
        return level5Score;
    }

    public void setLevel5Score(double level5Score) {
        this.level5Score = level5Score;
    }

    public boolean isLevel6Completed() {
        return level6Completed;
    }

    public void setLevel6Completed(boolean level6Completed) {
        this.level6Completed = level6Completed;
    }

    public double getLevel6Score() {
        return level6Score;
    }

    public void setLevel6Score(double level6Score) {
        this.level6Score = level6Score;
    }
}
