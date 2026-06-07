public class Player {
    private int playerId;
    private String username;

    public Player(int playerId, String username) {
        this.playerId = playerId;
        this.username = username;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }
}