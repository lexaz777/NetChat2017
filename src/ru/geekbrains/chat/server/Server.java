package ru.geekbrains.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {

    private ServerSocket server;
    private Socket socket;
    private Vector<ClientHandler> clients = new Vector<>();


    public Server(int serverBindPort) {
        try {
            server = new ServerSocket(serverBindPort);
            System.out.println("Сервер запущен на порту " + serverBindPort);

            while (true) {
                socket = server.accept();
                System.out.println("Клиент " + (clients.size() + 1) + " подключился.");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Клиент отключен!");
    }

    public void broadcastMsg(String str) {
        for (ClientHandler client : clients) {
            client.sendMsg(str);
        }
    }

    public void privateMsg(String nickname, String str) {
        for(ClientHandler client : clients) {
            if (client.getNickname().equals(nickname)) {
                client.sendMsg(str);
            }
        }
    }

    public void subscribeClient(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }


}
