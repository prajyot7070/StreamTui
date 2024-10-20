package com.streamtui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.util.Arrays;

public class WebRTCTuiApp {
    private final Screen screen;
    private final WindowBasedTextGUI textGUI;
    private final WebRTCHandler webRTCHandler;
    private Panel videoPanel;
    private Button endCallButton;

    public WebRTCTuiApp() throws IOException {
        screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();
        textGUI = new MultiWindowTextGUI(screen);
        webRTCHandler = new WebRTCHandler();
    }

    public void run() {
        showLoginWindow();
    }

    private void showLoginWindow() {
        BasicWindow window = new BasicWindow("Login");
        Panel panel = new Panel(new GridLayout(2));

        panel.addComponent(new Label("Username:"));
        TextBox usernameBox = new TextBox();
        panel.addComponent(usernameBox);

        panel.addComponent(new EmptySpace(new TerminalSize(0, 0)));
        Button loginButton = new Button("Login", () -> {
            String username = usernameBox.getText();
            if (!username.isEmpty()) {
                webRTCHandler.login(username);
                showMainMenu();
            }
        });
        panel.addComponent(loginButton);

        window.setComponent(panel);
        textGUI.addWindowAndWait(window);
    }

    private void showMainMenu() {
        BasicWindow window = new BasicWindow("Main Menu");
        Panel panel = new Panel(new GridLayout(1));

        Button createRoomButton = new Button("Create Room", this::showCreateRoomWindow);
        Button joinRoomButton = new Button("Join Room", this::showJoinRoomWindow);
        Button exitButton = new Button("Exit", () -> System.exit(0));

        panel.addComponent(createRoomButton);
        panel.addComponent(joinRoomButton);
        panel.addComponent(exitButton);

        window.setComponent(panel);
        textGUI.addWindowAndWait(window);
    }

    private void showCreateRoomWindow() {
        BasicWindow window = new BasicWindow("Create Room");
        Panel panel = new Panel(new GridLayout(2));

        panel.addComponent(new Label("Room Name:"));
        TextBox roomNameBox = new TextBox();
        panel.addComponent(roomNameBox);

        panel.addComponent(new EmptySpace(new TerminalSize(0, 0)));
        Button createButton = new Button("Create", () -> {
            String roomName = roomNameBox.getText();
            if (!roomName.isEmpty()) {
                webRTCHandler.createRoom(roomName);
                showStreamingWindow(roomName, true);
            }
        });
        panel.addComponent(createButton);

        window.setComponent(panel);
        textGUI.addWindowAndWait(window);
    }

    private void showJoinRoomWindow() {
        BasicWindow window = new BasicWindow("Join Room");
        Panel panel = new Panel(new GridLayout(2));

        panel.addComponent(new Label("Room Name:"));
        TextBox roomNameBox = new TextBox();
        panel.addComponent(roomNameBox);

        panel.addComponent(new EmptySpace(new TerminalSize(0, 0)));
        Button joinButton = new Button("Join", () -> {
            String roomName = roomNameBox.getText();
            if (!roomName.isEmpty()) {
                webRTCHandler.joinRoom(roomName);
                showStreamingWindow(roomName, false);
            }
        });
        panel.addComponent(joinButton);

        window.setComponent(panel);
        textGUI.addWindowAndWait(window);
    }

    private void showStreamingWindow(String roomName, boolean isHost) {
        BasicWindow window = new BasicWindow("Streaming: " + roomName);
        Panel mainPanel = new Panel(new GridLayout(1));

        videoPanel = new Panel(new GridLayout(1));
        videoPanel.setPreferredSize(new TerminalSize(50, 20));
        mainPanel.addComponent(videoPanel);

        endCallButton = new Button("End Call", this::endCall);
        mainPanel.addComponent(endCallButton);

        window.setComponent(mainPanel);
        textGUI.addWindow(window);

        startStreaming(isHost);
    }

    private void startStreaming(boolean isHost) {
        webRTCHandler.setupLocalMedia();
        if (isHost) {
            webRTCHandler.createOffer();
        }
        updateVideoPanel("Streaming...");
    }

    private void updateVideoPanel(String content) {
        videoPanel.removeAllComponents();
        videoPanel.addComponent(new Label(content));
        try {
            textGUI.updateScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void endCall() {
        // Implement call ending logic here
        updateVideoPanel("Call ended");
        endCallButton.setEnabled(false);
    }

    public static void main(String[] args) {
        try {
            WebRTCTuiApp app = new WebRTCTuiApp();
            app.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}