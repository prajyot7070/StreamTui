package com.streamtui;

public class ClientB {
    public static void main(String[] args) {
        try {
            WebRTCHandler clientB = new WebRTCHandler();
            Thread.sleep(1000); // Wait for WebSocket connection

            System.out.println("Starting login process for Client B...");
            clientB.login("user2");
            Thread.sleep(500);

            System.out.println("Joining room...");
            clientB.joinRoom("testRoom");
            Thread.sleep(1000);

            // Check connection status after joining
            clientB.checkConnectionStatus();

            // Keep the main thread alive to observe the connection
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
