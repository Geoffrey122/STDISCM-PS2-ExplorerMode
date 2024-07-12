import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class MainScreenUI extends JPanel {
    private final DrawPanel drawPanel;
    private final ThreadController threadManager;
    private Thread gameThread;
    private volatile boolean running = false;
    private FPS trackFPS = new FPS();
    private boolean explorerMode = false;

    public MainScreenUI() {
        drawPanel = new DrawPanel();

        setLayout(new BorderLayout());
        add(drawPanel, BorderLayout.CENTER);
        threadManager = new ThreadController();

        drawPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                drawPanel.removeComponentListener(this);
                startGameLoop();
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        });
    }

    private void handleKeyPress(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_E) {
            toggleExplorerMode();
        } else if (explorerMode) {
            Explorer explorer = threadManager.getExplorer();
            if (explorer != null) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        explorer.setVelocity(0, -5);
                        break;
                    case KeyEvent.VK_DOWN:
                        explorer.setVelocity(0, 5);
                        break;
                    case KeyEvent.VK_LEFT:
                        explorer.setVelocity(-5, 0);
                        break;
                    case KeyEvent.VK_RIGHT:
                        explorer.setVelocity(5, 0);
                        break;
                }
            }
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        if (explorerMode) {
            Explorer explorer = threadManager.getExplorer();
            if (explorer != null) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                        if (explorer.getVelocityX() == 0) explorer.setVelocity(0, 0);
                        else explorer.setVelocity(explorer.getVelocityX(), 0);
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT:
                        if (explorer.getVelocityY() == 0) explorer.setVelocity(0, 0);
                        else explorer.setVelocity(0, explorer.getVelocityY());
                        break;
                }
            }
        }
    }

    public void startGameLoop() {
        threadManager.setCanvasSize(drawPanel.getWidth(), drawPanel.getHeight());
        running = true;
        gameThread = new Thread(this::gameLoop);
        gameThread.start();
    }

    private void gameLoop() {
        final long targetDelay = 1000 / 60;
        long lastFpsDisplayTime = System.currentTimeMillis();

        while (running) {
            long now = System.currentTimeMillis();

            trackFPS.update();
            if (now - lastFpsDisplayTime >= 500 && trackFPS.getFPS() != 0) {
                drawPanel.setFps(trackFPS.getFPS());
                threadManager.checkAndAdjustThread();
                lastFpsDisplayTime = now;
            }

            updateAndRepaint();
            threadManager.updateProcessingTimes();

            long sleepTime = targetDelay - (System.currentTimeMillis() - now);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    running = false;
                }
            }
        }
    }

    private void updateAndRepaint() {
        threadManager.updateParticles();
        Explorer explorer = threadManager.getExplorer();
        if (explorer != null) {
            explorer.update(drawPanel.getWidth(), drawPanel.getHeight());
        }
        SwingUtilities.invokeLater(drawPanel::repaint);
    }

    public void stopGameLoop() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public ThreadController getDynamicThreadManager() {
        return threadManager;
    }

    public void toggleExplorerMode() {
        explorerMode = !explorerMode;
        System.out.println("Explorer mode toggled: " + explorerMode);

        if (explorerMode) {
            if (threadManager.getExplorer() == null) {
                threadManager.addExplorer(640, 360); // Initial starting point
                System.out.println("Explorer added at (640, 360)");
            }
        } else {
            System.out.println("Explorer mode off, retaining explorer position.");
        }
    }

    private class DrawPanel extends JPanel {
        private double fpsToDisplay = 0;

        public void setFps(double fps) {
            this.fpsToDisplay = fps;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1280, 720);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public DrawPanel() {
            super();
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.BLACK);

            if (explorerMode) {
                drawExplorerMode((Graphics2D) g);
            } else {
                drawDeveloperMode(g);
            }

            drawHUD(g);
        }

        private void drawExplorerMode(Graphics2D g2d) {
            Explorer explorer = threadManager.getExplorer();
            Color saiyanBlue = new Color(173, 216, 230, 255);
            g2d.setColor(saiyanBlue);
            if (explorer != null) {
                int explorerX = (int) explorer.getX();
                int explorerY = (int) explorer.getY();
        
                int viewWidth = getWidth() / 2; // Half the width of the panel
                int viewHeight = getHeight() / 2; // Half the height of the panel
        
                int halfWidth = viewWidth / 2;
                int halfHeight = viewHeight / 2;
        
                // Calculate viewport dynamically
                int left = Math.max(0, explorerX - halfWidth);
                int top = Math.max(0, explorerY - halfHeight);
                int right = Math.min(getWidth(), explorerX + halfWidth);
                int bottom = Math.min(getHeight(), explorerY + halfHeight);
        
                // Determine dynamic scaling based on explorer position
                double scale = 1.0; // Start with no scaling
                if (explorerX < 100 || explorerX > getWidth() - 100 || explorerY < 100 || explorerY > getHeight() - 100) {
                    scale = 1.5; // Less zoom when near edges
                } else {
                    scale = 2.0; // More zoom when away from edges
                }
        
                // Apply scaling and translation
                g2d.scale(scale, scale);
                g2d.translate(-left / scale, -top / scale);
                threadManager.drawParticlesInView(g2d, getHeight(), left, top, right, bottom);
                explorer.draw(g2d); // Draw the explorer
                g2d.translate(left / scale, top / scale);
                g2d.scale(1 / scale, 1 / scale);
            }
        }

        private void drawDeveloperMode(Graphics g) {
            Color saiyanBlue = new Color(173, 216, 230, 255);
            g.setColor(saiyanBlue);
            threadManager.drawParticles(g, getHeight());

            Font counterFont = new Font("Tahoma", Font.BOLD, 14);
            g.setFont(counterFont);

            int yOffset = getHeight() - 30;
            if (fpsToDisplay >= 60) {
                g.setColor(Color.GREEN);
            } else if (fpsToDisplay >= 50) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.RED);
            }
            g.drawString(String.format("FPS: %.2f", fpsToDisplay), 10, yOffset);

            String particlesText = String.format("Particles: %d", threadManager.getParticleSize());
            g.setColor(Color.BLACK);
            g.drawString(particlesText, 151, yOffset + 1);
            g.setColor(new Color(255, 215, 0));
            g.drawString(particlesText, 150, yOffset);
        }

        private void drawHUD(Graphics g) {
            Color saiyanBlue = new Color(173, 216, 230, 255);
            g.setColor(saiyanBlue);
            g.setFont(new Font("Tahoma", Font.BOLD, 14));
            String modeText = explorerMode ? "Mode: Explorer" : "Mode: Developer";
            g.drawString(modeText, 10, 20);

            // Instruction text
            g.drawString("Press E to Switch Mode", getWidth() - 220, 20); // Top right position

            // Movement keys info
            g.drawString("Move with Arrow Keys", getWidth() / 2 - 110, getHeight() - 20); // Bottom center


            int yOffset = getHeight() - 30;
            if (fpsToDisplay >= 60) {
                g.setColor(Color.GREEN);
            } else if (fpsToDisplay >= 50) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.RED);
            }
            g.drawString(String.format("FPS: %.2f", fpsToDisplay), 10, yOffset);

            String particlesText = String.format("Particles: %d", threadManager.getParticleSize());
            g.setColor(Color.BLACK);
            g.drawString(particlesText, 151, yOffset + 1);
            g.setColor(new Color(255, 215, 0));
            g.drawString(particlesText, 150, yOffset);

            if (explorerMode) {
                Explorer explorer = threadManager.getExplorer();
                if (explorer != null) {
                    String positionText = String.format("Explorer Position: (%.2f, %.2f)", explorer.getX(), explorer.getY());
                    g.drawString(positionText, 10, yOffset - 20);
                }
            }
        }
    }
}
