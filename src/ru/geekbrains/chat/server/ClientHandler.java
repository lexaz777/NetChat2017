package ru.geekbrains.chat.server;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;


public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataOutputStream out;
    private DataInputStream in;
    private String username;
    private boolean isAuthorized;
    private Thread threadReadSocket;

    public ClientHandler(Server server, Socket socket) {

        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //Дополнительный поток для отключения по таймауту
                    this.threadReadSocket = new Thread(this::readSocket);
                    threadReadSocket.start();
                    Long startTimer = System.currentTimeMillis();
                    while (true) {
                        if (isAuthorized) break;
                        Thread.sleep(200);
                        if (!isAuthorized) {
                            Long stopTimer = System.currentTimeMillis();
                            if (stopTimer - startTimer > 120000) {
                                System.out.println("Отключаем клиента по таймауту..." + this);
                                out.writeUTF("/serverclosed");
                                threadReadSocket.interrupt();
                                break;
                            }
                        }
                    }

                    while (isAuthorized) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            out.writeUTF("/serverclosed");
                            server.deleteClient(this);
                            break;
                        }
                            if(str.startsWith("/w")) {
                            String[] tokens = str.split(" ");
                            String msg = str.substring(1 + tokens[0].length() + tokens[1].length(),
                                    str.length());
                            server.privateMsg(tokens[1], msg);
                        } else
                            server.broadcastMsg(str);
                    }
                } catch (InterruptedException e){
                    e.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    try {
                        in.close();
                        out.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return username;
    }

    private void readSocket() {
        String str;
        do {
            try {
                str = in.readUTF();
                if (str.startsWith("/auth")) {
                    String[] tokens = str.split(" ");
                    if (AuthService.checkAuth(tokens[1], tokens[2])) {
                        username = tokens[1];
                        server.subscribeClient(this);
                        out.writeUTF("/authok");
                        isAuthorized = true;
                        System.out.println("Клиент " + tokens[1] + " авторизовался.");
                        break;
                    } else
                        isAuthorized = false;
                }
            } catch (IOException e) {
                System.out.println("Connection closed");
                break;
            }
        } while (true);
    }
}
