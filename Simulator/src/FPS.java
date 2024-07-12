public class FPS {
    private long lastTimeCheck; 
    private int frameCount; 
    private int fps; 

    public FPS() {
        lastTimeCheck = System.currentTimeMillis();
        frameCount = 0;
        fps = 0;
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastTimeCheck;

        frameCount++;

        if (elapsedTime >= 500) {
            fps = frameCount * 2;
            frameCount = 0; 
            lastTimeCheck = currentTime;
        }
    }

    public int getFPS() {
        return fps;
    }
}
