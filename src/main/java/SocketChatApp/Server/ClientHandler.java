package SocketChatApp.Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("||USER_LIST||SERVER||ALL||" + getUsernames() + "||");
            broadcastMessage("||MESSAGE||SERVER||ALL||" + clientUsername + " has entered the chat!||");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient != null) {
                    String[] parts = messageFromClient.split("\\|\\|");
                    if (parts.length >= 5 && parts[1].equals("MESSAGE")) {
                        String receiver = parts[3];
                        if (receiver.equals("ALL")) {
                            broadcastMessage(messageFromClient);
                        } else {
                            sendMessageToUser(messageFromClient, receiver);
                        }
                    }
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    private void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                clientHandler.bufferedWriter.write(messageToSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    private void sendMessageToUser(String messageToSend, String receiver) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clientUsername.equals(receiver)) {
                try {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
                break;
            }
        }
    }

    private String getUsernames() {
        StringBuilder usernames = new StringBuilder();
        for (ClientHandler clientHandler : clientHandlers) {
            usernames.append(clientHandler.clientUsername).append(",");
        }
        return usernames.toString();
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("||USER_LIST||SERVER||ALL||" + getUsernames() + "||");
        broadcastMessage("||MESSAGE||SERVER||ALL||" + clientUsername + " has left the chat!||");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}