package SocketChatApp.Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

//separate thread for each connected client
public class ClientHandler implements Runnable {
    // this client handler keep track of all clients
    // A static list that stores all active clients.
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    //socket connect client server
    private Socket socket;
    //read incomming messge from client
    private BufferedReader bufferedReader;
    //send message to client
    private BufferedWriter bufferedWriter;
    //store username to store client
    private String clientUsername;

    //read the first message from client == assume it is the username
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            // add the client handler list
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
        //continuously listening for the messages
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient != null) {
                    //split messages to all client
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
            // find the client name (receiver name == to client usernamae)
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
            //create a ","separate list for username of all client
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