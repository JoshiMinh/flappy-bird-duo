import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class FlappyBirdDuoGame extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private int birdX, birdY, bird2X, bird2Y;
    private int birdVelocity, birdVelocityX, bird2Velocity, bird2VelocityX, hit; // Added velocities for second bird
    private int rotationAngle, obstacleCount = 0, difficulty = 1;
    private List<Rectangle> obstacles = new ArrayList<>();
    private Random random = new Random();
    private float gameVol = 0.8f;
    private boolean endGame = false, startGame = false, Down = true, SHIFTED = false;

    // Game settings
    private int Tick, space, distance, velocity, gravity;

    // Game assets
    private ImageIcon base, deadBird, flappyBirdIcon, flappyBird2Icon, backgroundImage, upperPipeIcon, lowerPipeIcon;

    public static void main(String[] args) {
        // Setup main game window
        JFrame frame = new JFrame("Flappy Bird Duo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(new ImageIcon("Images/Flappy_Bird_Duo_icon.png").getImage());
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.add(new FlappyBirdDuoGame());
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    public FlappyBirdDuoGame() {
        setDifficulty(1); // Initialize difficulty

        // Load and scale images
        flappyBirdIcon = new ImageIcon("Images/bird.png");
        flappyBird2Icon = new ImageIcon("Images/bird2.png"); // Load second bird image
        backgroundImage = new ImageIcon("Images/background.png");
        upperPipeIcon = new ImageIcon("Images/obsdown.png");
        lowerPipeIcon = new ImageIcon("Images/obs.png");
        base = new ImageIcon("Images/base.png");
        deadBird = new ImageIcon("Images/dead_bird.png");

        // Scale images
        backgroundImage = new ImageIcon(backgroundImage.getImage().getScaledInstance(800, 600, Image.SCALE_DEFAULT));
        flappyBirdIcon = new ImageIcon(flappyBirdIcon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
        flappyBird2Icon = new ImageIcon(flappyBird2Icon.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT)); // Scale second bird
        base = new ImageIcon(base.getImage().getScaledInstance(800, 100, Image.SCALE_DEFAULT));

        // Initialize game variables
        birdX = 200;
        birdY = 250;
        bird2X = 600; // Initial position for second bird
        bird2Y = 250;
        birdVelocity = birdVelocityX = 0;
        bird2Velocity = bird2VelocityX = 0; // Initialize second bird's velocities
        rotationAngle = 0;
        obstacles = new ArrayList<>();
        random = new Random();

        // Setup game
        addKeyListener(this);
        setFocusable(true);

        timer = new Timer(150, this);
        timer.start();

        generateObstacle(); // Create the first obstacle
    }

    public void setDifficulty(int difficulty) {
        // Set parameters based on difficulty level
        switch (difficulty) {
            case 1:
                Tick = 16; space = 190; distance = 530; velocity = 4; gravity = 1; break;
            case 2:
                Tick = 12; space = 150; distance = 470; velocity = 6; gravity = 1; break;
            case 3:
                Tick = 8; space = 110; distance = 410; velocity = 8; gravity = 2; break;
            default:
                throw new IllegalArgumentException("Invalid difficulty level");
        }
    }

    public void playSound(String soundFilePath, float volume) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream(soundFilePath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);
            }

            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateObstacle() {
        int space = this.space; // Space between upper and lower obstacles
        int width = 60;
        int height = random.nextInt(300) + 50; // Random height for lower obstacle
    
        // Add upper and lower obstacles
        obstacles.add(new Rectangle(800, 0, width, height)); // Upper obstacle
        obstacles.add(new Rectangle(800, height + space, width, 600 - height - space)); // Lower obstacle
    
        obstacleCount++;
    
        // Increase difficulty every 10 obstacles
        if (obstacleCount % 10 == 0 && difficulty < 4) {
            setDifficulty(difficulty);
            playSound("Sounds/point.wav", gameVol);
            difficulty++;
        }
    }    

    public void move() {
        if (!endGame && startGame) {
            // Apply gravity to both birds
            if (!SHIFTED) {
                birdVelocityX += (birdVelocityX > 0) ? -gravity : gravity;
                bird2VelocityX += (bird2VelocityX > 0) ? -gravity : gravity; // Second bird's velocity adjustment
            }
            birdVelocity += gravity;
            bird2Velocity += gravity;
    
            birdY += birdVelocity;
            birdX += birdVelocityX;
            bird2Y += bird2Velocity; // Move second bird
            bird2X += bird2VelocityX; // Move second bird
    
            // Move obstacles
            for (Rectangle obstacle : obstacles) {
                obstacle.x -= velocity;
            }
    
            // Check for collisions for both birds
            Rectangle bird1 = new Rectangle(birdX, birdY, 50, 40);
            Rectangle bird2 = new Rectangle(bird2X, bird2Y, 50, 40); // Second bird's rectangle
            for (Rectangle obstacle : obstacles) {
                if (obstacle.intersects(bird1) || obstacle.intersects(bird2) || 
                    birdY < 0 || birdY > 475 || birdX < 0 || birdX > 800 || 
                    bird2Y < 0 || bird2Y > 475 || bird2X < 0 || bird2X > 800) { // Check for bird-to-bird collision
                    playSound("Sounds/bird-hit.wav", gameVol);
                    endGame = true;
                    return;
                }
                else if(bird1.intersects(bird2)) { // Check for bird-to-bird collision
                    playSound("Sounds/bird-hit.wav", gameVol);
                    hit = 15;
                    if (Math.abs(birdVelocityX) < Math.abs(bird2VelocityX)) {
                        birdVelocityX += (birdX > bird2X) ? hit : -hit;
                    } 
                    
                    else if (Math.abs(birdVelocityX) > Math.abs(bird2VelocityX)) {
                        bird2VelocityX += (bird2X > birdX) ? hit : -hit;
                    }
                    return;
                }
            }
    
            // Generate new obstacles
            if (obstacles.get(obstacles.size() - 1).x < distance) {
                generateObstacle();
            }
    
            // Remove off-screen obstacles
            obstacles.removeIf(obstacle -> obstacle.x + obstacle.width < 0);
    
        } else if (!endGame && !startGame) {
            // Apply gravity to both birds in a different way when the game isn't started or ended
            birdY += (Down) ? 10 : -10;
            bird2Y += (Down) ? 10 : -10; // Apply gravity effect to second bird
            Down = !Down;
    
            // Move obstacles (no movement when the game isn't started)
            obstacles.forEach(obstacle -> obstacle.x -= 0);
    
        } else if (endGame && startGame) {
            // Apply gravity to both birds during end game
            birdVelocity += gravity;
            birdVelocityX = 0;
            birdY += birdVelocity;
            birdX += birdVelocityX;
    
            bird2Velocity += gravity; // Gravity for second bird
            bird2VelocityX = 0;
            bird2Y += bird2Velocity;
            bird2X += bird2VelocityX;
    
            // Move obstacles (no movement during end game)
            for (Rectangle obstacle : obstacles) {
                obstacle.x -= 0;
                if (birdY > 475 || bird2Y > 475) { // Check for both birds
                    gameOver();
                }
            }
    
            // Generate new obstacles
            if (obstacles.get(obstacles.size() - 1).x < distance) {
                generateObstacle();
            }
    
            // Remove off-screen obstacles
            obstacles.removeIf(obstacle -> obstacle.x + obstacle.width < 0);
        }
    }    

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw background image
        backgroundImage.paintIcon(this, g, 0, 0);
    
        // Draw obstacles
        for (Rectangle obstacle : obstacles) {
            int pipeSpace = space;
            // Draw upper and lower pipes
            g.drawImage(upperPipeIcon.getImage(), obstacle.x, obstacle.y, obstacle.width, obstacle.height, this);
            g.drawImage(lowerPipeIcon.getImage(), obstacle.x, obstacle.y + obstacle.height + pipeSpace, obstacle.width, obstacle.height, this);
        }
    
        // Draw ground
        base.paintIcon(this, g, 0, 520);
    
        // Rotate and draw first Flappy Bird image
        Graphics2D g2d = (Graphics2D) g;
        g2d.rotate(Math.toRadians(rotationAngle), birdX + 20, birdY + 22);
        flappyBirdIcon.paintIcon(this, g2d, birdX, birdY);
        g2d.rotate(-Math.toRadians(rotationAngle), birdX + 20, birdY + 22); // Reset rotation
    
        // Draw second Flappy Bird image (no rotation needed if similar behavior)
        flappyBird2Icon.paintIcon(this, g, bird2X, bird2Y);
    
        // Draw start button if the game hasn't started or ended
        if (!endGame && !startGame) {
            ImageIcon startButtonIcon = new ImageIcon("Images/StartButton.png");
            startButtonIcon = new ImageIcon(startButtonIcon.getImage().getScaledInstance(150, 60, Image.SCALE_DEFAULT));
    
            int centerX = (getWidth() - startButtonIcon.getIconWidth()) / 2;
            startButtonIcon.paintIcon(this, g, centerX, 400);
        }
    }    
    
    public void actionPerformed(ActionEvent e) {
        move();    // Move game elements
        repaint(); // Refresh screen
    }    
    
    public void keyPressed(KeyEvent e) {
        if (!endGame) {
            int keyCode = e.getKeyCode();
            switch (keyCode) {
                // Controls for the first bird
                case KeyEvent.VK_SPACE:
                    startGame = true;
                    timer.setDelay(Tick);
                    break;
                case KeyEvent.VK_W:
                    birdVelocity = -13;
                    rotationAngle = -20;
                    playSound("Sounds/flap.wav", gameVol);
                    break;
                case KeyEvent.VK_S:
                    birdVelocity = 10;
                    rotationAngle = 20;
                    playSound("Sounds/flap.wav", gameVol);
                    break;
                case KeyEvent.VK_D:
                    birdVelocityX = 15;
                    rotationAngle = -20;
                    playSound("Sounds/flap.wav", gameVol);
                    break;
                case KeyEvent.VK_A:
                    birdVelocityX = -15;
                    rotationAngle = -20;
                    playSound("Sounds/flap.wav", gameVol);
                    break;
                case KeyEvent.VK_SHIFT:
                    SHIFTED = true;
                    break;
        
                // Controls for the second bird
                case KeyEvent.VK_UP:
                    bird2Velocity = -13;
                    playSound("Sounds/flap.wav", gameVol);
                    break;
                case KeyEvent.VK_DOWN:
                    bird2Velocity = 10;
                    playSound("Sounds/flap.wav", gameVol);
                    break;
                case KeyEvent.VK_RIGHT:
                    bird2VelocityX = 15;
                    playSound("Sounds/flap.wav", gameVol);
                    break;
                case KeyEvent.VK_LEFT:
                    bird2VelocityX = -15;
                    playSound("Sounds/flap.wav", gameVol);
                    break;
            }
        }
    
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            timer.stop();
            
            ImageIcon pauseIcon = new ImageIcon("Images/pause.png");
            pauseIcon = new ImageIcon(pauseIcon.getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT));
            
            // Pause dialog with three options
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Game Paused",
                    "Pause",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    pauseIcon,
                    new Object[]{"Continue", "Restart", "Quit"},
                    "Continue"
            );
    
            // Handle user choice
            if (choice == JOptionPane.YES_OPTION) {
                timer.start();
            } else if (choice == JOptionPane.NO_OPTION) {
                restartGame();
            } else {
                System.exit(0);
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
    
        // Common action for all birds
        if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
            rotationAngle = 30; // Set rotation angle when Space, W, or Up is released
        }
    
        // Reset flags and velocities
        if (keyCode == KeyEvent.VK_SHIFT) {
            SHIFTED = false; // Reset SHIFTED flag
        }
    }
    
    public void keyTyped(KeyEvent e) {} // No action required     

    public void gameOver() {
        timer.stop(); // Stop the game timer
    
        // Resize the dead bird image
        deadBird = new ImageIcon(deadBird.getImage().getScaledInstance(45, 45, Image.SCALE_DEFAULT));
    
        // Display game over dialog
        int choice = JOptionPane.showOptionDialog(
                this,
                "You Lose",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                deadBird,
                new Object[]{"Retry", "Quit"},
                "Retry"
        );
    
        // Handle the user's choice
        if (choice == JOptionPane.YES_OPTION) {
            restartGame(); // Restart the game
        } else {
            System.exit(0); // Exit the game
        }
    }    

    public void restartGame() {
        // Reset game variables for the first bird
        birdX = 200;
        birdY = 250;
        birdVelocity = birdVelocityX = 0;
        
        // Reset game variables for the second bird
        bird2X = 600; // Reset position for second bird
        bird2Y = 250;
        bird2Velocity = bird2VelocityX = 0;
    
        rotationAngle = 0;
        difficulty = 1;
        obstacleCount = 0;
        startGame = endGame = false;
    
        // Reset difficulty and obstacles
        setDifficulty(1);
        obstacles.clear();
        generateObstacle();
    
        // Restart game timer
        timer.setDelay(150);
        timer.start();
    }    
}