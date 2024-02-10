import acm.graphics.GOval;
import acm.graphics.GRect;

import java.awt.*;

public class Ball extends GOval {
    public double vx;
    public double vy;
    public GOval ball;
    Ball(double radius) {
        super(2 * radius, 2 * radius);
        ball = new GOval(getWidth() - radius, getHeight() - radius, 2 * radius, 2 * radius);

    }
}
