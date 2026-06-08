import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String HOST_URL = "jdbc:mysql://localhost:3306";
    private static final String URL = HOST_URL + "/subway_truck?useSSL=false&serverTimezone=UTC";
    private static final String ROOT_URL = HOST_URL + "/?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static volatile boolean initialized = false;

    public static Connection getConnection() throws SQLException {
        initializeDatabase();
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static synchronized void initializeDatabase() {
        if (initialized) {
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // Driver is auto-registered in newer JDKs.
        }

        try (Connection rootConn = DriverManager.getConnection(ROOT_URL, USER, PASSWORD);
             Statement rootStmt = rootConn.createStatement()) {
            rootStmt.executeUpdate("CREATE DATABASE IF NOT EXISTS subway_truck CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create or access subway_truck database.", e);
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player ("
                    + "player_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(15) NOT NULL UNIQUE, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS score ("
                    + "score_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "player_id INT NOT NULL, "
                    + "score INT NOT NULL, "
                    + "distance INT NOT NULL DEFAULT 0, "
                    + "play_time INT NOT NULL DEFAULT 0, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT fk_score_player FOREIGN KEY (player_id) REFERENCES player(player_id) ON DELETE CASCADE"
                    + ")");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema.", e);
        }

        initialized = true;
    }
}