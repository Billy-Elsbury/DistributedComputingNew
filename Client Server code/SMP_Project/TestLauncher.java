import javax.swing.*;
import java.util.concurrent.CountDownLatch;

public class TestLauncher {
    private static final CountDownLatch uiReadyLatch = new CountDownLatch(1); // Latch to ensure UI is ready

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", "clientTruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");

        // Launch the SMPServerUI for debugging
        SwingUtilities.invokeLater(() -> {
            try {
                SMPServerUI serverUI = SMPServerUI.getInstance();
                serverUI.setVisible(true); // Ensure the UI is visible
                uiReadyLatch.countDown(); // Signal that the UI is ready
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to initialize SMPServerUI: " + e.getMessage());
            }
        });

        // Wait for the UI to be fully initialized
        try {
            uiReadyLatch.await(); // Block until the UI is ready
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("UI initialization was interrupted: " + e.getMessage());
        }

        // Launch the server in a new thread
        new Thread(() -> SMPServer.main(new String[]{})).start();

        // Wait for the server to start
        try {
            Thread.sleep(2000); // Give the server time to initialize
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Launch login windows for testing
        launchLoginWindow();
        launchLoginWindow();
    }

    private static void launchLoginWindow() {
        SwingUtilities.invokeLater(() -> {
            try {
                LoginWindow loginWindow = new LoginWindow();
                loginWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                loginWindow.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to launch LoginWindow: " + e.getMessage());
            }
        });
    }
}