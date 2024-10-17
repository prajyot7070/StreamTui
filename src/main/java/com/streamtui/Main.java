package com.streamtui;

import com.streamtui.client.TerminalClient;
import com.streamtui.server.SignalingServer;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--server")) {
            // Start server mode
            SignalingServer server = new SignalingServer(8887);
            server.start();
        } else {
            // Start client mode
            TerminalClient client = new TerminalClient();
            client.start();
        }
    }
}