import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GRect;
import acm.program.GraphicsProgram;
import acm.util.MediaTools;
import acm.util.RandomGenerator;

import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.MouseEvent;

public class BreakoutExtended2 extends GraphicsProgram {

    /**
     * Width and height of application window in pixels
     */
    public static final int APPLICATION_WIDTH = 500;
    public static final int APPLICATION_HEIGHT = 800;

    /**
     * Dimensions of game board (usually the same)
     */
    private static final int WIDTH = APPLICATION_WIDTH;
    private static final int HEIGHT = APPLICATION_HEIGHT;

    /**
     * Dimensions of the paddle
     */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /**
     * Offset of the paddle up from the bottom
     */
    private static final int PADDLE_Y_OFFSET = 30;

    /**
     * Number of bricks per row
     */
    private static final int NBRICKS_PER_ROW = 10;

    /**
     * Number of rows of bricks
     */
    private static final int NBRICK_ROWS = 10;

    /**
     * Separation between bricks
     */
    private static final int BRICK_SEP = 4;

    /**
     * Width of a brick
     */
    private static final int BRICK_WIDTH = (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /**
     * Height of a brick
     */
    private static final int BRICK_HEIGHT = 8;

    /**
     * Radius of the ball in pixels
     */
    private static final int BALL_RADIUS = 10;

    /**
     * Offset of the top brick row from the top
     */
    private static final int BRICK_Y_OFFSET = 70;

    /**
     * Number of turns
     */
    private static final int NTURNS = 3;

    /* Method: run() */

    /**
     * Runs the Breakout program.
     */
    public void run() {
        addKeyListeners();
        addMouseListeners();
        draw();
        play();
    }

    // draws all of the objects
    private void draw() {
        drawPaddle();
        drawBricks();
        ball = drawBall(ball);
    }

    // Returns the center X coordinate
    private int getCenterX() {
        return WIDTH / 2;
    }
    // Returns the center Y coordinate

    private int getCenterY() {
        return HEIGHT / 2;
    }

    // draws the paddle
    private void drawPaddle() {
        paddle = drawBrick(getCenterX() - PADDLE_WIDTH / 2, getHeight() - PADDLE_Y_OFFSET, PADDLE_WIDTH, PADDLE_HEIGHT);
        fillBrick(paddle, Color.black);
    }

    // method for drawing the ball
    private Ball drawBall(Ball ball) {
        ball = new Ball(BALL_RADIUS);
        ball.setLocation(getCenterX() - BALL_RADIUS, getCenterY() - BALL_RADIUS);
        ball.setFilled(true);
        add(ball);
        return ball;
    }


    // Method for drawing the Bricks
    private void drawBricks() {

        for (int i = 0; i < NBRICK_ROWS; i++) {
            for (int j = 0; j < NBRICKS_PER_ROW; j++) {
                int startingPoint = (BRICK_SEP) / 2;
                GRect brick = drawBrick(startingPoint + j * (BRICK_WIDTH + BRICK_SEP),
                        BRICK_Y_OFFSET + i * (BRICK_SEP + BRICK_HEIGHT), BRICK_WIDTH, BRICK_HEIGHT);
                paintBricks(brick, i);
            }
        }
    }

    // This method makes creating new GRectangles easier,
    // as you dont need to write the add() method every time you create a rectangle instance
    private GRect drawBrick(double x, double y, double width, double height) {
        GRect rect = new GRect(x, y, width, height);
        add(rect);
        return rect;
    }


    /**
     * this method colorizes the bricks according to which row they belong to
     * it colorizes the row according to its remainder from division by 10.
     */
    private void paintBricks(GRect rectangle, int rowPosition) {
        if (rowPosition % 10 <= 1)
            fillBrick(rectangle, Color.RED);
        else if (rowPosition % 10 <= 3)
            fillBrick(rectangle, Color.ORANGE);
        else if (rowPosition % 10 <= 5)
            fillBrick(rectangle, Color.YELLOW);
        else if (rowPosition % 10 <= 7)
            fillBrick(rectangle, Color.GREEN);
        else if (rowPosition % 10 <= 9)
            fillBrick(rectangle, Color.CYAN);
    }

    // Method for reducing the lines of code
    private void fillBrick(GRect rect, Color color) {
        rect.setFillColor(color);
        rect.setFilled(true);
    }

    /**
     * Method for playing the game
     * At first the method randomizes the X axis velocity
     * it uses a while loop to make the ball move and check locations
     * the loop stops when the player has no lives left
     * or when the bricksDestroyed variable becomes equal to the amount of the bricks at the beginning
     */
    private void play() {
        vy = 4.0;
        vx = rgen.nextDouble(1.0, maxVx);
        if (rgen.nextBoolean(0.5))
            vx = -vx;
        while (lives > 0 && bricksDestroyed < NBRICK_ROWS * NBRICKS_PER_ROW) {
            showLives();
            drawPoints();
            moveBall();
            collisionLogic();
            pause(25);
            remove(livesLabel);
            remove(pointsLabel);
            //   System.out.println(collectedPoints);
        }
        if (lives == 0)
            loseMessage();
        if (bricksDestroyed == NBRICK_ROWS * NBRICKS_PER_ROW)
            winMessage();
        remove(ball);
    }

    // Method for handling the ball movement, it makes the ball move on the both axis by their respective velocities,
    // The method also handles the wall collision logic
    private void moveBall() {
        ball.move(vx, vy);
        if ((ball.getY()) <= 0) {
            vy = -vy;
            bounceClip.play();
        }

        if ((ball.getX() + diameter) >= WIDTH) {
            vx = -vx;
            bounceClip.play();
        }
        if (ball.getX() <= 0) {
            vx = -vx;
            bounceClip.play();

        }
        if (ball.getY() >= paddle.getY() + paddle.getHeight() + 20) {
            roundLost();
        }
    }

    // Method that handles losing a life. it teleports the ball to the middle
    //  gives it a random velocity and also subtracts 1 from the lives variable.
    private void roundLost() {
        ball.setLocation(getCenterX() - diameter / 2, getCenterY() - diameter / 2);
        vx = rgen.nextDouble(1.0, 3.0);

        if (rgen.nextBoolean(0.5))
            vx = -vx;

        if (vy < 0)
            vy = -vy;
        lives--;

    }

    /**
     * Implements the Collisions. if the collisionObject is the paddle,
     * the method makes sure that the ball does not get stuck in the paddle.
     * If the collisionObject is a brick, it removes the brick from the frame and increments the bricksDestroyed variable
     */
    private void collisionLogic() {

        getCollisionObject();
        if (collisionObject != null && collisionObject != livesLabel && collisionObject != pointsLabel) {
            vy = -vy;
            bounceClip.play();
            if (collisionObject == paddle) {
                paddleBounce();
                if (vy > 0)
                    vy = -vy;

            } else {
                int random = rgen.nextInt(0, 100);
                if (random == 20 || random == 27 || random == 65) {
                    System.out.println(random);
                    alertClip.play();
                    lives = lives + 1;
                }
                if (random == 13) {
                    collectedPoints = collectedPoints + 2000;
                    alertClip2.play();
                }

                increaseVelocities();
                collectPoints();
                remove(collisionObject);
                bricksDestroyed++;
            }
        }
    }
    // Increases both velocities. also increases the maximum vx velocity;
    private void increaseVelocities() {
        vy = VELOCITY_MULTIPLIER * vy;
        vx = VELOCITY_MULTIPLIER * vx;
        maxVx = VELOCITY_MULTIPLIER * maxVx;
    }
    /*
     this method checks the 4 edges of the ball bounding square to check if they touch a brick or a paddle
     method returns the collisionObject instance variable.
     if the 4 edges touch nothing, then the method returns null
     as the method is called multiple times, we need to make the collisionObject variable null at the beginning
     */
    private void getCollisionObject() {
        collisionObject = null;
        //  && getElementAt(ball.getX(), ball.getY()) != racket
        if (getElementAt(ball.getX(), ball.getY()) != null) {
            collisionObject = getElementAt(ball.getX(), ball.getY());
        }
        if (getElementAt(ball.getX() + diameter, ball.getY() + diameter) != null)
            collisionObject = getElementAt(ball.getX() + diameter, ball.getY() + diameter);
        //  && getElementAt(ball.getX() + diameter, ball.getY()) != racket
        if (getElementAt(ball.getX() + diameter, ball.getY()) != null)
            collisionObject = getElementAt(ball.getX() + diameter, ball.getY());

        if (getElementAt(ball.getX(), ball.getY() + diameter) != null)
            collisionObject = getElementAt(ball.getX(), ball.getY() + diameter);
    }

    // this method makes the paddle follow the mouse
    public void mouseMoved(MouseEvent e) {
        double y = paddle.getY();
        paddle.setLocation(e.getX() - PADDLE_WIDTH / 2, y);
        if ((e.getX() + PADDLE_WIDTH / 2) > WIDTH)
            paddle.setLocation(WIDTH - PADDLE_WIDTH, y);
        if ((e.getX() - PADDLE_WIDTH / 2) < 0)
            paddle.setLocation(0, y);

    }

    /**
     * Implements the controlled bouncing mechanism
     * ball will bounce differently depending on the position where it touched the paddle
     * bouncing differently just means changing the vx velocity
     * during this operation, total velocity stays the same, so vy has to also change.
     */
    private void paddleBounce() {
        // total velocity, calculated by taking the square root of the sum of squares of the vx and vy velocities
        double totalVelocity = Math.sqrt(vx * vx + vy * vy);

        double paddleHalf = paddle.getWidth() / 2;

        double ballCenterX = ball.getX() + BALL_RADIUS;

        double paddleCenterX = paddle.getX() + paddleHalf;

        double angle = (paddleCenterX - ballCenterX) / (paddleHalf + BALL_RADIUS);

        vx = -1 * maxVx * angle;

        vy = Math.sqrt(totalVelocity * totalVelocity - vx * vx);
    }


    // collects points based on which row the brick belongs to
    private void collectPoints() {
        // the upper Y coordinate of the last row bricks;
        double distance = (NBRICK_ROWS - 1) * (BRICK_SEP + BRICK_HEIGHT);
        double y = collisionObject.getY() - BRICK_Y_OFFSET;
        // determines which row the brick belongs to and increments the collectedPoints variable based on the row position
        // every upper row brick gives 5 more points
        collectedPoints = collectedPoints + (5 * (((distance - y) / (BRICK_HEIGHT + BRICK_SEP)) + 1));
    }

    // shows the amount of points player has collected
    private void drawPoints() {
        pointsLabel = new GLabel("Points: " + collectedPoints);
        pointsLabel.setFont(new Font("Points" + collectedPoints, Font.ITALIC, 20));
        pointsLabel.sendToFront();
        pointsLabel.setColor(Color.BLACK);
        double x = pointsLabel.getBounds().getWidth();
        double y = pointsLabel.getBounds().getHeight();
        pointsLabel.setLocation(0, y);
        pointsLabel.setVisible(true);
        add(pointsLabel);
    }
    // shows the amount of lives player has left
    private void showLives() {
        livesLabel = new GLabel("Lives left: " + lives);
        livesLabel.setFont(new Font("Lives left" + lives, Font.ITALIC, 20));
        livesLabel.sendToFront();
        livesLabel.setColor(Color.BLACK);
        double x = livesLabel.getBounds().getWidth();
        double y = livesLabel.getBounds().getHeight();
        livesLabel.setLocation(WIDTH - x - 20, y);
        livesLabel.setVisible(true);
        add(livesLabel);
    }
    // displays the losing message
    private void loseMessage() {
        loseMessage = new GLabel("game over", getCenterX(), getCenterY());
        loseMessage.setFont(new Font("Game Over", Font.ITALIC, 80));
        loseMessage.setLocation(getCenterX() - loseMessage.getWidth() / 2, getCenterY());
        drawBrick(loseMessage.getX() - 10, getCenterY() - loseMessage.getHeight() / 2 - 20, loseMessage.getWidth() + 20, loseMessage.getHeight());
        add(loseMessage);
    }
    // displays the winning message
    private void winMessage() {
        winMessage = new GLabel("Congratulations!", getCenterX(), getCenterY());
        winMessage.setFont(new Font("Congratulations!", Font.ITALIC, 60));
        winMessage.setLocation(getCenterX() - winMessage.getWidth() / 2, getCenterY());
        drawBrick(winMessage.getX() - 10, getCenterY() - winMessage.getHeight() / 2 - 20, winMessage.getWidth() + 20, winMessage.getHeight());
        add(winMessage);
    }


    // variable that increments as the ball hits the bricks
    private int bricksDestroyed = 0;
    // private GLabel label;
    private GObject collisionObject = null;

    private final double VELOCITY_MULTIPLIER = 1.008;

    private GRect paddle;

    private Ball ball;

    private int lives = NTURNS;

    private double collectedPoints = 0;

    private GLabel livesLabel;

    private GLabel winMessage;

    private GLabel loseMessage;

    private GLabel pointsLabel;

    private double vx, vy;

    // maximum value of vx velocity. used for the bouncing mechanism
    private double maxVx = 3.0;

    private int diameter = 2 * BALL_RADIUS;

    private final RandomGenerator rgen = RandomGenerator.getInstance();

    AudioClip bounceClip = MediaTools.loadAudioClip("bounce.au");

    AudioClip alertClip = MediaTools.loadAudioClip("alert.au");

    AudioClip alertClip2 = MediaTools.loadAudioClip("alert2.au");

}

