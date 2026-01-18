import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.io.*;
import javax.sound.sampled.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    static final int WIDTH = 500;
    static final int HEIGHT = 500;
    static final int UNIT_SIZE = 20;
    static final int GAME_UNITS = (WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 100;

    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];

    int bodyParts, applesEaten, appleX, appleY;
    char direction;
    boolean running = false, paused = false;

    Timer timer;
    Random random;

    int highScore = 0;

    SnakeGame() {
        random = new Random();
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(new Color(20, 20, 20));
        this.setFocusable(true);
        this.addKeyListener(this);
        loadHighScore();
        startGame();
    }

    public void startGame() {
        bodyParts = 4;
        applesEaten = 0;
        direction = 'R';
        paused = false;

        x[0] = 100;
        y[0] = 100;

        newApple();
        running = true;

        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            // Food
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            // Snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(new Color(0, 200, 0));
                    g.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 10, 10);
                    drawEyes(g, x[i], y[i]);
                } else {
                    g.setColor(new Color(0, 150, 0));
                    g.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 8, 8);
                }
            }

            // Score
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Score: " + applesEaten, 10, 20);
            g.drawString("High Score: " + highScore, 350, 20);

            if (paused) {
                g.setFont(new Font("Arial", Font.BOLD, 30));
                g.drawString("PAUSED", 180, 250);
            }

        } else {
            gameOver(g);
        }
    }

    private void drawEyes(Graphics g, int x, int y) {
        g.setColor(Color.white);
        g.fillOval(x + 4, y + 4, 4, 4);
        g.fillOval(x + 12, y + 4, 4, 4);
    }

    public void newApple() {
        appleX = random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U': y[0] -= UNIT_SIZE; break;
            case 'D': y[0] += UNIT_SIZE; break;
            case 'L': x[0] -= UNIT_SIZE; break;
            case 'R': x[0] += UNIT_SIZE; break;
        }
    }

    public void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++;
            applesEaten++;
            playSound("eat.wav");
            newApple();
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) running = false;
        }

        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT)
            running = false;

        if (!running) {
            playSound("gameover.wav");
            updateHighScore();
            timer.stop();
        }
    }

    public void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Game Over", 150, 220);

        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + applesEaten, 200, 260);
        g.drawString("High Score: " + highScore, 180, 290);
        g.drawString("Press R to Restart", 160, 330);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && !paused) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_P) paused = !paused;
        if (e.getKeyCode() == KeyEvent.VK_R && !running) startGame();

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: if (direction != 'R') direction = 'L'; break;
            case KeyEvent.VK_RIGHT: if (direction != 'L') direction = 'R'; break;
            case KeyEvent.VK_UP: if (direction != 'D') direction = 'U'; break;
            case KeyEvent.VK_DOWN: if (direction != 'U') direction = 'D'; break;
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    // 🔊 Sound
    private void playSound(String fileName) {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(fileName));
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception e) {
            System.out.println("Sound error");
        }
    }

    // 🏆 High Score
    private void loadHighScore() {
        try {
            File file = new File("highscore.txt");
            if (!file.exists()) file.createNewFile();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            if (line != null) highScore = Integer.parseInt(line);
            br.close();
        } catch (Exception e) {
            highScore = 0;
        }
    }

    private void updateHighScore() {
        if (applesEaten > highScore) {
            highScore = applesEaten;
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter("highscore.txt"));
                bw.write(String.valueOf(highScore));
                bw.close();
            } catch (IOException e) {}
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        SnakeGame game = new SnakeGame();
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}
