package com.streamtui;

public class WebRTCTest {
    public static void main(String[] args) {
        try {
            WebRTCHandler client1 = new WebRTCHandler();
            Thread.sleep(1000); // Wait for WebSocket connection

            WebRTCHandler client2 = new WebRTCHandler();
            Thread.sleep(1000);

            System.out.println("Starting login process...");
            client1.login("user1");
            Thread.sleep(500);
            client2.login("user2");
            Thread.sleep(500);

            System.out.println("Creating and joining room...");
            client1.createRoom("testRoom");
            Thread.sleep(1000);
            client2.joinRoom("testRoom");

            // Keep the main thread alive to observe the connection
            Thread.sleep(10000);
            client1.checkConnectionStatus();
            client2.checkConnectionStatus();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
