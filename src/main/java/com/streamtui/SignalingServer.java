
package com.streamtui;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.*;

public class SignalingServer extends WebSocketServer {
    private Map<String, WebSocket> users = new HashMap<>();
    private Map<String, String> userRooms = new HashMap<>();
    private Map<String, Set<String>> roomUsers = new HashMap<>();
    private Map<WebSocket, String> socketToUser = new HashMap<>();

    public SignalingServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onStart() {
        System.out.println("SignalingServer started on port: " + getPort());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String userId = socketToUser.get(conn);
        if (userId != null) {
            handleUserDisconnection(userId, conn);
        }
        socketToUser.remove(conn);
    }

    private void handleUserDisconnection(String userId, WebSocket conn) {
        String roomId = userRooms.get(userId);
        users.remove(userId);
        userRooms.remove(userId);

        if (roomId != null && roomUsers.containsKey(roomId)) {
            roomUsers.get(roomId).remove(userId);
            notifyRoom(roomId, "USER_LEFT", userId);

            if (roomUsers.get(roomId).isEmpty()) {
                roomUsers.remove(roomId);
            }
        }

        System.out.println("User disconnected: " + userId);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            String[] parts = message.split(":", 2);
            if (parts.length != 2) {
                conn.send("ERROR:Invalid message format");
                return;
            }

            String type = parts[0];
            String payload = parts[1];
            System.out.println("Received message - Type: " + type + ", Payload: " + payload);

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
                default:
                    conn.send("ERROR:Unknown message type: " + type);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            conn.send("ERROR:Internal server error");
        }
    }

    private void handleLogin(WebSocket conn, String userId) {
        if (users.containsKey(userId)) {
            conn.send("ERROR:User ID already in use");
            return;
        }

        users.put(userId, conn);
        socketToUser.put(conn, userId);
        conn.send("LOGIN_SUCCESS:" + userId);
        System.out.println("User logged in: " + userId);
    }

    private void handleCreateRoom(WebSocket conn, String roomId) {
        String userId = socketToUser.get(conn);
        if (userId == null) {
            conn.send("ERROR:Not logged in");
            return;
        }

        if (roomUsers.containsKey(roomId)) {
            conn.send("ERROR:Room already exists");
            return;
        }

        Set<String> roomMembers = new HashSet<>();
        roomMembers.add(userId);
        roomUsers.put(roomId, roomMembers);
        userRooms.put(userId, roomId);

        conn.send("ROOM_CREATED:" + roomId);
        System.out.println("Room created: " + roomId + " by user: " + userId);
    }

    private void handleJoinRoom(WebSocket conn, String roomId) {
        String userId = socketToUser.get(conn);
        if (userId == null) {
            conn.send("ERROR:Not logged in");
            return;
        }

        if (!roomUsers.containsKey(roomId)) {
            conn.send("ERROR:Room does not exist");
            return;
        }

        roomUsers.get(roomId).add(userId);
        userRooms.put(userId, roomId);
        conn.send("ROOM_JOINED:" + roomId);
        notifyRoom(roomId, "USER_JOINED", userId, userId);
        System.out.println("User " + userId + " joined room: " + roomId);
    }

    private void handleSignalingMessage(WebSocket sender, String type, String payload) {
        String senderId = socketToUser.get(sender);
        if (senderId == null) {
            sender.send("ERROR:Not logged in");
            return;
        }

        String roomId = userRooms.get(senderId);
        if (roomId == null) {
            sender.send("ERROR:Not in a room");
            return;
        }

        // Check if there is someone else in the room other than the sender
        Set<String> roomMembers = roomUsers.get(roomId);
        if (roomMembers == null || roomMembers.size() <= 1) {
            sender.send("ERROR:No other participants in the room");
            System.out.println("No participants other than the sender in the room. Offer not sent.");
            return;
        }

        notifyRoom(roomId, type, payload, senderId);
        System.out.println("Forwarding " + type + " message from " + senderId + " to room " + roomId);
    }


    private void notifyRoom(String roomId, String type, String payload) {
        notifyRoom(roomId, type, payload, null);
    }

    private void notifyRoom(String roomId, String type, String payload, String excludeUserId) {
        Set<String> roomMembers = roomUsers.get(roomId);
        if (roomMembers != null) {
            boolean offerSent = false; // Flag to track if the offer is sent
            for (String userId : roomMembers) {
                // Send to everyone except the user who created/sent the offer (excludeUserId)
                if (excludeUserId == null || !userId.equals(excludeUserId)) {
                    WebSocket userConn = users.get(userId);
                    if (userConn != null && userConn.isOpen()) {
                        userConn.send(type + ":" + payload);
                        System.out.println("Sent " + type + " to user: " + userId);
                        offerSent = true;
                    }
                }
            }

            // If no offer was sent, notify the sender
            if (!offerSent) {
                WebSocket senderConn = users.get(excludeUserId);
                if (senderConn != null && senderConn.isOpen()) {
                    senderConn.send("ERROR:No participants to send the offer to.");
                }
            }
        }
    }


    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error on connection " + conn.getRemoteSocketAddress() + ": " + ex.getMessage());
        if (conn.isOpen()) {
            conn.send("ERROR:Internal server error");
        }
    }

    public static void main(String[] args) {
        int port = 8887;
        SignalingServer server = new SignalingServer(port);
        server.start();
        System.out.println("SignalingServer is running on port: " + port);
    }
}