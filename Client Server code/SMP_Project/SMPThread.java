import java.io.IOException;
import java.util.List;

//Handles client requests on the server side.
public class SMPThread implements Runnable {
    private MyStreamSocket myDataSocket;
    private MessageStorage messageStorage = MessageStorage.getInstance(); //Use singleton instance
    private UserManager userManager = new UserManager();
    private String username = null; //username for this session

    public SMPThread(MyStreamSocket myDataSocket) {
        this.myDataSocket = myDataSocket;
    }

    public void run() {
        boolean done = false;
        String message = "";

        try {
            while (!done) {
                message = myDataSocket.receiveMessage();
                SMPServerUI.getInstance().log("Message received: " + message);

                //if the message is null (client disconnected)
                if (message == null) {
                    SMPServerUI.getInstance().log("Client disconnected.");
                    break;
                }

                // Split message into parts
                String[] parts = message.split(" ", 4);
                if (parts.length == 0) {
                    myDataSocket.sendMessage(ResponseCodes.INVALID_COMMAND + " Invalid command format.");
                    SMPServerUI.getInstance().log("Invalid command format.");
                    continue;
                }

                int requestCode = Integer.parseInt(parts[0]);

                switch (requestCode) {
                    case RequestCodes.REGISTER:
                        if (parts.length == 3) {
                            String username = parts[1];
                            String password = parts[2];
                            if (userManager.addUser(username, password)) {
                                myDataSocket.sendMessage(ResponseCodes.SUCCESS + " Registration successful.");
                                SMPServerUI.getInstance().log("User registered: " + username);
                            } else {
                                myDataSocket.sendMessage(ResponseCodes.INVALID_LOGIN_FORMAT + " Username already exists.");
                                SMPServerUI.getInstance().log("Registration failed: Username already exists.");
                            }
                        } else {
                            myDataSocket.sendMessage(ResponseCodes.INVALID_LOGIN_FORMAT + " Invalid registration format. Usage: " + RequestCodes.REGISTER + " <username> <password>");
                            SMPServerUI.getInstance().log("Invalid registration format.");
                        }
                        break;

                    case RequestCodes.LOGIN:
                        if (parts.length == 3)
                        {
                            String username = parts[1];
                            String password = parts[2];

                            if (userManager.verifyUser(username, password))
                            {
                                this.username = username;
                                myDataSocket.sendMessage(ResponseCodes.SUCCESS + " Login successful.");
                                SMPServerUI.getInstance().log("User logged in: " + username);
                            }
                            else
                            {
                                myDataSocket.sendMessage(ResponseCodes.NOT_LOGGED_IN + " Login Error in Thread, Invalid username or password.");
                                SMPServerUI.getInstance().log("Login failed: Invalid username or password.");
                            }
                        }
                        else
                        {
                            myDataSocket.sendMessage(ResponseCodes.INVALID_LOGIN_FORMAT + " Login Error in Thread, Invalid login format. Usage: " + RequestCodes.LOGIN + " <username> <password>");
                            SMPServerUI.getInstance().log("Invalid login format.");
                        }
                        break;

                    case RequestCodes.UPLOAD:
                        if (parts.length == 4) {
                            String username = parts[1];
                            if (this.username == null || !this.username.equals(username))
                            {
                                myDataSocket.sendMessage(ResponseCodes.NOT_LOGGED_IN + " Not logged in.");
                                SMPServerUI.getInstance().log("Upload failed: User not logged in.");
                                break;
                            }
                            try {
                                int id = Integer.parseInt(parts[2]);
                                String messageContent = parts[3];
                                if (messageContent.isEmpty())
                                {
                                    myDataSocket.sendMessage(ResponseCodes.EMPTY_MESSAGE + " Message content cannot be empty.");
                                    SMPServerUI.getInstance().log("Upload failed: Empty message content.");
                                }
                                else if (messageStorage.uploadMessage(username, id, messageContent))
                                {
                                    myDataSocket.sendMessage(ResponseCodes.SUCCESS + " Message uploaded.");
                                    SMPServerUI.getInstance().log("Message uploaded by " + username + " with ID: " + id);
                                }
                                else
                                {
                                    myDataSocket.sendMessage(ResponseCodes.MESSAGE_ID_EXISTS + " Message ID already exists.");
                                    SMPServerUI.getInstance().log("Upload failed: Message ID already exists.");
                                }
                            } catch (NumberFormatException e) {
                                myDataSocket.sendMessage(ResponseCodes.INVALID_MESSAGE_ID + " Invalid message ID.");
                                SMPServerUI.getInstance().log("Upload failed: Invalid message ID.");
                            }
                        } else {
                            myDataSocket.sendMessage(ResponseCodes.INVALID_UPLOAD_FORMAT + " Invalid upload format. Usage: " + RequestCodes.UPLOAD + " <username> <ID> <message>");
                            SMPServerUI.getInstance().log("Invalid upload format.");
                        }
                        break;

                    case RequestCodes.DOWNLOAD_ALL:
                        List<String> allMessages = messageStorage.getAllMessages();
                        String response = String.join("|", allMessages);  //Join messages with delimiter for UI formatting
                        myDataSocket.sendMessage(response);
                        SMPServerUI.getInstance().log("Downloaded all messages for user: " + username);
                        break;

                    case RequestCodes.DOWNLOAD:
                        if (parts.length == 2) {
                            String input = parts[1];
                            if (input.isEmpty()) {
                                myDataSocket.sendMessage(ResponseCodes.NO_MESSAGE_ID_PROVIDED + " No message ID provided. Usage: " + RequestCodes.DOWNLOAD + " <ID>");
                                SMPServerUI.getInstance().log("Download failed: No message ID provided.");
                            }
                            else
                            {
                                try {
                                    int messageId = Integer.parseInt(input);
                                    String specificMessage = messageStorage.getMessageById(messageId);

                                    if (specificMessage != null)
                                    {
                                        myDataSocket.sendMessage(ResponseCodes.SUCCESS + " " + specificMessage);
                                        SMPServerUI.getInstance().log("Downloaded message with ID: " + messageId);
                                    }
                                    else
                                    {
                                        myDataSocket.sendMessage(ResponseCodes.MESSAGE_NOT_FOUND + " Message not found.");
                                        SMPServerUI.getInstance().log("Download failed: Message not found for ID: " + messageId);
                                    }
                                } catch (NumberFormatException e)
                                {
                                    myDataSocket.sendMessage(ResponseCodes.INVALID_MESSAGE_ID + " Invalid message ID.");
                                    SMPServerUI.getInstance().log("Download failed: Invalid message ID.");
                                }
                            }
                        } else
                        {
                            myDataSocket.sendMessage(ResponseCodes.INVALID_DOWNLOAD_FORMAT + " Invalid download format. Usage: " + RequestCodes.DOWNLOAD + " <ID>");
                            SMPServerUI.getInstance().log("Invalid download format.");
                        }
                        break;

                    case RequestCodes.CLEAR:
                        if (parts.length == 1)
                        {
                            try
                            {
                                messageStorage.clearMessages();
                                myDataSocket.sendMessage(ResponseCodes.SUCCESS + " All messages cleared.");
                                SMPServerUI.getInstance().log("All messages cleared.");
                            }
                            catch (Exception ex)
                            {
                                myDataSocket.sendMessage(ResponseCodes.ERROR_CLEARING_MESSAGES + " Error clearing messages.");
                                SMPServerUI.getInstance().log("Error clearing messages: " + ex.getMessage());
                            }
                        } else
                        {
                            myDataSocket.sendMessage(ResponseCodes.INVALID_CLEAR_FORMAT + " Invalid clear format. Usage: " + RequestCodes.CLEAR);
                            SMPServerUI.getInstance().log("Invalid clear format.");
                        }
                        break;

                    case RequestCodes.LOGOFF:
                        if (parts.length == 2) {
                            String username = parts[1];
                            if (this.username != null && this.username.equals(username)) {
                                this.username = null;
                                myDataSocket.sendMessage(ResponseCodes.SUCCESS + " Logoff successful.");
                                SMPServerUI.getInstance().log("User logged off: " + username);
                                done = true;
                            }
                        }
                        break;

                    default:
                        myDataSocket.sendMessage(ResponseCodes.UNKNOWN_COMMAND + " Unknown command.");
                        SMPServerUI.getInstance().log("Unknown command received.");
                        break;
                }
            }
        } catch (Exception ex) {
            SMPServerUI.getInstance().log("Exception caught in thread: " + ex);
        } finally {
            try {
                myDataSocket.close();
                SMPServerUI.getInstance().log("Socket closed for user: " + username);
            } catch (IOException e) {
                SMPServerUI.getInstance().log("Error closing socket: " + e.getMessage());
            }
        }
    }
}