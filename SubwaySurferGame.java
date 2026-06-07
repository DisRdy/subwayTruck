import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;
import javax.swing.text.*;

public class SubwaySurferGame extends JFrame {
    static final int W = 400, H = 700, LANES = 3;
    static final int PLAYER_W = 40, PLAYER_H = 60, OBS_W = 40, OBS_H = 50;
    static final int SPEED = 5, FPS = 16, MAX_NAME = 15, TOP_LIMIT = 5;
    static final Color BG = new Color(0x2b2b2b), GOLD = new Color(0xFFD700);

    private final PlayerDAO playerDAO = new PlayerDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final ArrayList<ScoreEntry> leaderboard = new ArrayList<>();

    CardLayout cards = new CardLayout();
    JPanel root = new JPanel(cards);
    HomePanel home = new HomePanel();
    GamePanel game = new GamePanel();

    public SubwaySurferGame() {
        setTitle("Subway Truck");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        root.add(home, "HOME");
        root.add(game, "GAME");
        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
        refreshLeaderboard();
        showHome();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SubwaySurferGame().setVisible(true));
    }

    void refreshLeaderboard() {
        leaderboard.clear();
        leaderboard.addAll(scoreDAO.getTopScores(TOP_LIMIT));
    }

    void showHome() {
        game.stop();
        refreshLeaderboard();
        home.refreshBest();
        cards.show(root, "HOME");
    }

    void showGame() {
        refreshLeaderboard();
        cards.show(root, "GAME");
        game.start();
        SwingUtilities.invokeLater(game::requestFocusInWindow);
    }

    boolean saveScoreToDatabase(String name, int score, int distance, int playTime) {
        int playerId = playerDAO.findOrCreatePlayer(name);
        if (playerId == -1) {
            return false;
        }

        boolean saved = scoreDAO.saveScore(playerId, score, distance, playTime);
        if (saved) {
            refreshLeaderboard();
        }
        return saved;
    }

    class HomePanel extends JPanel {
        String bestText = "🏆 No records yet. Be the first!";

        HomePanel() {
            setPreferredSize(new Dimension(W, H));
            setBackground(BG);
            setLayout(null);
            add(button("▶  PLAY", 340, new Color(0x4CAF50), new Color(0x66BB6A), e -> showGame()));
            add(button("EXIT", 410, new Color(0xD32F2F), new Color(0xEF5350), e -> System.exit(0)));
        }

        JButton button(String text, int y, Color normal, Color hover, ActionListener action) {
            JButton btn = new JButton(text);
            btn.setBounds(120, y, 160, 55);
            btn.setFont(new Font("Arial", Font.BOLD, 22));
            btn.setForeground(Color.WHITE);
            btn.setBackground(normal);
            btn.setFocusPainted(false);
            btn.addActionListener(action);
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
                public void mouseExited(MouseEvent e) { btn.setBackground(normal); }
            });
            return btn;
        }

        void refreshBest() {
            if (leaderboard.isEmpty()) {
                bestText = "🏆 No records yet. Be the first!";
            } else {
                ScoreEntry best = leaderboard.get(0);
                bestText = "🏆 Best: " + best.getName() + " - " + best.getScore();
            }
            repaint();
        }

        protected void paintComponent(Graphics gr) {
            super.paintComponent(gr);
            Graphics2D g = (Graphics2D) gr;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            center(g, "🚛 SUBWAY TRUCK", 48, GOLD, 120);
            center(g, "How far can you go?", 16, Color.LIGHT_GRAY, 155);
            center(g, bestText, 14, Color.WHITE, 655);
        }

        void center(Graphics2D g, String text, int size, Color color, int y) {
            g.setFont(new Font("Segoe UI Emoji", Font.BOLD, size));
            g.setColor(color);
            g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2, y);
        }
    }

    class GamePanel extends JPanel implements KeyListener {
        int playerLane = 1, score = 0, tick = 0, nextSpawn = 70, distance = 0;
        boolean gameOver = false, submitted = false;
        String error = "";
        Random random = new Random();
        ArrayList<int[]> obstacles = new ArrayList<>();
        Timer timer = new Timer(FPS, e -> update());
        JTextField nameField = new JTextField();
        JButton submit = new JButton("Submit");

        GamePanel() {
            setPreferredSize(new Dimension(W, H));
            setBackground(BG);
            setLayout(null);
            setFocusable(true);
            addKeyListener(this);
            setupInput();
        }

        void setupInput() {
            nameField.setBounds(110, 350, 180, 28);
            nameField.setHorizontalAlignment(JTextField.CENTER);
            ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new NameFilter());
            nameField.addActionListener(e -> submitScore());

            submit.setBounds(150, 390, 100, 30);
            submit.addActionListener(e -> submitScore());

            add(nameField);
            add(submit);
            hideInput();
        }

        void start() {
            reset();
            timer.start();
        }

        void stop() {
            timer.stop();
            hideInput();
        }

        void reset() {
            playerLane = 1;
            score = 0;
            tick = 0;
            distance = 0;
            nextSpawn = spawnDelay();
            gameOver = false;
            submitted = false;
            error = "";
            obstacles.clear();
            hideInput();
            repaint();
        }

        void update() {
            score++;
            tick++;
            distance += SPEED;

            if (tick >= nextSpawn) {
                obstacles.add(new int[] { random.nextInt(LANES), -OBS_H });
                nextSpawn = tick + spawnDelay();
            }

            moveObstacles();
            checkCollision();
            repaint();
        }

        int spawnDelay() {
            return 60 + random.nextInt(31);
        }

        void moveObstacles() {
            Iterator<int[]> it = obstacles.iterator();
            while (it.hasNext()) {
                int[] obs = it.next();
                obs[1] += SPEED;
                if (obs[1] > getHeight()) {
                    it.remove();
                }
            }
        }

        void checkCollision() {
            int playerY = playerY();
            for (int[] obs : obstacles) {
                boolean sameLane = obs[0] == playerLane;
                boolean sameY = obs[1] + OBS_H >= playerY && obs[1] <= playerY + PLAYER_H;
                if (sameLane && sameY) {
                    endGame();
                    return;
                }
            }
        }

        void endGame() {
            gameOver = true;
            timer.stop();
            nameField.setText("");
            nameField.setVisible(true);
            submit.setVisible(true);
            nameField.requestFocusInWindow();
        }

        void submitScore() {
            if (submitted) return;

            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                error = "Name cannot be empty";
                repaint();
                return;
            }

            boolean saved = saveScoreToDatabase(name, score, distance, tick);
            if (!saved) {
                error = "Failed to save score";
                repaint();
                return;
            }

            submitted = true;
            error = "";
            hideInput();
            requestFocusInWindow();
            repaint();
        }

        void hideInput() {
            nameField.setVisible(false);
            submit.setVisible(false);
        }

        protected void paintComponent(Graphics gr) {
            super.paintComponent(gr);
            Graphics2D g = (Graphics2D) gr;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawLanes(g);
            drawGameObjects(g);
            drawScore(g);
            drawLeaderboard(g);
            if (gameOver) drawGameOver(g);
        }

        void drawLanes(Graphics2D g) {
            int laneW = getWidth() / LANES;
            g.setColor(Color.LIGHT_GRAY);
            g.setStroke(new BasicStroke(
                    2,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,
                    1,
                    new float[] {12, 12},
                    0));
            for (int i = 1; i < LANES; i++) {
                g.drawLine(i * laneW, 0, i * laneW, getHeight());
            }
        }

        void drawGameObjects(Graphics2D g) {
            g.setColor(Color.GREEN);
            g.fillRect(laneX(playerLane, PLAYER_W), playerY(), PLAYER_W, PLAYER_H);

            g.setColor(Color.RED);
            for (int[] obs : obstacles) {
                g.fillRect(laneX(obs[0], OBS_W), obs[1], OBS_W, OBS_H);
            }
        }

        void drawScore(Graphics2D g) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + score, 15, 30);
        }

        void drawLeaderboard(Graphics2D g) {
            int x = getWidth() - 195;
            g.setColor(new Color(0, 0, 0, 155));
            g.fillRoundRect(x, 10, 185, 128, 8, 8);

            g.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
            g.setColor(GOLD);
            g.drawString("🏆 LEADERBOARD", x + 10, 32);

            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.setColor(Color.WHITE);
            for (int i = 0; i < TOP_LIMIT; i++) {
                g.drawString(rankLine(i), x + 10, 56 + i * 18);
            }
        }

        String rankLine(int i) {
            if (i >= leaderboard.size()) return "#" + (i + 1) + "  ---  0";
            ScoreEntry entry = leaderboard.get(i);
            return "#" + (i + 1) + "  " + entry.getName() + "  " + entry.getScore();
        }

        void drawGameOver(Graphics2D g) {
            center(g, "GAME OVER", 36, Color.RED, 245);
            center(g, "Final Score: " + score, 20, Color.WHITE, 290);
            if (!submitted) {
                center(g, "Enter your name:", 20, Color.WHITE, 330);
                center(g, error, 14, new Color(255, 190, 190), 445);
            }
            center(g, "Press R to Restart", 16, Color.WHITE, 470);
            center(g, "Press H for Home", 16, Color.WHITE, 495);
        }

        void center(Graphics2D g, String text, int size, Color color, int y) {
            g.setFont(new Font("Arial", Font.BOLD, size));
            g.setColor(color);
            g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2, y);
        }

        int laneX(int lane, int width) {
            int laneW = getWidth() / LANES;
            return lane * laneW + laneW / 2 - width / 2;
        }

        int playerY() {
            return getHeight() - 130;
        }

        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT && !gameOver) playerLane = Math.max(0, playerLane - 1);
            if (key == KeyEvent.VK_RIGHT && !gameOver) playerLane = Math.min(LANES - 1, playerLane + 1);
            if (key == KeyEvent.VK_R && gameOver) { reset(); timer.start(); }
            if (key == KeyEvent.VK_H && gameOver) showHome();
        }

        public void keyReleased(KeyEvent e) {}
        public void keyTyped(KeyEvent e) {}
    }

    static class NameFilter extends DocumentFilter {
        public void insertString(FilterBypass fb, int off, String text, AttributeSet attr)
                throws BadLocationException {
            replace(fb, off, 0, text, attr);
        }

        public void replace(FilterBypass fb, int off, int len, String text, AttributeSet attr)
                throws BadLocationException {
            if (text == null) return;
            int space = MAX_NAME - (fb.getDocument().getLength() - len);
            String accepted = text.substring(0, Math.min(text.length(), Math.max(space, 0)));
            super.replace(fb, off, len, accepted, attr);
        }
    }
}