package ru.geekbrains.chat.server;

import java.util.HashMap;

public class AuthService {

    private static HashMap<String, String> userdb = new HashMap<>();

    static {
        userdb.put("alex", "123456");
        userdb.put("ustas", "qaz159edc");
        userdb.put("ammy", "pond");
        userdb.put("test", "test");
    }

    public static boolean checkAuth(String username, String password) {
        return userdb.get(username).equals(password);
    }

}
