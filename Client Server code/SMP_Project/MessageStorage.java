import java.io.*;
import java.util.*;

public class MessageStorage {
    // Singleton instance
    private static MessageStorage instance;
    private Map<String, List<Message>> userMessages;
    private String storageFile = "messages.txt";
    private int globalMessageIdCounter = 0;

    // Private constructor, prevent instantiation
    private MessageStorage() {
        this.userMessages = new HashMap<>();
        loadMessagesFromFile();
    }

    // Singleton accessor
    public static synchronized MessageStorage getInstance() {
        if (instance == null) {
            instance = new MessageStorage();
        }
        return instance;
    }

    private static class Message {
        int id;
        String username;
        String content;

        Message(int id, String username, String content) {
            this.id = id;
            this.username = username;
            this.content = content;
        }

        @Override
        public String toString() {
            return username + ":" + id + ":" + content;
        }
    }

    private void loadMessagesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 3);
                if (parts.length == 3) {
                    String username = parts[0];
                    int id = Integer.parseInt(parts[1]);
                    String content = parts[2];
                    userMessages.putIfAbsent(username, new ArrayList<>());
                    userMessages.get(username).add(new Message(id, username, content));
                    if (id > globalMessageIdCounter) {
                        globalMessageIdCounter = id;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("No existing message file found. Starting with an empty message store.");
        }
    }

    private void saveMessagesToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile))) {
            for (Map.Entry<String, List<Message>> entry : userMessages.entrySet()) {
                for (Message msg : entry.getValue()) {
                    writer.write(msg.toString() + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to save messages to file: " + e.getMessage());
        }
    }

    public synchronized boolean addMessage(String username, int id, String message) {
        userMessages.putIfAbsent(username, new ArrayList<>());

        //If ID not provided auto generate next available ID
        if (id == -1) {
            id = getNextAvailableId();
        }

        //if the ID already exists
        for (List<Message> messages : userMessages.values()) {
            for (Message msg : messages) {
                if (msg.id == id) {
                    return false;  // ID already exists
                }
            }
        }

        userMessages.get(username).add(new Message(id, username, message));
        saveMessagesToFile();  // Save messages to file
        return true;
    }

    private int getNextAvailableId() {
        return ++globalMessageIdCounter;  // Increment and return the global counter
    }

    public String getMessageById(int messageId) {
        for (Map.Entry<String, List<Message>> entry : userMessages.entrySet()) {
            for (Message msg : entry.getValue()) {
                if (msg.id == messageId) {
                    return "User: " + entry.getKey() + " - ID: " + msg.id + " - " + msg.content;
                }
            }
        }
        return null;
    }

    public List<String> getAllMessages() {
        List<String> allMessages = new ArrayList<>();
        for (Map.Entry<String, List<Message>> entry : userMessages.entrySet()) {
            for (Message msg : entry.getValue()) {
                allMessages.add("ID: " + msg.id + " - " + msg.content);  // Add each message as a separate item
            }
        }
        return allMessages;
    }

    public synchronized void clearMessages() {
        userMessages.clear();
        globalMessageIdCounter = 0;
        saveMessagesToFile();
    }
}