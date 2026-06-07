import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ScoreDAO {

    public boolean saveScore(int playerId, int score, int distance, int playTime) {
        String sql = "INSERT INTO score (player_id, score, distance, play_time) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playerId);
            ps.setInt(2, score);
            ps.setInt(3, distance);
            ps.setInt(4, playTime);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<ScoreEntry> getTopScores(int limit) {
        ArrayList<ScoreEntry> list = new ArrayList<>();

        String sql =
                "SELECT s.score_id, s.player_id, p.username, s.score, s.distance, s.play_time, s.created_at " +
                "FROM score s " +
                "JOIN player p ON s.player_id = p.player_id " +
                "ORDER BY s.score DESC, s.created_at ASC " +
                "LIMIT " + limit;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ScoreEntry(
                        rs.getInt("score_id"),
                        rs.getInt("player_id"),
                        rs.getString("username"),
                        rs.getInt("score"),
                        rs.getInt("distance"),
                        rs.getInt("play_time"),
                        rs.getTimestamp("created_at")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}