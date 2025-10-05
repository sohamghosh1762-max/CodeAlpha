import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;

public class StudentGradeTrackerGUI extends JFrame implements ActionListener {

    // --- Data Storage ---
    private ArrayList<String> studentNames;
    private ArrayList<Double> studentScores;

    // --- GUI Components ---
    private JTextField nameField, scoreField;
    private JButton addButton, reportButton;
    private JTextArea reportArea;

    public StudentGradeTrackerGUI() {
        // 1. Initialize Data
        studentNames = new ArrayList<>();
        studentScores = new ArrayList<>();

        // 2. Set up the JFrame (Window)
        setTitle("Student Grade Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // Use BorderLayout for overall structure

        // 3. Create Input Panel (North)
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        nameField = new JTextField(15);
        scoreField = new JTextField(15);
        
        inputPanel.add(new JLabel("Student Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Score (0-100):"));
        inputPanel.add(scoreField);
        
        addButton = new JButton("Add Grade");
        addButton.addActionListener(this);
        reportButton = new JButton("Generate Report");
        reportButton.addActionListener(this);

        inputPanel.add(addButton);
        inputPanel.add(reportButton);

        // 4. Create Report Area (Center)
        reportArea = new JTextArea(15, 40);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);

        // 5. Add Panels to the Frame
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 6. Final Frame Configuration
        pack(); // Adjusts frame size to fit components
        setLocationRelativeTo(null); // Centers the frame
        setVisible(true);
    }

    // --- Event Handling ---

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addGrade();
        } else if (e.getSource() == reportButton) {
            displaySummaryReport();
        }
    }
    
    // --- Grade Management Methods ---

    /**
     * Attempts to read input fields and add a new grade.
     */
    private void addGrade() {
        String name = nameField.getText().trim();
        String scoreText = scoreField.getText().trim();

        if (name.isEmpty() || scoreText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both name and score.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double score = Double.parseDouble(scoreText);

            if (score < 0 || score > 100) {
                JOptionPane.showMessageDialog(this, "Score must be between 0 and 100.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Add to ArrayLists
            studentNames.add(name);
            studentScores.add(score);

            // Clear fields and give feedback
            nameField.setText("");
            scoreField.setText("");
            JOptionPane.showMessageDialog(this, "Grade for " + name + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid score format. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // --- Calculation Methods ---
    
    private double calculateAverage() {
        if (studentScores.isEmpty()) return 0.0;
        double sum = studentScores.stream().mapToDouble(Double::doubleValue).sum();
        return sum / studentScores.size();
    }

    private double getHighestScore() {
        return studentScores.isEmpty() ? 0.0 : Collections.max(studentScores);
    }

    private double getLowestScore() {
        return studentScores.isEmpty() ? 0.0 : Collections.min(studentScores);
    }

    // --- Report Method ---

    /**
     * Generates and displays the summary report in the JTextArea.
     */
    private void displaySummaryReport() {
        if (studentNames.isEmpty()) {
            reportArea.setText("No student data available to generate report.");
            return;
        }

        StringBuilder report = new StringBuilder();
        report.append("=".repeat(45)).append("\n");
        report.append("          STUDENT GRADE SUMMARY\n");
        report.append("=".repeat(45)).append("\n");
        report.append(String.format("%-25s%s\n", "STUDENT NAME", "SCORE"));
        report.append("-".repeat(45)).append("\n");

        // Display individual student data
        for (int i = 0; i < studentNames.size(); i++) {
            report.append(String.format("%-25s%.2f\n", studentNames.get(i), studentScores.get(i)));
        }

        // Display statistics
        report.append("\n").append("-".repeat(45)).append("\n");
        report.append(String.format("Total Students: %d\n", studentNames.size()));
        report.append(String.format("Average Score: %.2f\n", calculateAverage()));
        report.append(String.format("Highest Score: %.2f\n", getHighestScore()));
        report.append(String.format("Lowest Score: %.2f\n", getLowestScore()));
        report.append("=".repeat(45)).append("\n");

        reportArea.setText(report.toString());
    }

    /**
     * Main method to launch the application.
     */
    public static void main(String[] args) {
        // Ensure the GUI updates are run on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> new StudentGradeTrackerGUI());
    }
}