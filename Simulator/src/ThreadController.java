import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

public class ThreadController {
    private List<ParticleProcessor> processors = new CopyOnWriteArrayList<>();
    private ForkJoinPool executorService = new ForkJoinPool();
    private int canvasWidth, canvasHeight;
    private int particleSize = 0;

    private long lastAverageProcessingTime = 0;
    private List<Long> processingTimesHistory = new ArrayList<>();
    private static final int PROCESSING_TIME_HISTORY_SIZE = 20;

    private int lastParticleSizeAtThreadAddition = 0;
    private boolean isPaused = false;

    private Controller explorerController;

    public ThreadController() {
        prewarmThreads();
    }

    public void setCanvasSize(int canvasWidth, int canvasHeight) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        addProcessor();
    }

    private void addProcessor() {
        ParticleProcessor processor = new ParticleProcessor(canvasWidth, canvasHeight);
        processors.add(processor);
        executorService.execute(processor);
        lastParticleSizeAtThreadAddition = particleSize;
    }

    private void addProcessor(List<Ball> particles) {
        ParticleProcessor processor = new ParticleProcessor(canvasWidth, canvasHeight, particles);
        processors.add(processor);
        executorService.execute(processor);
        lastParticleSizeAtThreadAddition = particleSize;
    }

    public void checkAndAdjustThread() {
        if (shouldAddThread()) {
            redistributeParticles();
        }
    }

    private boolean shouldAddThread() {
        if (processingTimesHistory.isEmpty()) {
            return false;
        }

        long currentAverageProcessingTime = processingTimesHistory.get(processingTimesHistory.size() - 1);
        boolean processingTimeIncreasing = currentAverageProcessingTime > lastAverageProcessingTime;
        boolean significantParticleIncrease = particleSize >= lastParticleSizeAtThreadAddition * 1.10;

        return processingTimeIncreasing &&
            processors.size() < Runtime.getRuntime().availableProcessors() &&
            significantParticleIncrease;
    }

    public void addParticle(Ball particle) {
        particleSize++;

        ParticleProcessor leastLoadedProcessor = processors.stream()
            .min((p1, p2) -> Integer.compare(p1.getParticleCount(), p2.getParticleCount()))
            .orElseThrow(IllegalStateException::new);

        leastLoadedProcessor.addParticle(particle);
    }

    public void addParticle(int x, int y, double angle, double velocity) {
        particleSize++;

        ParticleProcessor leastLoadedProcessor = processors.stream()
            .min((p1, p2) -> Integer.compare(p1.getParticleCount(), p2.getParticleCount()))
            .orElseThrow(IllegalStateException::new);

        leastLoadedProcessor.addParticle(new Ball(x, y, Math.cos(angle) * velocity, Math.sin(angle) * velocity));
    }

    public void addParticlesWithUniformDistance(int n, int x1, int y1, int x2, int y2, double angle, double velocity) {
        double deltaX = (x2 - x1) / (double) (n - 1);
        double deltaY = (y2 - y1) / (double) (n - 1);

        for (int i = 0; i < n; i++) {
            int x = x1 + (int) (i * deltaX);
            int y = y1 + (int) (i * deltaY);
            addParticle(x, y, angle, velocity);
        }
    }

    public void addParticlesWithUniformAngle(int n, int x, int y, double startTheta, double endTheta, double velocity) {
        double deltaTheta = (endTheta - startTheta) / (n - 1);

        for (int i = 0; i < n; i++) {
            double angle = startTheta + i * deltaTheta;
            addParticle(x, y, angle, velocity);
        }
    }

    public void addParticlesWithUniformVelocity(int n, int x, int y, double angle, double startVelocity, double endVelocity) {
        double deltaVelocity = (endVelocity - startVelocity) / (n - 1);

        for (int i = 0; i < n; i++) {
            double velocity = startVelocity + i * deltaVelocity;
            addParticle(x, y, angle, velocity);
        }
    }

    public void updateParticles() {
        if (!isPaused) {
            for (ParticleProcessor processor : processors) {
                processor.run(); // Synchronous execution to ensure immediate update
            }
        }
    }

    public void drawParticles(Graphics g, int canvasHeight) {
        for (ParticleProcessor processor : processors) {
            processor.getParticleController().drawParticles(g, canvasHeight);
        }
        if (explorerController != null) {
            explorerController.drawParticles(g, canvasHeight);
        }
    }

    public int getParticleSize() {
        return particleSize;
    }

    private void redistributeParticles() {
        int processorSize = processors.size();
        List<Ball> newParticles = new ArrayList<>();

        processors.parallelStream().forEach(processor -> {
            List<Ball> allParticles = processor.getParticleController().getParticles();
            int popCount = allParticles.size() / (processorSize + 1);
            List<Ball> particles = popItems(allParticles, popCount);
            synchronized (newParticles) {
                newParticles.addAll(particles);
            }
        });

        addProcessor(newParticles);
    }

    public static List<Ball> popItems(List<Ball> list, int numberOfItemsToPop) {
        List<Ball> poppedItems = new ArrayList<>();
        int size = list.size();
        int endIndex = Math.min(size, numberOfItemsToPop);
        for (int i = 0; i < endIndex; i++) {
            poppedItems.add(list.remove(0));
        }
        return poppedItems;
    }

    public void updateProcessingTimes() {
        long totalProcessingTime = 0;
        for (ParticleProcessor processor : processors) {
            totalProcessingTime += processor.getLastProcessingTime();
        }
        long currentAverageProcessingTime = totalProcessingTime / processors.size();

        processingTimesHistory.add(currentAverageProcessingTime);
        if (processingTimesHistory.size() > PROCESSING_TIME_HISTORY_SIZE) {
            processingTimesHistory.remove(0);
        }

        lastAverageProcessingTime = processingTimesHistory.stream().mapToLong(Long::longValue).sum() / processingTimesHistory.size();
    }

    private void prewarmThreads() {
        int prewarmTaskCount = Runtime.getRuntime().availableProcessors();
        List<Runnable> prewarmTasks = new ArrayList<>();
        for (int i = 0; i < prewarmTaskCount; i++) {
            prewarmTasks.add(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
            });
        }
        prewarmTasks.forEach(executorService::execute);
    }

    public void pauseParticles() {
        isPaused = true;
    }

    public void resumeParticles() {
        isPaused = false;
        updateParticles();
    }

    public void clearParticles() {
        processors.forEach(processor -> processor.getParticleController().clearParticles());
        particleSize = 0;
        if (explorerController != null) {
            explorerController.clearParticles();
        }
    }

    public void addExplorer(int x, int y) {
        if (explorerController == null) {
            explorerController = new Controller();
            explorerController.addExplorer(x, y);
        } else {
            Explorer explorer = explorerController.getExplorer();
            if (explorer == null) {
                explorerController.addExplorer(x, y);
            }
        }
    }

    public void removeExplorer() {
        explorerController = null;
    }

    public Explorer getExplorer() {
        if (explorerController != null) {
            return explorerController.getExplorer();
        }
        return null;
    }

    private class ParticleProcessor implements Runnable {
        private Controller particleController;
        private int canvasWidth, canvasHeight;
        private long lastProcessingTime = 0;

        public ParticleProcessor(int canvasWidth, int canvasHeight) {
            this.canvasWidth = canvasWidth;
            this.canvasHeight = canvasHeight;
            this.particleController = new Controller();
        }

        public ParticleProcessor(int canvasWidth, int canvasHeight, List<Ball> particles) {
            this.canvasWidth = canvasWidth;
            this.canvasHeight = canvasHeight;
            this.particleController = new Controller();
            for (Ball particle : particles) {
                this.particleController.addParticle(particle);
            }
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            particleController.updateParticles(canvasWidth, canvasHeight);
            long endTime = System.currentTimeMillis();
            lastProcessingTime = endTime - startTime;
        }

        public void addParticle(Ball particle) {
            particleController.addParticle(particle);
        }

        public Controller getParticleController() {
            return particleController;
        }

        public long getLastProcessingTime() {
            return lastProcessingTime;
        }

        public int getParticleCount() {
            return particleController.getParticles().size();
        }
    }

    public void drawParticlesInView(Graphics g, int canvasHeight, int viewLeft, int viewTop, int viewRight, int viewBottom) {
        for (ParticleProcessor processor : processors) {
            processor.getParticleController().drawParticlesInView(g, canvasHeight, viewLeft, viewTop, viewRight, viewBottom);
        }
        if (explorerController != null) {
            explorerController.drawParticlesInView(g, canvasHeight, viewLeft, viewTop, viewRight, viewBottom);
        }
    }
}
