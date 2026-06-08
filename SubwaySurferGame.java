import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;
import javax.swing.text.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SubwaySurferGame extends JFrame {
    static final int W = 450, H = 800, LANES = 3;
    static final int SCALE = 6;

    static final int PLAYER_W = 16 * SCALE;
    static final int PLAYER_H = 32 * SCALE;

    static final int OBS_W = 16 * SCALE;
    static final int OBS_H = 32 * SCALE;
    static final int SPEED = 5, FPS = 16, MAX_NAME = 15, TOP_LIMIT = 5;
    static final int SAFE_MARGIN = 16;
    static final Color BG = new Color(0x1f1f1f);
    static final Color ROAD = new Color(0x272727);
    static final Color LANE_LINE = new Color(0x6f6f6f);
    static final Color PANEL_BG = new Color(0, 0, 0, 170);
    static final Color GOLD = new Color(0xFFD700);
    static final Font FONT_TITLE = new Font("Segoe UI Emoji", Font.BOLD, 36);
    static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 12);
    static final Font FONT_HEADLINE = new Font("Segoe UI", Font.BOLD, 14);
    static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 10);
    static final int ROAD_SCALE = 8;
    static final int ROAD_W = 80 * ROAD_SCALE;
    static final int ROAD_H = 16 * ROAD_SCALE;
    static final int ROAD_GRASS_LEFT = 80;
    static final int ROAD_GRASS_RIGHT = 80;

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
        float titleGlow = 0.6f;
        boolean glowIncreasing = true;
        Timer animator = new Timer(40, e -> animateHome());

        HomePanel() {
            setPreferredSize(new Dimension(W, H));
            setBackground(BG);
            setLayout(new BorderLayout());
            animator.start();
            JPanel bottom = new JPanel();
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
            bottom.setOpaque(false);
            bottom.setBorder(BorderFactory.createEmptyBorder(0, 16, SAFE_MARGIN + 4, 16));
            bottom.add(Box.createRigidArea(new Dimension(0, 8)));
            bottom.add(button("▶ PLAY", new Color(0x4CAF50), new Color(0x66BB6A), e -> showGame()));
            bottom.add(Box.createRigidArea(new Dimension(0, 10)));
            bottom.add(button("⏹ EXIT", new Color(0xD32F2F), new Color(0xEF5350), e -> System.exit(0)));
            add(bottom, BorderLayout.SOUTH);
        }

        JButton button(String text, Color normal, Color hover, ActionListener action) {
            JButton btn = new JButton(text);
            btn.setMaximumSize(new Dimension(220, 48));
            btn.setPreferredSize(new Dimension(220, 48));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            btn.setForeground(Color.WHITE);
            btn.setBackground(normal);
            btn.setOpaque(true);
            btn.setBorderPainted(true);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                    BorderFactory.createEmptyBorder(8, 20, 8, 20)));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(action);
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { 
                    btn.setBackground(hover);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 255, 255, 150), 2),
                            BorderFactory.createEmptyBorder(8, 20, 8, 20)));
                }
                public void mouseExited(MouseEvent e) { 
                    btn.setBackground(normal);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                            BorderFactory.createEmptyBorder(8, 20, 8, 20)));
                }
            });
            return btn;
        }

        void animateHome() {
            if (glowIncreasing) {
                titleGlow += 0.03f;
                if (titleGlow >= 1f) {
                    titleGlow = 1f;
                    glowIncreasing = false;
                }
            } else {
                titleGlow -= 0.03f;
                if (titleGlow <= 0.6f) {
                    titleGlow = 0.6f;
                    glowIncreasing = true;
                }
            }
            repaint();
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
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            GradientPaint background = new GradientPaint(0, 0, new Color(0x111111), 0, getHeight(), BG);
            g.setPaint(background);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(new Color(255, 255, 255, 18));
            g.fillRoundRect(SAFE_MARGIN, SAFE_MARGIN, getWidth() - SAFE_MARGIN * 2, getHeight() - SAFE_MARGIN * 2, 20, 20);
            g.setColor(new Color(255, 255, 255, 8));
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(SAFE_MARGIN, SAFE_MARGIN, getWidth() - SAFE_MARGIN * 2, getHeight() - SAFE_MARGIN * 2, 20, 20);

            g.setColor(PANEL_BG);
            g.fillRoundRect(SAFE_MARGIN + 4, 95, getWidth() - SAFE_MARGIN * 2 - 8, 200, 18, 18);
            g.setColor(new Color(255, 215, 0, 30));
            g.drawRoundRect(SAFE_MARGIN + 4, 95, getWidth() - SAFE_MARGIN * 2 - 8, 200, 18, 18);

            g.setFont(FONT_TITLE);
            Color titleColor = new Color(255, 215, 0, 140 + (int) (95 * titleGlow));
            g.setColor(titleColor);
            g.drawString("🚛 SUBWAY", (getWidth() - g.getFontMetrics().stringWidth("🚛 SUBWAY")) / 2, 65);

            g.setFont(FONT_SUBTITLE);
            g.setColor(Color.LIGHT_GRAY);
            String subtitle = "Mobile edition";
            g.drawString(subtitle, (getWidth() - g.getFontMetrics().stringWidth(subtitle)) / 2, 85);

            int left = SAFE_MARGIN + 16;
            int top = 120;
            g.setFont(FONT_HEADLINE);
            g.setColor(Color.WHITE);
            g.drawString("Why play?", left, top);

            g.setFont(FONT_BODY);
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("🚀 Smooth & fast", left, top + 22);
            g.drawString("🏆 Permanent scores", left, top + 38);
            g.drawString("✨ Zero overlap", left, top + 54);

            g.setFont(FONT_HEADLINE);
            g.setColor(GOLD);
            g.drawString("Best Score", left, top + 80);

            g.setFont(FONT_BODY);
            g.setColor(Color.WHITE);
            g.drawString(bestText, left, top + 100);
            int driveX = SAFE_MARGIN + 20;
            int driveWidth = getWidth() - SAFE_MARGIN * 2 - 40;

            g.setColor(Color.RED);
            g.drawRect(driveX, 0, driveWidth, getHeight());
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
        float fadeAlpha = 0f;
        boolean animatingIn = false;
        Timer fadeTimer = new Timer(20, e -> animateFade());
        JTextField nameField = new JTextField();
        JPanel inputPanel;
        JButton submit = new JButton("Submit");
        BufferedImage truckImg;
        BufferedImage[] carImgs = new BufferedImage[3];
        BufferedImage[] roadTiles = new BufferedImage[2];
        double roadOffset = 0;

        GamePanel() {
            setPreferredSize(new Dimension(W, H));
            setBackground(BG);
            setLayout(new BorderLayout());
            setFocusable(true);
            addKeyListener(this);

            try {
                truckImg = ImageIO.read(new File("res/truck.png"));

                carImgs[0] = ImageIO.read(new File("res/cars1.png"));
                carImgs[1] = ImageIO.read(new File("res/cars2.png"));
                carImgs[2] = ImageIO.read(new File("res/cars3.png"));

                roadTiles[0] = ImageIO.read(new File("res/road1.png"));
                roadTiles[1] = ImageIO.read(new File("res/road2.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            fadeTimer.setRepeats(true);
            setupInput();
        }

        void setupInput() {
            nameField.setPreferredSize(new Dimension(220, 40));
            nameField.setMaximumSize(new Dimension(280, 40));
            nameField.setHorizontalAlignment(JTextField.CENTER);
            nameField.setForeground(Color.WHITE);
            nameField.setBackground(new Color(0x2b2b2b));
            nameField.setCaretColor(GOLD);
            nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            nameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GOLD, 2),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new NameFilter());
            nameField.addActionListener(e -> submitScore());

            submit.setPreferredSize(new Dimension(200, 40));
            submit.setFont(new Font("Segoe UI", Font.BOLD, 14));
            submit.setForeground(Color.WHITE);
            submit.setBackground(new Color(0x4CAF50));
            submit.setOpaque(true);
            submit.setBorderPainted(true);
            submit.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                    BorderFactory.createEmptyBorder(8, 20, 8, 20)));
            submit.setFocusPainted(false);
            submit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            submit.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { 
                    submit.setBackground(new Color(0x66BB6A));
                    submit.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 255, 255, 150), 2),
                            BorderFactory.createEmptyBorder(8, 20, 8, 20)));
                }
                public void mouseExited(MouseEvent e) { 
                    submit.setBackground(new Color(0x4CAF50));
                    submit.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                            BorderFactory.createEmptyBorder(8, 20, 8, 20)));
                }
            });
            submit.addActionListener(e -> submitScore());

            inputPanel = new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (gameOver && isVisible()) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setColor(new Color(0, 0, 0, 100));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            };
            inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 110, 20));
            nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
            submit.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel inputLabel = new JLabel("Player Name");
            inputLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            inputLabel.setForeground(GOLD);
            inputLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            inputPanel.add(Box.createRigidArea(new Dimension(0, 12)));
            inputPanel.add(inputLabel);
            inputPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            inputPanel.add(nameField);
            inputPanel.add(Box.createRigidArea(new Dimension(0, 14)));
            inputPanel.add(submit);
            add(inputPanel, BorderLayout.SOUTH);
            hideInput();
        }

        void start() {
            reset();
            timer.start();
            beginFadeIn();
        }

        void beginFadeIn() {
            fadeAlpha = 0f;
            animatingIn = true;
            fadeTimer.start();
        }

        void animateFade() {
            fadeAlpha += 0.08f;
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f;
                animatingIn = false;
                fadeTimer.stop();
            }
            repaint();
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
            int currentSpeed = getProgressiveSpeed();
            distance += currentSpeed;

            roadOffset += currentSpeed*0.3;

            if (roadOffset >= ROAD_H) {
                roadOffset -= ROAD_H;
            }

            if (tick >= nextSpawn) {
                int sprite = random.nextInt(carImgs.length);

                obstacles.add(
                    new int[] {
                        random.nextInt(LANES),
                        -OBS_H,
                        sprite
                    }
                );
                nextSpawn = tick + spawnDelay();
            }

            moveObstacles();
            checkCollision();
            repaint();
        }

        int getProgressiveSpeed() {
            int baseSpeed = SPEED;
            int bonus = (distance / 500);
            return Math.min(baseSpeed + bonus, SPEED + 5);
        }

        int spawnDelay() {
            int baseSeed = 60 + random.nextInt(31);
            int difficultyReduction = (distance / 500) * 5;
            return Math.max(baseSeed - difficultyReduction, 35);
        }

        void moveObstacles() {
            int currentSpeed = getProgressiveSpeed();
            Iterator<int[]> it = obstacles.iterator();
            while (it.hasNext()) {
                int[] obs = it.next();
                obs[1] += currentSpeed;
                if (obs[1] > getHeight()) {
                    it.remove();
                }
            }
        }

        void checkCollision() {

            Rectangle playerHitbox = new Rectangle(
                laneX(playerLane, PLAYER_W) + 15,
                playerY() + 20,
                PLAYER_W - 30,
                PLAYER_H - 80
            );

            for (int[] obs : obstacles) {

                Rectangle obstacleHitbox = new Rectangle(
                    laneX(obs[0], OBS_W) + 15,
                    obs[1] + 20,
                    OBS_W - 30,
                    OBS_H - 90
                );

                if (playerHitbox.intersects(obstacleHitbox)) {
                    endGame();
                    return;
                }
            }
        }

        void endGame() {
            gameOver = true;
            timer.stop();
            nameField.setText("");
            if (inputPanel != null) {
                inputPanel.setVisible(true);
                nameField.requestFocusInWindow();
            } else {
                nameField.setVisible(true);
                submit.setVisible(true);
                nameField.requestFocusInWindow();
            }
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
            if (inputPanel != null) inputPanel.setVisible(false);
            else {
                nameField.setVisible(false);
                submit.setVisible(false);
            }
        }

        protected void paintComponent(Graphics gr) {
            super.paintComponent(gr);
            Graphics2D g = (Graphics2D) gr;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint background = new GradientPaint(0, 0, new Color(0x121212), 0, getHeight(), BG);
            g.setPaint(background);
            g.fillRect(0, 0, getWidth(), getHeight());

            drawRoad(g);
            drawLanes(g);
            drawGameObjects(g);
            drawHUD(g);
            drawLeaderboard(g);
            drawControls(g);
            if (gameOver) drawGameOver(g);
        }

        void drawRoad(Graphics2D g) {
            g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
            );

            int roadWidth = getWidth() - SAFE_MARGIN * 2;

            for (int y = -(int) roadOffset;
                y < getHeight();
                y += ROAD_H) {

                int index = Math.floorDiv(y, ROAD_H);

                BufferedImage tile =
                    roadTiles[Math.abs(index) % roadTiles.length];

                g.drawImage(
                    tile,
                    SAFE_MARGIN,
                    y,
                    roadWidth,
                    ROAD_H,
                    null
                );
            }
        }

        void drawLanes(Graphics2D g) {
            int laneW = (getWidth() - SAFE_MARGIN * 2) / LANES;
            g.setColor(LANE_LINE);
            g.setStroke(new BasicStroke(
                    2,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,
                    1,
                    new float[] {12, 12},
                    0));
            for (int i = 1; i < LANES; i++) {
                int x = SAFE_MARGIN + i * laneW;
                g.drawLine(x, SAFE_MARGIN + 20, x, getHeight() - SAFE_MARGIN - 20);
            }
        }

        void drawGameObjects(Graphics2D g) {
            // Player
            int playerX = laneX(playerLane, PLAYER_W);
            int playerY = playerY();

            g.drawImage(
                truckImg,
                playerX,
                playerY,
                PLAYER_W,
                PLAYER_H,
                null
            );

            // Obstacles
            for (int[] obs : obstacles) {
                g.drawImage(
                    carImgs[obs[2]],
                    laneX(obs[0], OBS_W),
                    obs[1],
                    OBS_W,
                    OBS_H,
                    null
                );
            }
        }

        void drawHUD(Graphics2D g) {
            g.setColor(PANEL_BG);
            g.fillRoundRect(SAFE_MARGIN, SAFE_MARGIN, 180, 90, 14, 14);
            g.setColor(new Color(255, 215, 0, 40));
            g.drawRoundRect(SAFE_MARGIN, SAFE_MARGIN, 180, 90, 14, 14);

            g.setColor(Color.WHITE);
            g.setFont(FONT_HEADLINE);
            g.drawString("Score: " + score, SAFE_MARGIN + 16, SAFE_MARGIN + 28);
            g.setFont(FONT_BODY);
            g.drawString("Distance: " + distance, SAFE_MARGIN + 16, SAFE_MARGIN + 52);
            g.drawString("Time: " + (tick / 60) + "s", SAFE_MARGIN + 16, SAFE_MARGIN + 76);
        }

        void drawLeaderboard(Graphics2D g) {
            int x = getWidth() - SAFE_MARGIN - 180;
            g.setColor(PANEL_BG);
            g.fillRoundRect(x, SAFE_MARGIN, 180, 130, 14, 14);
            g.setColor(new Color(255, 215, 0, 40));
            g.drawRoundRect(x, SAFE_MARGIN, 180, 130, 14, 14);

            g.setFont(FONT_SMALL);
            g.setColor(GOLD);
            g.drawString("🏆 TOP 5", x + 12, SAFE_MARGIN + 22);

            g.setFont(FONT_BODY);
            g.setColor(Color.WHITE);
            for (int i = 0; i < TOP_LIMIT; i++) {
                g.drawString(rankLine(i), x + 12, SAFE_MARGIN + 42 + i * 18);
            }
        }

        String rankLine(int i) {
            if (i >= leaderboard.size()) {
                return "#" + (i + 1) + "  ---  0";
            }
            ScoreEntry entry = leaderboard.get(i);
            return "#" + (i + 1) + "  " + entry.getName() + "  " + entry.getScore();
        }

        void drawControls(Graphics2D g) {
            int panelY = getHeight() - SAFE_MARGIN - 85;
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRoundRect(SAFE_MARGIN, panelY, getWidth() - SAFE_MARGIN * 2, 80, 14, 14);

            g.setColor(Color.WHITE);
            g.setFont(FONT_SMALL);
            g.drawString("← → Move", SAFE_MARGIN + 12, panelY + 20);
            g.drawString("R Restart", SAFE_MARGIN + 12, panelY + 36);
            g.drawString("H Home", SAFE_MARGIN + 12, panelY + 52);
            g.drawString("Submit to save", getWidth() / 2 - 40, panelY + 36);
        }

        void drawScore(Graphics2D g) {
            // Deprecated: replaced by drawHUD().
        }

        void drawGameOver(Graphics2D g) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());
            
            int x = SAFE_MARGIN + 8;
            int y = SAFE_MARGIN + 50;
            int width = getWidth() - SAFE_MARGIN * 2 - 16;
            int height = getHeight() - SAFE_MARGIN * 2 - 110;

            g.setColor(new Color(0, 0, 0, 240));
            g.fillRoundRect(x, y, width, height, 20, 20);
            g.setColor(new Color(255, 215, 0, 180));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(x, y, width, height, 20, 20);

            center(g, "🎮 GAME OVER", 26, new Color(255, 100, 100), y + 40);
            center(g, "Score: " + score, 16, GOLD, y + 70);
            center(g, "Distance: " + distance + "m", 12, Color.WHITE, y + 88);
            
            if (!submitted) {
                center(g, "Save your score below", 11, Color.LIGHT_GRAY, y + 110);
            } else {
                center(g, "✓ Score saved!", 16, new Color(100, 255, 100), y + 110);
            }
            center(g, "R:Restart  H:Home", 10, Color.LIGHT_GRAY, y + height - 20);
        }

        void center(Graphics2D g, String text, int size, Color color, int y) {
            g.setFont(new Font("Segoe UI", Font.BOLD, size));
            g.setColor(color);
            g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2, y);
        }

        int laneX(int lane, int width) {

            int roadX = SAFE_MARGIN;
            int roadWidth = getWidth() - SAFE_MARGIN * 2;

            // Amount of grass on each side in your sprite

            int driveX = roadX + ROAD_GRASS_LEFT;
            int driveWidth = roadWidth - ROAD_GRASS_LEFT - ROAD_GRASS_RIGHT;

            int laneW = driveWidth / LANES;

            return driveX + lane * laneW + laneW / 2 - width / 2;
        }

        int playerY() {
            return getHeight() - 115;
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