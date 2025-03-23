import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

//Interface for server logs and debugging.
public class SMPServerUI extends JFrame {
    private static SMPServerUI instance;
    private JTextArea logArea;
    private boolean isInitialised = false;

    private SMPServerUI() {
        initializeUI();
    }

    public static SMPServerUI getInstance() {
        if (instance == null) {
            instance = new SMPServerUI();
        }
        return instance;
    }

    private void initializeUI() {
        setTitle("SMP Server Debugging UI");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(Color.WHITE);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        isInitialised = true;
    }

    public void log(String message) {
        if (!isInitialised) {
            System.err.println("SMPServerUI is not initialized. Cannot log: " + message);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                String logMessage = "[" + timestamp + "] " + message + "\n";
                logArea.append(logMessage);
                logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll to the bottom
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to update SMPServerUI: " + e.getMessage());
            }
        });
    }
}