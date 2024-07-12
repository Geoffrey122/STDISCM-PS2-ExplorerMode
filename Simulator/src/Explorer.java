import java.awt.Color;
import java.awt.Graphics;

public class Explorer {
    private double x, y;
    private double vx, vy;
    private static final int SIZE = 10; // Adjusted size for a smaller sprite

    public Explorer(int x, int y) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
    }

    public void update(int canvasWidth, int canvasHeight) {
        x += vx;
        y += vy;

        if (x < 0) x = 0;
        if (x > canvasWidth - SIZE) x = canvasWidth - SIZE;
        if (y < 0) y = 0;
        if (y > canvasHeight - SIZE) y = canvasHeight - SIZE;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillOval((int) x, (int) y, SIZE, SIZE);
    }

    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    public double getVelocityX() {
        return vx;
    }

    public double getVelocityY() {
        return vy;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
