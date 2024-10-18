//package com.streamtui;
//
//public class WebRTCTest {
//    public static void main(String[] args) {
//        // Create two WebRTCHandler instances
//        WebRTCHandler client1 = new WebRTCHandler();
//        WebRTCHandler client2 = new WebRTCHandler();
//System.out.println("login strted");
//        // Simulate login and room creation/joining
//        client1.login("user1");
//        client2.login("user2");
//        System.out.println("login ended");
//        System.out.println("creation strted");
//        client1.createRoom("testRoom");
//        System.out.println("creation ended");
//        System.out.println("joining stterd");
//        client2.joinRoom("testRoom");
//
//
//        // Initiate the connection
//        client1.createOffer();
//        //client1.initiateConnection();
//        // Wait for the connection to establish
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        // Check connection status
//        client1.checkConnectionStatus();
//        client2.checkConnectionStatus();
//    }
//}
//public class WebRTCTest {
//    public static void main(String[] args) {
//        try {
//            WebRTCHandler client1 = new WebRTCHandler();
//            Thread.sleep(1000); // Wait for WebSocket connection
//
//            WebRTCHandler client2 = new WebRTCHandler();
//            Thread.sleep(1000);
//
//            System.out.println("Starting login process...");
//            client1.login("user1");
//            Thread.sleep(500);
//            client2.login("user2");
//            Thread.sleep(500);
//
//            System.out.println("Creating and joining room...");
//            client1.createRoom("testRoom");
//            Thread.sleep(1000);
//            client2.joinRoom("testRoom");
//
//            // Keep the main thread alive to observe the connection
//            Thread.sleep(10000);
//            client1.checkConnectionStatus();
//            client2.checkConnectionStatus();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }
//}
package com.streamtui;

import java.util.Scanner;

public class WebRTCTest {
    public static void main(String[] args) {
        // Create a WebRTCHandler instance
        WebRTCHandler client = new WebRTCHandler();
        Scanner scanner = new Scanner(System.in);
        String userId;

        // Input user ID
        System.out.print("Enter user ID: ");
        userId = scanner.nextLine();
        client.login(userId);

        boolean running = true;

        while (running) {
            System.out.println("\nMenu:");
            System.out.println("1. Create Room");
            System.out.println("2. Join Room");
            System.out.println("3. Create Offer");
            System.out.println("4. Check Connection Status");
            System.out.println("5. Exit");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter room ID to create: ");
                    String roomIdToCreate = scanner.nextLine();
                    client.createRoom(roomIdToCreate);
                    break;
                case 2:
                    System.out.print("Enter room ID to join: ");
                    String roomIdToJoin = scanner.nextLine();
                    client.joinRoom(roomIdToJoin);
                    break;
                case 3:
                    System.out.println("Creating offer...");
                    client.createOffer();
                    break;
                case 4:
                    System.out.println("Checking connection status...");
                    client.checkConnectionStatus();
                    break;
                case 5:
                    running = false; // Exit the loop
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }

            // Wait for a moment to allow any asynchronous operations to complete
            try {
                Thread.sleep(1000); // Adjust as necessary for your setup
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        scanner.close();
    }
}
