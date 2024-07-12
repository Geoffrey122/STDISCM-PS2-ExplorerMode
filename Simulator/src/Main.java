import java.awt.*;
import javax.swing.*;

public class Main extends JFrame {
    private final MainScreenUI simulationPanel;

    public Main() {
        simulationPanel = new MainScreenUI();
        setupUI();
    }

    private void setupUI() {
        setTitle("Particle Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(simulationPanel);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public MainScreenUI getSimulationPanel() {
        return simulationPanel;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Main simulatorFrame = new Main();
            simulatorFrame.setVisible(true);
            MainScreenUI simPanel = simulatorFrame.getSimulationPanel();
            if (simPanel != null) {
                InputSelection inputDialog = new InputSelection(simPanel.getDynamicThreadManager());
                inputDialog.setVisible(true);
            } else {
                System.out.println("Simulation Panel is null.");
            }
        });
    }    
}
