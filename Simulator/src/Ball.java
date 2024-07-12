import java.awt.Graphics;

public class Ball {
    private double x, y, vx, vy;
    private static final int SIZE = 10; // Ball size

    public Ball(int x, int y, double vx, double vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    public void update(int canvasWidth, int canvasHeight) {
        x += vx;
        y += vy;

        // Bounce off the edges of the canvas
        if (x <= SIZE / 2 || x >= canvasWidth - SIZE / 2) {
            vx *= -1;
        }
        if (y <= SIZE / 2 || y >= canvasHeight - SIZE / 2) {
            vy *= -1;
        }

        // Ensure the ball stays within the bounds
        x = Math.max(SIZE / 2, Math.min(canvasWidth - SIZE / 2, x));
        y = Math.max(SIZE / 2, Math.min(canvasHeight - SIZE / 2, y));
    }

    public void draw(Graphics g, int canvasHeight) {
        int drawX = (int) (x - SIZE / 2);
        int drawY = (int) (y - SIZE / 2);
        g.fillOval(drawX, drawY, SIZE, SIZE);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
