package com.streamtui;

public class Main {
    public static void main(String[] args) {
        int port = 8887; // You can change this port number if needed
        SignalingServer server = new SignalingServer(port);
        server.start();
        System.out.println("SignalingServer started on port: " + port);
    }
}