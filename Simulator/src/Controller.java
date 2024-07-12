import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Controller {
    private List<Ball> particles = new CopyOnWriteArrayList<>();
    private Explorer explorer;

    public void addParticle(int x, int y, double angle, double velocity) {
        particles.add(new Ball(x, y, Math.cos(angle) * velocity, Math.sin(angle) * velocity));
    }

    public void addParticle(Ball particle) {
        particles.add(particle);
    }

    public void addExplorer(int x, int y) {
        explorer = new Explorer(x, y);
    }

    public Explorer getExplorer() {
        return explorer;
    }

    public void updateParticles(int canvasWidth, int canvasHeight) {
        for (Ball particle : particles) {
            particle.update(canvasWidth, canvasHeight);
        }
        if (explorer != null) {
            explorer.update(canvasWidth, canvasHeight);
        }
    }

    public List<Ball> getParticles() {
        return particles;
    }

    public void drawParticles(Graphics g, int canvasHeight) {
        for (Ball particle : particles) {
            particle.draw(g, canvasHeight);
        }
        if (explorer != null) {
            explorer.draw(g);
        }
    }

    public void clearParticles() {
        particles.clear();
        explorer = null;
    }

    public void drawParticlesInView(Graphics g, int canvasHeight, int viewLeft, int viewTop, int viewRight, int viewBottom) {
        for (Ball particle : particles) {
            if (particle.getX() >= viewLeft && particle.getX() <= viewRight && particle.getY() >= viewTop && particle.getY() <= viewBottom) {
                particle.draw(g, canvasHeight);
            }
        }
        if (explorer != null) {
            explorer.draw(g);
        }
    }
}
