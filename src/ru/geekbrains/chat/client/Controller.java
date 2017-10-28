package ru.geekbrains.chat.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public TextArea chatArea;
    public TextField msgField;
    public TextField loginTextField;
    public TextField passwdTextField;
    public Button loginButton;
    public Button sendMsgButton;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final String IP_ADDRESS = "localhost";
    final int PORT = 31337;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        sendMsgButton.setVisible(false);
        msgField.setVisible(false);

        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if(str.equals("/serverclosed")) break;
                        if(str.equals("/authok")) {
                            setAuthorized(true);
                            break;
                        } else
                            chatArea.appendText(str + "\n");
                    }

                    while (true) {
                        String str = in.readUTF();
                        if(str.equals("/serverclosed")) break;
                        chatArea.appendText(str + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                        Platform.exit();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

        } catch (ConnectException e) {
            chatArea.setText("Сервер не доступен");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setAuthorized(boolean authorized) {
        sendMsgButton.setVisible(authorized);
        msgField.setVisible(authorized);
        //loginTextField.setVisible(!authorized);
        loginTextField.setDisable(true);
        passwdTextField.setVisible(!authorized);
        loginButton.setVisible(!authorized);
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void auth(ActionEvent event) {
        try {
            out.writeUTF("/auth " + loginTextField.getText() + " " + passwdTextField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            chatArea.setText("Сервер не доступен");
        }
    }
}
