package com.streamtui.server;

import org.java_websocket.WebSocket;
import java.util.HashSet;
import java.util.Set;

public class Room {
    private final Set<WebSocket> users = new HashSet<>();
    private static final int MAX_USERS = 2;

    public void addUser(WebSocket user) {
        users.add(user);
    }

    public void removeUser(WebSocket user) {
        users.remove(user);
    }

    public boolean canJoin() {
        return users.size() < MAX_USERS;
    }

    public boolean isEmpty() {
        return users.isEmpty();
    }

    public void broadcastToOthers(WebSocket sender, String message) {
        users.stream()
                .filter(user -> user != sender)
                .forEach(user -> user.send(message));
    }
}