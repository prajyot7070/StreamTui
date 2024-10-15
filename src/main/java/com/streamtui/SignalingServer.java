package com.streamtui;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class SignalingServer extends WebSocketServer {

    private Map<String, String> userRooms = new HashMap<>();
    private Map<String, WebSocket> users = new HashMap<>();

    public SignalingServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String userId = getUserId(conn);
        if (userId != null) {
            userRooms.remove(userId);
            users.remove(userId);
            System.out.println("Connection closed for user: " + userId);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Parse the message and handle different types of requests
        String[] parts = message.split(":", 2);
        if (parts.length != 2) return;

        String type = parts[0];
        String payload = parts[1];

        switch (type) {
            case "LOGIN":
                handleLogin(conn, payload);
                break;
            case "CREATE_ROOM":
                handleCreateRoom(conn, payload);
                break;
            case "JOIN_ROOM":
                handleJoinRoom(conn, payload);
                break;
            case "OFFER":
            case "ANSWER":
            case "ICE_CANDIDATE":
                handleSignalingMessage(conn, type, payload);
                break;
        }
    }

    private void handleLogin(WebSocket conn, String userId) {
        users.put(userId, conn);
        conn.send("LOGIN_SUCCESS:" + userId);
    }

    private void handleCreateRoom(WebSocket conn, String roomId) {
        String userId = getUserId(conn);
        if (userId != null) {
            userRooms.put(userId, roomId);
            conn.send("ROOM_CREATED:" + roomId);
        }
    }

    private void handleJoinRoom(WebSocket conn, String roomId) {
        String userId = getUserId(conn);
        if (userId != null) {
            userRooms.put(userId, roomId);
            conn.send("ROOM_JOINED:" + roomId);
        }
    }

    private void handleSignalingMessage(WebSocket sender, String type, String payload) {
        String senderId = getUserId(sender);
        String senderRoom = userRooms.get(senderId);

        if (senderId != null && senderRoom != null) {
            for (Map.Entry<String, String> entry : userRooms.entrySet()) {
                if (entry.getValue().equals(senderRoom) && !entry.getKey().equals(senderId)) {
                    WebSocket recipient = users.get(entry.getKey());
                    if (recipient != null) {
                        recipient.send(type + ":" + payload);
                    }
                }
            }
        }
    }

    private String getUserId(WebSocket conn) {
        for (Map.Entry<String, WebSocket> entry : users.entrySet()) {
            if (entry.getValue() == conn) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("An error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
    }

    @Override
    public void onStart() {
        System.out.println("SignalingServer started on port: " + getPort());
    }

    public static void main(String[] args) {
        int port = 8887; // You can change this port number if needed
        SignalingServer server = new SignalingServer(port);
        server.start();
    }
}