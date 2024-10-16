package com.streamtui;

public class WebRTCTest {
    public static void main(String[] args) {
        // Create two WebRTCHandler instances
        WebRTCHandler client1 = new WebRTCHandler();
        WebRTCHandler client2 = new WebRTCHandler();

        // Simulate login and room creation/joining
        client1.login("user1");
        client2.login("user2");

        client1.createRoom("testRoom");
        client2.joinRoom("testRoom");

        // Initiate the connection
        client1.createOffer();

        // Wait for the connection to establish
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check connection status
        client1.checkConnectionStatus();
        client2.checkConnectionStatus();
    }
}