package android.example.quantummind.domain;

public class RankingItem {
    private int position;
    private String username;
    private int percentageCompleted;
    private double score;

    public RankingItem(int position, String username, int percentageCompleted, double score) {
        this.position = position;
        this.username = username;
        this.percentageCompleted = percentageCompleted;
        this.score = score;
    }

    public int getPosition() {
        return position;
    }

    public String getUsername() {
        return username;
    }

    public int getPercentageCompleted() {
        return percentageCompleted;
    }

    public double getScore() {
        return score;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
