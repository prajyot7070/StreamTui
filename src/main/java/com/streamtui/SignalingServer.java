//package com.streamtui;
//
//import org.java_websocket.WebSocket;
//import org.java_websocket.handshake.ClientHandshake;
//import org.java_websocket.server.WebSocketServer;
//
//import java.net.InetSocketAddress;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.List;
//import java.util.ArrayList;
//public class SignalingServer extends WebSocketServer {
//
//    private Map<String, String> userRooms = new HashMap<>();
//    private Map<String, WebSocket> users = new HashMap<>();
//
//    private Map<String, List<String>> roomUsers = new HashMap<>();
//    public SignalingServer(int port) {
//        super(new InetSocketAddress(port));
//    }
//
//    @Override
//    public void onOpen(WebSocket conn, ClientHandshake handshake) {
//        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
//    }
//
//    @Override
//    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
//        String userId = getUserId(conn);
//        if (userId != null) {
//            userRooms.remove(userId);
//            users.remove(userId);
//            System.out.println("Connection closed for user: " + userId);
//        }
//    }
//
//    @Override
//    public void onMessage(WebSocket conn, String message) {
//        // Parse the message and handle different types of requests
//        String[] parts = message.split(":", 2);
//        if (parts.length != 2) return;
//
//        String type = parts[0];
//        String payload = parts[1];
//
//        switch (type) {
//            case "LOGIN":
//                handleLogin(conn, payload);
//                break;
//            case "CREATE_ROOM":
//                handleCreateRoom(conn, payload);
//                break;
//            case "JOIN_ROOM":
//                handleJoinRoom(conn, payload);
//                break;
//            case "OFFER":
//            case "ANSWER":
//            case "ICE_CANDIDATE":
//                handleSignalingMessage(conn, type, payload);
//                break;
//            case "LOGIN_SUCCESS":
//                System.out.println("Logged in successfully as: " + payload);
//                break;
//            case "ERROR":
//                System.out.println("Error received: " + payload);
//                break;
//            case "ROOM_JOINED":
//                System.out.println("Room joined: " + payload);
//                break;
//            case "ROOM_CREATED":
//                System.out.println("Room created: " + payload);
//                break;
//            default:
//                System.out.println("Unknown message type: " + message);
//                break;
//        }
//    }
//
//
//    private void handleLogin(WebSocket conn, String userId) {
//        if (!users.containsKey(userId)) {
//            users.put(userId, conn);
//            conn.send("LOGIN_SUCCESS:" + userId);
//            System.out.println("User logged in: " + userId);
//        } else {
//            conn.send("ERROR: User already logged in.");
//            System.out.println("User already logged in: " + userId);
//        }
//    }
//
//    private void handleCreateRoom(WebSocket conn, String roomId) {
//        String userId = getUserId(conn);
//        if (userId != null) {
//            // Debugging logs
//            System.out.println("User " + userId + " is trying to create room: " + roomId);
//
//            // Check if room already exists
//            if (roomUsers.containsKey(roomId)) {
//                conn.send("ERROR: Room already exists.");
//                System.out.println("Room " + roomId + " already exists. Cannot create.");
//            } else {
//                // Create a new room and add user to the room
//                userRooms.put(userId, roomId);
//                roomUsers.put(roomId, new ArrayList<>(List.of(userId)));
//                conn.send("ROOM_CREATED:" + roomId);
//                System.out.println("Room " + roomId + " created by user: " + userId);
//            }
//        } else {
//            System.out.println("Failed to create room: User not logged in.");
//            conn.send("ERROR: User not logged in.");
//        }
//    }
//
//
//
//    private void handleJoinRoom(WebSocket conn, String roomId) {
//        String userId = getUserId(conn);
//        if (userId != null) {
//            userRooms.put(userId, roomId);
//            roomUsers.get(roomId).add(userId); // Add user to the room's user list
//            conn.send("ROOM_JOINED:" + roomId);
//            System.out.println(userId + " joined room: " + roomId);
//
//            // Notify other users in the room
//            for (String user : roomUsers.get(roomId)) {
//                if (!user.equals(userId)) {
//                    WebSocket recipient = users.get(user);
//                    if (recipient != null) {
//                        recipient.send("USER_JOINED:" + userId);
//                    }
//                }
//            }
//        } else {
//            conn.send("ERROR: User not logged in.");
//        }
//    }
//
//
//    private void handleSignalingMessage(WebSocket sender, String type, String payload) {
//        String senderId = getUserId(sender);
//        String senderRoom = userRooms.get(senderId);
//
//        if (senderId != null && senderRoom != null) {
//            for (Map.Entry<String, String> entry : userRooms.entrySet()) {
//                if (entry.getValue().equals(senderRoom) && !entry.getKey().equals(senderId)) {
//                    WebSocket recipient = users.get(entry.getKey());
//                    if (recipient != null) {
//                        recipient.send(type + ":" + payload);
//                    }
//                }
//            }
//        }
//    }
//
//    private String getUserId(WebSocket conn) {
//        for (Map.Entry<String, WebSocket> entry : users.entrySet()) {
//            if (entry.getValue() == conn) {
//                return entry.getKey();
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public void onError(WebSocket conn, Exception ex) {
//        System.err.println("An error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
//    }
//
//    @Override
//    public void onStart() {
//        System.out.println("SignalingServer started on port: " + getPort());
//    }
//
//    public static void main(String[] args) {
//        int port = 8887; // You can change this port number if needed
//        SignalingServer server = new SignalingServer(port);
//        server.start();
//    }
//}

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


        notifyRoom(roomId, type, payload, senderId);
        System.out.println("Forwarding " + type + " message from " + senderId + " to room " + roomId);


    }

    private void notifyRoom(String roomId, String type, String payload) {
        notifyRoom(roomId, type, payload, null);
    }

    private void notifyRoom(String roomId, String type, String payload, String excludeUserId) {
        Set<String> roomMembers = roomUsers.get(roomId);
        if (roomMembers != null) {
            for (String userId : roomMembers) {
                if (excludeUserId == null || !userId.equals(excludeUserId)) {
                    WebSocket userConn = users.get(userId);
                    if (userConn != null && userConn.isOpen()) {
                        userConn.send(type + ":" + payload);
                        System.out.println("Sent " + type + " to user: " + userId);
                    }
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