//package com.streamtui.client;
//
//
//import com.googlecode.lanterna.TerminalSize;
//import com.googlecode.lanterna.TextColor;
//import com.googlecode.lanterna.gui2.*;
//import com.googlecode.lanterna.screen.Screen;
//import com.googlecode.lanterna.screen.TerminalScreen;
//import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
//import com.googlecode.lanterna.terminal.Terminal;
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.handshake.ServerHandshake;
//import com.google.gson.Gson;
//import com.streamtui.server.SignalingMessage;
//import java.net.URI;
//import java.util.Arrays;
//
//public class TerminalClient {
//    private Screen screen;
//    private WindowBasedTextGUI textGUI;
//    private final Gson gson = new Gson();
//    private WebSocketClient wsClient;
//    private String currentRoom;
//
//    public void start() {
//        try {
//            Terminal terminal = new DefaultTerminalFactory().createTerminal();
//            screen = new TerminalScreen(terminal);
//            screen.startScreen();
//
//            textGUI = new MultiWindowTextGUI(screen);
//            showMainMenu();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void showMainMenu() {
//        BasicWindow window = new BasicWindow("WebRTC Terminal Streaming");
//        Panel panel = new Panel(new GridLayout(2));
//
//        Button createRoomButton = new Button("Create Room", this::createRoom);
//        Button joinRoomButton = new Button("Join Room", this::showJoinRoomDialog);
//
//        panel.addComponent(createRoomButton);
//        panel.addComponent(joinRoomButton);
//
//        window.setComponent(panel);
//        textGUI.addWindowAndWait(window);
//    }
//
//    private void createRoom() {
//        connectToServer();
//        wsClient.send(gson.toJson(new SignalingMessage("CREATE_ROOM", "")));
//    }
//
//    private void showJoinRoomDialog() {
//        BasicWindow dialog = new BasicWindow("Join Room");
//        Panel panel = new Panel(new GridLayout(2));
//
//        TextBox roomIdInput = new TextBox();
//        Button joinButton = new Button("Join", () -> {
//            connectToServer();
//            wsClient.send(gson.toJson(new SignalingMessage("JOIN_ROOM", roomIdInput.getText())));
//            dialog.close();
//        });
//
//        panel.addComponent(new Label("Room ID:"));
//        panel.addComponent(roomIdInput);
//        panel.addComponent(joinButton);
//
//        dialog.setComponent(panel);
//        textGUI.addWindowAndWait(dialog);
//    }
//
//    private void connectToServer() {
//        try {
//            wsClient = new WebSocketClient(new URI("ws://localhost:8887")) {
//                @Override
//                public void onOpen(ServerHandshake handshakedata) {
//                    System.out.println("Connected to signaling server");
//                }
//
//                @Override
//                public void onMessage(String message) {
//                    SignalingMessage msg = gson.fromJson(message, SignalingMessage.class);
//                    handleSignalingMessage(msg);
//                }
//
//                @Override
//                public void onClose(int code, String reason, boolean remote) {
//                    System.out.println("Disconnected from signaling server");
//                }
//
//                @Override
//                public void onError(Exception ex) {
//                    ex.printStackTrace();
//                }
//            };
//            wsClient.connect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void handleSignalingMessage(SignalingMessage msg) {
//        switch (msg.type) {
//            case "ROOM_CREATED":
//                currentRoom = msg.payload;
//                showRoomScreen("Room created with ID: " + currentRoom);
//                break;
//            case "JOINED_ROOM":
//                currentRoom = msg.payload;
//                showRoomScreen("Joined room with ID: " + currentRoom);
//                break;
//            case "ERROR":
//                showError(msg.payload);
//                break;
//        }
//    }
//
//    private void showRoomScreen(String message) {
//        BasicWindow roomWindow = new BasicWindow("Room");
//        Panel panel = new Panel(new GridLayout(1));
//
//        panel.addComponent(new Label(message));
//        panel.addComponent(new Button("Leave Room", () -> {
//            wsClient.close();
//            roomWindow.close();
//            showMainMenu();
//        }));
//
//        roomWindow.setComponent(panel);
//        textGUI.addWindowAndWait(roomWindow);
//    }
//
//    private void showError(String message) {
//        BasicWindow errorWindow = new BasicWindow("Error");
//        Panel panel = new Panel(new GridLayout(1));
//
//        panel.addComponent(new Label(message));
//        panel.addComponent(new Button("OK", errorWindow::close));
//
//        errorWindow.setComponent(panel);
//        textGUI.addWindowAndWait(errorWindow);
//    }
//}
package com.streamtui.client;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.google.gson.Gson;
import java.net.URI;
import com.streamtui.server.SignalingMessage;
import java.util.Arrays;

