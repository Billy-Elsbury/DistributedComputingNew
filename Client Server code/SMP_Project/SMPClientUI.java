import javax.swing.*;
import java.awt.*;
import java.io.IOException;

//Main Interface for message management.
public class SMPClientUI extends JFrame {
    private String username;
    private final ClientHelper clientHelper;
    private JTextArea outputArea;

    public SMPClientUI(String username, ClientHelper clientHelper) {
        this.username = username;
        this.clientHelper = clientHelper;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("SMP Client - " + username);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(30, 30, 30));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(new Color(30, 30, 30));

        JLabel messageLabel = new JLabel("Message:");
        messageLabel.setForeground(Color.WHITE);
        JTextField messageField = new JTextField();
        messageField.setBackground(new Color(50, 50, 50));
        messageField.setForeground(Color.WHITE);
        messageField.setCaretColor(Color.WHITE);

        JLabel idLabel = new JLabel("Message ID:");
        idLabel.setForeground(Color.WHITE);
        JTextField idField = new JTextField();
        idField.setBackground(new Color(50, 50, 50));
        idField.setForeground(Color.WHITE);
        idField.setCaretColor(Color.WHITE);

        inputPanel.add(messageLabel);
        inputPanel.add(messageField);
        inputPanel.add(idLabel);
        inputPanel.add(idField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(30, 30, 30));

        JButton uploadButton = new JButton("Upload");
        uploadButton.setBackground(Color.WHITE);
        uploadButton.setForeground(Color.BLACK);
        uploadButton.setFocusPainted(false);

        JButton downloadButton = new JButton("Download");
        downloadButton.setBackground(Color.WHITE);
        downloadButton.setForeground(Color.BLACK);
        downloadButton.setFocusPainted(false);

        JButton clearButton = new JButton("Clear");
        clearButton.setBackground(Color.WHITE);
        clearButton.setForeground(Color.BLACK);
        clearButton.setFocusPainted(false);

        JButton logoffButton = new JButton("Logoff");
        logoffButton.setBackground(Color.WHITE);
        logoffButton.setForeground(Color.BLACK);
        logoffButton.setFocusPainted(false);

        JButton downloadAllButton = new JButton("Download All Messages");
        downloadAllButton.setBackground(Color.WHITE);
        downloadAllButton.setForeground(Color.BLACK);
        downloadAllButton.setFocusPainted(false);

        buttonPanel.add(uploadButton);
        buttonPanel.add(downloadButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(logoffButton);
        buttonPanel.add(downloadAllButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setWrapStyleWord(true);
        outputArea.setLineWrap(true);
        outputArea.setBackground(new Color(50, 50, 50));
        outputArea.setForeground(Color.WHITE);
        outputArea.setCaretColor(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBackground(new Color(30, 30, 30));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(inputPanel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 10, 0);
        add(buttonPanel, gbc);

        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollPane, gbc);

        uploadButton.addActionListener(e -> upload(messageField.getText(), idField.getText()));
        downloadButton.addActionListener(e -> download(idField.getText()));
        downloadAllButton.addActionListener(e -> downloadAll());
        clearButton.addActionListener(e -> clear());
        logoffButton.addActionListener(e -> logoff());
    }

    private void upload(String message, String id) {
        if (username == null) {
            outputArea.append(ResponseCodes.NOT_LOGGED_IN + " Not logged in.\n");
            return;
        }
        if (message.isEmpty()) {
            outputArea.append(ResponseCodes.EMPTY_MESSAGE + " Message content cannot be empty.\n");
            return;
        }
        try {
            int messageId = id.isEmpty() ? -1 : Integer.parseInt(id);
            String response = clientHelper.upload(username, messageId, message);
            outputArea.append(response + "\n");
        } catch (NumberFormatException e) {
            outputArea.append(ResponseCodes.INVALID_MESSAGE_ID + " Invalid message ID.\n");
        } catch (IOException ex) {
            outputArea.append("Error: " + ex.getMessage() + "\n");
        }
    }

    private void download(String id) {
        if (username == null)
        {
            outputArea.append(ResponseCodes.NOT_LOGGED_IN + " Not logged in.\n");
            return;
        }
        try {
            String response = clientHelper.download(id); // Pass only the message ID
            outputArea.append("Messages from server:\n" + response + "\n");
        }
        catch (IOException ex)
        {
            outputArea.append("Error: " + ex.getMessage() + "\n");
        }
    }

    private void downloadAll() {
        if (username == null)
        {
            outputArea.append(ResponseCodes.NOT_LOGGED_IN + " Not logged in.\n");
            return;
        }
        try {
            String response = clientHelper.download("all");
            String[] messages = response.split("\\|");
            outputArea.setText("");
            for (String message : messages)
            {
                outputArea.append(message + "\n");
            }
        }
        catch (IOException ex)
        {
            outputArea.append("Error: " + ex.getMessage() + "\n");
        }
    }

    private void clear() {
        if (username == null)
        {
            outputArea.append(ResponseCodes.NOT_LOGGED_IN + " Not logged in.\n");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all messages?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION)
        {
            try
            {
                String result = clientHelper.clear();
                outputArea.append(result + "\n");
            }
            catch (IOException ex)
            {
                outputArea.append("Error: " + ex.getMessage() + "\n");
            }
        }
    }

    private void logoff() {
        if (username == null) return;

        try
        {
            String response = clientHelper.logoff(username);
            outputArea.append(response + "\n");
            clientHelper.close();
            username = null;
            dispose();

            SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
        }
        catch (IOException ex)
        {
            outputArea.append("Logoff error: " + ex.getMessage() + "\n");
        }
    }
}