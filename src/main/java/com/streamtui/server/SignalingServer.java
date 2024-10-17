package com.streamtui.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import com.google.gson.Gson;
import java.net.InetSocketAddress;
import java.util.*;

public class SignalingServer extends WebSocketServer {
    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<WebSocket, String> userRooms = new HashMap<>();
    private final Gson gson = new Gson();

    public SignalingServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String roomId = userRooms.get(conn);
        if (roomId != null) {
            Room room = rooms.get(roomId);
            if (room != null) {
                room.removeUser(conn);
                if (room.isEmpty()) {
                    rooms.remove(roomId);
                }
            }
            userRooms.remove(conn);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        SignalingMessage sigMsg = gson.fromJson(message, SignalingMessage.class);

        switch (sigMsg.type) {
            case "CREATE_ROOM":
                String roomId = UUID.randomUUID().toString().substring(0, 8);
                rooms.put(roomId, new Room());
                rooms.get(roomId).addUser(conn);
                userRooms.put(conn, roomId);
                conn.send(gson.toJson(new SignalingMessage("ROOM_CREATED", roomId)));
                break;

            case "JOIN_ROOM":
                Room room = rooms.get(sigMsg.payload);
                if (room != null && room.canJoin()) {
                    room.addUser(conn);
                    userRooms.put(conn, sigMsg.payload);
                    conn.send(gson.toJson(new SignalingMessage("JOINED_ROOM", sigMsg.payload)));
                    room.broadcastToOthers(conn, gson.toJson(new SignalingMessage("USER_JOINED", "")));
                } else {
                    conn.send(gson.toJson(new SignalingMessage("ERROR", "Room not found or full")));
                }
                break;

            case "OFFER":
            case "ANSWER":
            case "ICE_CANDIDATE":
                String userRoomId = userRooms.get(conn);
                if (userRoomId != null) {
                    Room userRoom = rooms.get(userRoomId);
                    if (userRoom != null) {
                        userRoom.broadcastToOthers(conn, message);
                    }
                }
                break;
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error occurred on connection " + conn.getRemoteSocketAddress());
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Signaling server started on port " + getPort());
    }
}