public class TerminalClient {
    private Screen screen;
    private WindowBasedTextGUI textGUI;
    private final Gson gson = new Gson();
    private WebSocketClient wsClient;
    private String currentRoom;

    public void start() {
        try {
            Terminal terminal = new DefaultTerminalFactory().createTerminal();
            screen = new TerminalScreen(terminal);
            screen.startScreen();

            textGUI = new MultiWindowTextGUI(screen);
            showMainMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMainMenu() {
        BasicWindow window = new BasicWindow("WebRTC Terminal Streaming");
        Panel panel = new Panel(new GridLayout(2));

        Button createRoomButton = new Button("Create Room", this::createRoom);
        Button joinRoomButton = new Button("Join Room", this::showJoinRoomDialog);

        panel.addComponent(createRoomButton);
        panel.addComponent(joinRoomButton);

        window.setComponent(panel);
        textGUI.addWindowAndWait(window);
    }

    private void createRoom() {
        connectToServer(() -> {
            wsClient.send(gson.toJson(new SignalingMessage("CREATE_ROOM", "")));
        });
    }

    private void showJoinRoomDialog() {
        BasicWindow dialog = new BasicWindow("Join Room");
        Panel panel = new Panel(new GridLayout(2));

        TextBox roomIdInput = new TextBox();
        Button joinButton = new Button("Join", () -> {
            connectToServer(() -> {
                wsClient.send(gson.toJson(new SignalingMessage("JOIN_ROOM", roomIdInput.getText())));
            });
            dialog.close();
        });

        panel.addComponent(new Label("Room ID:"));
        panel.addComponent(roomIdInput);
        panel.addComponent(joinButton);

        dialog.setComponent(panel);
        textGUI.addWindowAndWait(dialog);
    }

    private void connectToServer(Runnable onConnected) {
        try {
            wsClient = new WebSocketClient(new URI("ws://localhost:8887")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to signaling server");
                    onConnected.run(); // Execute the callback when the connection is open
                }

                @Override
                public void onMessage(String message) {
                    SignalingMessage msg = gson.fromJson(message, SignalingMessage.class);
                    handleSignalingMessage(msg);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from signaling server");
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            wsClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSignalingMessage(SignalingMessage msg) {
        switch (msg.type) {
            case "ROOM_CREATED":
                currentRoom = msg.payload;
                showRoomScreen("Room created with ID: " + currentRoom);
                break;
            case "JOINED_ROOM":
                currentRoom = msg.payload;
                showRoomScreen("Joined room with ID: " + currentRoom);
                break;
            case "ERROR":
                showError(msg.payload);
                break;
        }
    }

    private void showRoomScreen(String message) {
        BasicWindow roomWindow = new BasicWindow("Room");
        Panel panel = new Panel(new GridLayout(1));

        panel.addComponent(new Label(message));
        panel.addComponent(new Button("Leave Room", () -> {
            wsClient.close();
            roomWindow.close();
            showMainMenu();
        }));

        roomWindow.setComponent(panel);
        textGUI.addWindowAndWait(roomWindow);
    }

    private void showError(String message) {
        BasicWindow errorWindow = new BasicWindow("Error");
        Panel panel = new Panel(new GridLayout(1));

        panel.addComponent(new Label(message));
        panel.addComponent(new Button("OK", errorWindow::close));

        errorWindow.setComponent(panel);
        textGUI.addWindowAndWait(errorWindow);
    }
}
