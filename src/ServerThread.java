import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class ServerThread extends Thread {

    private final Socket clientSocket;
    private static final boolean[] connectedSockets = new boolean[5];
    private static final String[] playerPositions = new String[5];
    private static final int[] playerHealth = new int[5];
    private static final int[] playerKills = new int[5];
    private static final int[] playerDeaths = new int[5];
    private int ID;

    public ServerThread(Socket clientSocket) {
        super("ServerThread");
        this.clientSocket = clientSocket;
        for (int i = 1; i <= 4; i++) {
            if (!connectedSockets[i]) {
                connectedSockets[i] = true;
                playerHealth[i] = 100;
                ID = i;
                break;
            }
        }
    }

    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            if (ID == 0) {
                out.println("EXIT");
            } else {
                out.println("CONNECTED");
                out.println(ID);
                System.out.println("connected " + ID);
                String input;
                main:
                while (true) {
                    out.println("GET");
                    if ((input = in.readLine()).equals("DISCONNECT")) {
                        break;
                    }
                    playerPositions[ID] = input;
                    out.println("EVENTS");
                    while (!(input = in.readLine()).equals("END")) {
                        if (input.equals("DISCONNECT")) {
                            break main;
                        }
                        if (input.equals("WET")) {
                            playerDeaths[ID]++;
                            playerHealth[ID] = 100;
                        } else {
                            StringTokenizer tokens = new StringTokenizer(input);
                            int player = Integer.parseInt(tokens.nextToken());
                            int damage = Integer.parseInt(tokens.nextToken());
                            if (playerHealth[player] <= damage && playerHealth[player] > 0) {
                                playerKills[ID]++;
                            }
                            playerHealth[player] -= damage;
                        }
                    }
                    if (playerHealth[ID] <= 0) {
                        playerDeaths[ID]++;
                        playerHealth[ID] = 100;
                        out.println("RESPAWN");
                        if ((input = in.readLine()).equals("DISCONNECT")) {
                            break;
                        }
                        playerPositions[ID] = input;
                    }
                    out.println("UPDATE");
                    out.println(playerHealth[ID] + " " + playerKills[ID] + " " + playerDeaths[ID]);
                    for (int i = 1; i <= 4; i++) {
                        if (ID != i) {
                            if (playerPositions[i] == null) {
                                out.println(i + " -1000 -1000 0 0 false");
                            } else {
                                out.println(i + " " + playerPositions[i]);
                            }
                        }
                    }
                    out.println("END");
                    Thread.sleep(1000 / 60);
                }
                System.out.println("disconnected " + ID);
            }
            clientSocket.close();
            connectedSockets[ID] = false;
            playerPositions[ID] = null;
            playerKills[ID] = 0;
            playerDeaths[ID] = 0;
        } catch (IOException e) {
            connectedSockets[ID] = false;
            playerPositions[ID] = null;
            playerKills[ID] = 0;
            playerDeaths[ID] = 0;
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            connectedSockets[ID] = false;
            playerPositions[ID] = null;
            playerKills[ID] = 0;
            playerDeaths[ID] = 0;
            System.exit(-1);
        }
    }
}
