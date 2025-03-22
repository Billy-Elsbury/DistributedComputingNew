import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileInputStream;
import java.util.Properties;

public class SMPServer {
    public static void main(String[] args) {
        int serverPort = 12345;  //Default port
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }

        //properties from config file
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        } catch (Exception ex) {
            System.err.println("Failed to load config.properties");
            ex.printStackTrace();
            return;
        }

        //keystore properties
        System.setProperty("javax.net.ssl.keyStore", props.getProperty("keystore.path"));
        System.setProperty("javax.net.ssl.keyStorePassword", props.getProperty("keystore.password"));

        try {
            SSLServerSocketFactory sslServerSocketFactory =
                    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslServerSocket =
                    (SSLServerSocket) sslServerSocketFactory.createServerSocket(serverPort);

            SMPServerUI.getInstance().log("SMP Server (SSL) ready on port " + serverPort);

            while (true) {
                SMPServerUI.getInstance().log("Waiting for a connection...");
                MyStreamSocket myDataSocket = new MyStreamSocket(sslServerSocket.accept());
                SMPServerUI.getInstance().log("Connection accepted.");

                Thread theThread = new Thread(new SMPThread(myDataSocket));
                theThread.start();
            }
        } catch (Exception ex) {
            SMPServerUI.getInstance().log("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}