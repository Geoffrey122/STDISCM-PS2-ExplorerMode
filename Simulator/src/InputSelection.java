import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class InputSelection extends JFrame {
    private ThreadController threadManager;
    private JLabel feedbackLabel;
    private List<String> feedbackMessages = new ArrayList<>();
    private JTextField secondXInput, secondYInput, secondAngleInput, secondVelocityInput; // Global for access
    private JButton pauseButton;
    private boolean isPaused = false;

    public InputSelection(ThreadController threadManager) {
        this.threadManager = threadManager;
        setTitle("Particle Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        feedbackLabel = new JLabel("<html></html>");
        feedbackLabel.setHorizontalAlignment(JLabel.CENTER);
        feedbackLabel.setForeground(Color.BLUE);

        JScrollPane feedbackScrollPane = new JScrollPane(feedbackLabel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        feedbackScrollPane.setPreferredSize(new Dimension(800, 150));
        feedbackScrollPane.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45), 2));

        JLabel feedbackTitle = new JLabel("Result");
        feedbackTitle.setHorizontalAlignment(JLabel.CENTER);
        feedbackTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        feedbackTitle.setOpaque(true);
        feedbackTitle.setBackground(new Color(45, 45, 45));
        feedbackTitle.setForeground(Color.WHITE);

        JPanel feedbackPanel = new JPanel(new BorderLayout());
        feedbackPanel.add(feedbackTitle, BorderLayout.NORTH);
        feedbackPanel.add(feedbackScrollPane, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(100, 149, 237));

        mainPanel.add(createParticleInputPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.NORTH);
        getContentPane().add(feedbackPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton clearButton = new JButton("Clear");
        pauseButton = new JButton("Pause");
        controlPanel.add(clearButton);
        controlPanel.add(pauseButton);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);

        clearButton.addActionListener(e -> {
            threadManager.clearParticles();
            feedbackMessages.add("All particles cleared.");
            updateFeedbackDisplay();
        });

        pauseButton.addActionListener(e -> {
            if (isPaused) {
                threadManager.resumeParticles();
                pauseButton.setText("Pause");
                feedbackMessages.add("Simulation resumed.");
            } else {
                threadManager.pauseParticles();
                pauseButton.setText("Resume");
                feedbackMessages.add("Simulation paused.");
            }
            isPaused = !isPaused;
            updateFeedbackDisplay();
        });

        pack();
    }

    private JPanel createParticleInputPanel() {
        JPanel particlePanel = new JPanel(new BorderLayout());
        JPanel particleInputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        particleInputPanel.setBackground(new Color(173, 216, 230));
        particleInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
        JLabel modeLabel = new JLabel("Select Mode:");
        JRadioButton genericModeButton = new JRadioButton("Basic Mode");
        JRadioButton uniformDistanceButton = new JRadioButton("Distance Mode");
        JRadioButton uniformAngleButton = new JRadioButton("Angle Mode");
        JRadioButton uniformVelocityButton = new JRadioButton("Velocity Mode");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(genericModeButton);
        modeGroup.add(uniformDistanceButton);
        modeGroup.add(uniformAngleButton);
        modeGroup.add(uniformVelocityButton);
        uniformDistanceButton.setSelected(true); // Default selection for distance batch mode
    
        JTextField numberInput = new JTextField("10", 5);
        JTextField xInput = new JTextField("0", 5);
        JTextField yInput = new JTextField("0", 5);
        secondXInput = new JTextField("1280", 5); // For uniform distance end point
        secondYInput = new JTextField("720", 5); // For uniform distance end point
        JTextField angleInput = new JTextField("0", 5);
        secondAngleInput = new JTextField("360", 5); // For uniform angle end angle
        JTextField velocityInput = new JTextField("10", 5);
        secondVelocityInput = new JTextField("50", 5); // For uniform velocity end velocity
        JButton addButton = new JButton("Add Now");
    
        // Adding components
        particleInputPanel.add(modeLabel);
        particleInputPanel.add(new JLabel(""));
        particleInputPanel.add(genericModeButton);
        particleInputPanel.add(uniformDistanceButton);
        particleInputPanel.add(uniformAngleButton);
        particleInputPanel.add(uniformVelocityButton);
        particleInputPanel.add(new JLabel("Num. of Particles:"));
        particleInputPanel.add(numberInput);
        particleInputPanel.add(new JLabel("X:"));
        particleInputPanel.add(xInput);
        particleInputPanel.add(new JLabel("Y:"));
        particleInputPanel.add(yInput);
        particleInputPanel.add(new JLabel("Second X:")); // For uniform distance
        particleInputPanel.add(secondXInput);
        particleInputPanel.add(new JLabel("Second Y:")); // For uniform distance
        particleInputPanel.add(secondYInput);
        particleInputPanel.add(new JLabel("Angle:"));
        particleInputPanel.add(angleInput);
        particleInputPanel.add(new JLabel("End Angle:")); // For uniform angle
        particleInputPanel.add(secondAngleInput);
        particleInputPanel.add(new JLabel("Velocity:"));
        particleInputPanel.add(velocityInput);
        particleInputPanel.add(new JLabel("End Velocity:")); // For uniform velocity
        particleInputPanel.add(secondVelocityInput);
        particleInputPanel.add(new JLabel("")); // Empty cell to improve spacing
        particleInputPanel.add(addButton);
    
        // Action listeners for the buttons and mode change
        ActionListener modeChangeListener = e -> {
            boolean isGeneric = genericModeButton.isSelected();
            numberInput.setEnabled(isGeneric || uniformDistanceButton.isSelected() || uniformAngleButton.isSelected() || uniformVelocityButton.isSelected());
            secondXInput.setVisible(uniformDistanceButton.isSelected());
            secondYInput.setVisible(uniformDistanceButton.isSelected());
            secondAngleInput.setVisible(uniformAngleButton.isSelected());
            secondVelocityInput.setVisible(uniformVelocityButton.isSelected());
        };
    
        genericModeButton.addActionListener(modeChangeListener);
        uniformDistanceButton.addActionListener(modeChangeListener);
        uniformAngleButton.addActionListener(modeChangeListener);
        uniformVelocityButton.addActionListener(modeChangeListener);
        modeChangeListener.actionPerformed(null);  // Initial call to set visibility
    
        addButton.addActionListener(e -> {
            try {
                int number = Integer.parseInt(numberInput.getText());
                int x = Integer.parseInt(xInput.getText());
                // Transform the Y-coordinate so 0 is at the bottom
                int y = 720 - Integer.parseInt(yInput.getText()); // Flipping the y-coordinate
                double angle = Math.toRadians(Double.parseDouble(angleInput.getText()));
                double velocity = Double.parseDouble(velocityInput.getText());
        
                if (genericModeButton.isSelected()) {
                    for (int i = 0; i < number; i++) {
                        threadManager.addParticle(x, y, angle, velocity);
                    }
                    feedbackMessages.add("Added " + number + " individual particle(s) at (" + x + ", " + (720 - y) + ") with angle " +
                                         Math.toDegrees(angle) + " degrees and velocity " + velocity + ".");
                } else if (uniformDistanceButton.isSelected()) {
                    int x2 = Integer.parseInt(secondXInput.getText());
                    int y2 = 720 - Integer.parseInt(secondYInput.getText()); // Flipping the y-coordinate
                    threadManager.addParticlesWithUniformDistance(number, x, y, x2, y2, angle, velocity);
                    feedbackMessages.add(number + " particles added uniformly between (" + x + ", " + (720 - y) + ") and (" + x2 + ", " + (720 - y2) + ").");
                } else if (uniformAngleButton.isSelected()) {
                    double endTheta = Math.toRadians(Double.parseDouble(secondAngleInput.getText()));
                    threadManager.addParticlesWithUniformAngle(number, x, y, angle, endTheta, velocity);
                    feedbackMessages.add(number + " particles added from " + Math.toDegrees(angle) + "° to " + Math.toDegrees(endTheta) + "°.");
                } else if (uniformVelocityButton.isSelected()) {
                    double endVelocity = Double.parseDouble(secondVelocityInput.getText());
                    threadManager.addParticlesWithUniformVelocity(number, x, y, angle, velocity, endVelocity);
                    feedbackMessages.add(number + " particles added with velocities ranging from " + velocity + " to " + endVelocity + ".");
                }
                updateFeedbackDisplay();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please ensure all fields are filled correctly.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
    
        particlePanel.add(particleInputPanel, BorderLayout.CENTER);
        return particlePanel;
    }
    

    private void updateFeedbackDisplay() {
        StringBuilder feedbackHtml = new StringBuilder("<html>");
        for (String msg : feedbackMessages) {
            feedbackHtml.append(msg).append("<br>");
        }
        feedbackHtml.append("</html>");
        feedbackLabel.setText(feedbackHtml.toString());
    }

    public static void main(String[] args) {
        ThreadController threadManager = new ThreadController();
        InputSelection window = new InputSelection(threadManager);
        window.setVisible(true);
    }
}
