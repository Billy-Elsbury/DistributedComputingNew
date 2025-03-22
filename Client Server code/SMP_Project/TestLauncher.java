import javax.swing.*;
import java.util.concurrent.CountDownLatch;

public class TestLauncher {
    private static final CountDownLatch uiReadyLatch = new CountDownLatch(1); //Latch to ensure UI is ready

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", "clientTruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");

        //SMPServerUI for debugging
        SwingUtilities.invokeLater(() -> {
            try {
                SMPServerUI serverUI = SMPServerUI.getInstance();
                serverUI.setVisible(true);
                uiReadyLatch.countDown(); //Signal UI is ready
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to initialize SMPServerUI: " + e.getMessage());
            }
        });

        //Wait for UI to be initialised
        try {
            uiReadyLatch.await(); //Blocking until UI is ready
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("UI initialization was interrupted: " + e.getMessage());
        }

        //server in a new thread
        new Thread(() -> SMPServer.main(new String[]{})).start();

        try {
            Thread.sleep(2000); // couple seconds for server to initialise
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Launch any number of login windows
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