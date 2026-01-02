import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) {
        try (
            ServerSocket serverSocket = new ServerSocket(0)
        ) {
            System.out.println("waiting at " + InetAddress.getLocalHost() + ":" + serverSocket.getLocalPort());
            while (true) {
                new ServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}