package SocketChatApp.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    private final ServerSocket serverSocket;

    public ServerMain(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }


    public void startServer() {
        System.out.println("Server is running and waiting for connections on port 9822...");
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client connected.");

                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            closeServer();
        }
    }

    private void closeServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9822);
        ServerMain serverMain = new ServerMain(serverSocket);
        serverMain.startServer();
    }
}
