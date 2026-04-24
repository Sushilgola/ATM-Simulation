import javax.swing.SwingUtilities;

public class ATMMain {
    public static void main(String[] args) {

        System.out.println("║     Java Bank ATM — Starting...      ║");

        // Step 1: Initialize database (creates folder + files on first run)
        DatabaseManager.initialize();

        // Step 2: Launch the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ATMFrame frame = new ATMFrame();
            frame.setVisible(true);
            System.out.println("[APP] ATM window launched successfully.");
        });
    }
}
