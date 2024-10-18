package com.streamtui;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "server":
                    runServer();
                    break;
                case "A":
                    runTestA();
                    break;
                case "B":
                    runTestB();
                    break;
                default:
                    System.out.println("Invalid argument. Use 'server' or 'test'.");
            }
        } else {
            System.out.println("Please specify 'server' or 'test' as an argument.");
        }
    }

    private static void runServer() {
        int port = 8887;
        SignalingServer server = new SignalingServer(port);
        server.start();
        System.out.println("SignalingServer started on port: " + port);
    }

    private static void runTestA() {
        ClientA.main(new String[]{});
    }
    private static void runTestB() {
        ClientB.main(new String[]{});
    }
}