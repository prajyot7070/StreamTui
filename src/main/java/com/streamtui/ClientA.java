package com.streamtui;

public class ClientA {
    public static void main(String[] args) {
        try {
            WebRTCHandler clientA = new WebRTCHandler();
            Thread.sleep(1000); // Wait for WebSocket connection

            System.out.println("Starting login process for Client A...");
            clientA.login("user1");
            Thread.sleep(500);

            System.out.println("Creating room...");
            clientA.createRoom("testRoom");
            Thread.sleep(6000);

            // Check connection status after room creation
            clientA.checkConnectionStatus();
            clientA.createOffer();
            // Keep the main thread alive to observe the connection
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
