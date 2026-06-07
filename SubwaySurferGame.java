import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class SubwaySurferGame extends JFrame {
    public SubwaySurferGame() {
        setTitle("Subway Surfer Lane Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);

        SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SubwaySurferGame game = new SubwaySurferGame();
            game.setVisible(true);
        });
    }

    class GamePanel extends JPanel implements KeyListener {
        private static final int PANEL_WIDTH = 400;
        private static final int PANEL_HEIGHT = 700;
        private static final int PLAYER_WIDTH = 40;
        private static final int PLAYER_HEIGHT = 60;
        private static final int OBSTACLE_WIDTH = 40;
        private static final int OBSTACLE_HEIGHT = 50;
        private static final int PLAYER_BOTTOM_MARGIN = 70;
        private static final int OBSTACLE_SPEED = 5;
        private static final int MAX_NAME_LENGTH = 15;

        int playerLane = 1;
        ArrayList<int[]> obstacles = new ArrayList<>();
        int score = 0;
        boolean gameOver = false;
        int tickCount = 0;
        int nextObstacleSpawn;

        private final Random random = new Random();
        private final Timer timer;
        private final JTextField nameField = new JTextField();
        private final JButton submitButton = new JButton("Submit");
        private final ArrayList<String[]> allScores = new ArrayList<>();
        private List<String[]> leaderboard = new ArrayList<>();
        private boolean scoreSubmitted = false;
        private String nameErrorMessage = "";

        GamePanel() {
            setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
            setBackground(new Color(0x2b2b2b));
            setLayout(null);
            setFocusable(true);
            addKeyListener(this);

            setupNameInputControls();

            nextObstacleSpawn = randomSpawnInterval();
            timer = new Timer(16, event -> updateGame());
            timer.start();
        }

        private void setupNameInputControls() {
            nameField.setFont(new Font("Arial", Font.PLAIN, 16));
            nameField.setHorizontalAlignment(JTextField.CENTER);
            ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new NameLengthFilter());
            nameField.addActionListener(event -> submitScore());

            submitButton.setFont(new Font("Arial", Font.BOLD, 14));
            submitButton.addActionListener(event -> submitScore());

            nameField.setVisible(false);
            submitButton.setVisible(false);

            add(nameField);
            add(submitButton);
        }

        private void updateGame() {
            if (!gameOver) {
                tickCount++;
                score++;

                if (tickCount >= nextObstacleSpawn) {
                    spawnObstacle();
                    nextObstacleSpawn = tickCount + randomSpawnInterval();
                }

                moveObstacles();
                checkCollisions();
            }

            repaint();
        }

        private int randomSpawnInterval() {
            return 60 + random.nextInt(31);
        }

        private void spawnObstacle() {
            int lane = random.nextInt(3);
            obstacles.add(new int[] {lane, -OBSTACLE_HEIGHT});
        }

        private void moveObstacles() {
            Iterator<int[]> iterator = obstacles.iterator();
            while (iterator.hasNext()) {
                int[] obstacle = iterator.next();
                obstacle[1] += OBSTACLE_SPEED;

                if (obstacle[1] > getHeight()) {
                    iterator.remove();
                }
            }
        }

        private void checkCollisions() {
            int playerY = getPlayerY();
            int playerBottom = playerY + PLAYER_HEIGHT;

            for (int[] obstacle : obstacles) {
                int obstacleLane = obstacle[0];
                int obstacleY = obstacle[1];
                int obstacleBottom = obstacleY + OBSTACLE_HEIGHT;

                if (obstacleLane == playerLane && obstacleBottom >= playerY && obstacleY <= playerBottom) {
                    handleGameOver();
                    break;
                }
            }
        }

        private void handleGameOver() {
            gameOver = true;
            scoreSubmitted = false;
            timer.stop();
            showNameInputControls();
            repaint();
        }

        private void restartGame() {
            hideNameInputControls();
            playerLane = 1;
            obstacles.clear();
            score = 0;
            gameOver = false;
            scoreSubmitted = false;
            nameErrorMessage = "";
            tickCount = 0;
            nextObstacleSpawn = randomSpawnInterval();
            timer.start();
            requestFocusInWindow();
            repaint();
        }

        private void submitScore() {
            if (scoreSubmitted) {
                return;
            }

            String playerName = nameField.getText().trim();

            if (playerName.isEmpty()) {
                nameErrorMessage = "Name cannot be empty";
                nameField.requestFocusInWindow();
                repaint();
                return;
            }

            submitButton.setEnabled(false);
            saveScore(playerName, score);
            refreshLeaderboard();
            scoreSubmitted = true;
            nameErrorMessage = "";
            hideNameInputControls();
            requestFocusInWindow();
            repaint();
        }

        private void saveScore(String playerName, int finalScore) {
            allScores.add(new String[] {playerName, String.valueOf(finalScore)});
        }

        private void refreshLeaderboard() {
            ArrayList<String[]> sortedScores = new ArrayList<>(allScores);
            sortedScores.sort((first, second) -> Integer.compare(
                    Integer.parseInt(second[1]),
                    Integer.parseInt(first[1])));

            int topScoreCount = Math.min(5, sortedScores.size());
            leaderboard = new ArrayList<>(sortedScores.subList(0, topScoreCount));
        }

        private void showNameInputControls() {
            nameField.setText("");
            nameErrorMessage = "";
            positionNameInputControls();
            submitButton.setEnabled(true);
            nameField.setVisible(true);
            submitButton.setVisible(true);
            nameField.requestFocusInWindow();
        }

        private void hideNameInputControls() {
            nameField.setVisible(false);
            submitButton.setVisible(false);
        }

        private void positionNameInputControls() {
            int fieldWidth = 180;
            int fieldHeight = 28;
            int buttonWidth = 100;
            int buttonHeight = 30;
            int fieldX = (getWidth() - fieldWidth) / 2;
            int fieldY = getHeight() / 2 + 40;
            int buttonX = (getWidth() - buttonWidth) / 2;
            int buttonY = fieldY + fieldHeight + 12;

            nameField.setBounds(fieldX, fieldY, fieldWidth, fieldHeight);
            submitButton.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawLanes(g2);
            drawPlayer(g2);
            drawObstacles(g2);
            drawScore(g2);
            drawLeaderboard(g2);

            if (gameOver) {
                drawGameOver(g2);
            }
        }

        private void drawLanes(Graphics2D g2) {
            int laneWidth = getWidth() / 3;

            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(
                    2,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,
                    0,
                    new float[] {12, 12},
                    0));

            for (int i = 1; i < 3; i++) {
                int x = i * laneWidth;
                g2.drawLine(x, 0, x, getHeight());
            }
        }

        private void drawPlayer(Graphics2D g2) {
            g2.setColor(Color.GREEN);
            g2.fillRect(getLaneCenterX(playerLane) - PLAYER_WIDTH / 2, getPlayerY(), PLAYER_WIDTH, PLAYER_HEIGHT);
        }

        private void drawObstacles(Graphics2D g2) {
            g2.setColor(Color.RED);

            for (int[] obstacle : obstacles) {
                int lane = obstacle[0];
                int y = obstacle[1];
                int x = getLaneCenterX(lane) - OBSTACLE_WIDTH / 2;
                g2.fillRect(x, y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
            }
        }

        private void drawScore(Graphics2D g2) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString("Score: " + score, 15, 30);
        }

        private void drawLeaderboard(Graphics2D g2) {
            int panelWidth = 185;
            int panelHeight = 128;
            int panelX = getWidth() - panelWidth - 10;
            int panelY = 10;

            g2.setColor(new Color(0, 0, 0, 155));
            g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 8, 8);

            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(new Color(255, 215, 0));
            g2.drawString("\uD83C\uDFC6 LEADERBOARD", panelX + 10, panelY + 22);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 14));

            for (int i = 0; i < 5; i++) {
                String name = "---";
                String entryScore = "0";

                if (i < leaderboard.size()) {
                    String[] entry = leaderboard.get(i);
                    name = entry[0];
                    entryScore = entry[1];
                }

                String line = "#" + (i + 1) + "  " + name + "  " + entryScore;
                g2.drawString(line, panelX + 10, panelY + 46 + i * 18);
            }
        }

        private void drawGameOver(Graphics2D g2) {
            String gameOverText = "GAME OVER";
            String finalScoreText = "Final Score: " + score;
            String restartText = "Press R to Restart";
            String inputPrompt = "Enter your name:";

            g2.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics gameOverMetrics = g2.getFontMetrics();
            int gameOverX = (getWidth() - gameOverMetrics.stringWidth(gameOverText)) / 2;
            int gameOverY = getHeight() / 2 - 40;

            g2.setColor(Color.RED);
            g2.drawString(gameOverText, gameOverX, gameOverY);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics messageMetrics = g2.getFontMetrics();

            int scoreX = (getWidth() - messageMetrics.stringWidth(finalScoreText)) / 2;

            g2.drawString(finalScoreText, scoreX, gameOverY + 45);

            if (scoreSubmitted) {
                int restartX = (getWidth() - messageMetrics.stringWidth(restartText)) / 2;
                g2.drawString(restartText, restartX, gameOverY + 85);
            } else {
                int promptX = (getWidth() - messageMetrics.stringWidth(inputPrompt)) / 2;
                g2.drawString(inputPrompt, promptX, gameOverY + 85);

                if (!nameErrorMessage.isEmpty()) {
                    g2.setColor(new Color(255, 190, 190));
                    g2.setFont(new Font("Arial", Font.BOLD, 14));
                    FontMetrics errorMetrics = g2.getFontMetrics();
                    int errorX = (getWidth() - errorMetrics.stringWidth(nameErrorMessage)) / 2;
                    g2.drawString(nameErrorMessage, errorX, getHeight() / 2 + 125);
                }
            }
        }

        private int getLaneCenterX(int laneIndex) {
            int laneWidth = getWidth() / 3;
            return laneIndex * laneWidth + laneWidth / 2;
        }

        private int getPlayerY() {
            return getHeight() - PLAYER_BOTTOM_MARGIN - PLAYER_HEIGHT;
        }

        @Override
        public void keyPressed(KeyEvent event) {
            int keyCode = event.getKeyCode();

            if (keyCode == KeyEvent.VK_LEFT && !gameOver) {
                playerLane = Math.max(0, playerLane - 1);
            } else if (keyCode == KeyEvent.VK_RIGHT && !gameOver) {
                playerLane = Math.min(2, playerLane + 1);
            } else if (keyCode == KeyEvent.VK_R && (!gameOver || scoreSubmitted)) {
                restartGame();
            }
        }

        @Override
        public void keyReleased(KeyEvent event) {
        }

        @Override
        public void keyTyped(KeyEvent event) {
        }

        class NameLengthFilter extends DocumentFilter {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attributes)
                    throws BadLocationException {
                if (text == null) {
                    return;
                }

                int availableCharacters = MAX_NAME_LENGTH - fb.getDocument().getLength();
                if (availableCharacters <= 0) {
                    return;
                }

                String insertedText = text.substring(0, Math.min(text.length(), availableCharacters));
                super.insertString(fb, offset, insertedText, attributes);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attributes)
                    throws BadLocationException {
                if (text == null) {
                    return;
                }

                int currentLength = fb.getDocument().getLength();
                int availableCharacters = MAX_NAME_LENGTH - (currentLength - length);
                if (availableCharacters <= 0) {
                    return;
                }

                String replacementText = text.substring(0, Math.min(text.length(), availableCharacters));
                super.replace(fb, offset, length, replacementText, attributes);
            }
        }
    }
}
