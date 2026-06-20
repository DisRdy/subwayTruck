import java.sql.Timestamp;

public class ScoreEntry {
    private int scoreId;
    private int playerId;
    private String name;
    private int score;
    private int distance;
    private int playTime;
    private int difficulty;
    private Timestamp createdAt;

    public ScoreEntry(int scoreId, int playerId, String name, int score, int distance, int playTime, int difficulty, Timestamp createdAt) {
        this.scoreId = scoreId;
        this.playerId = playerId;
        this.name = name;
        this.score = score;
        this.distance = distance;
        this.playTime = playTime;
        this.createdAt = createdAt;
    }

    public int getScoreId() {
        return scoreId;
    }

    public int getDifficulty() {
    return difficulty;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public int getDistance() {
        return distance;
    }

    public int getPlayTime() {
        return playTime;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}