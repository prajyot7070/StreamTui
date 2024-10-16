package com.streamtui;


// TODO: Implement WebRTC client step by step

// 1. Initialize WebRTC PeerConnection Factory
//    - Create PeerConnectionFactory.
//    - Set up RTCConfiguration (add STUN server).
// 2. Create PeerConnection
//    - Use the factory to create PeerConnection with observer callbacks.
//    - Listen for ICE candidates and connection state changes.

// 3. Create an SDP Offer (for the initiating peer)
//    - Call createOffer() to generate an SDP offer.
//    - Set local description with the offer.
//    - Send the SDP offer to the signaling server.

// 4. Handle Remote SDP Answer
//    - Receive the SDP answer from the remote peer via the signaling server.
//    - Set the remote description with the received answer.

// 5. Handle ICE Candidate Exchange
//    - Send local ICE candidates to the signaling server.
//    - Receive remote ICE candidates via the signaling server.
//    - Add received ICE candidates to the peer connection.

// 6. Test the connection
//    - Exchange SDP offer/answer and ICE candidates between peers.
//    - Ensure the signaling server facilitates proper communication between clients.

import dev.onvoid.webrtc.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;

public class WebRTCHandler {
    private PeerConnectionFactory factory;
    private RTCPeerConnection peerConnection;
    private RTCConfiguration rtcConfig;
    private WebSocketClient webSocketClient;

    // Constructor
    public WebRTCHandler() {
        factory = new PeerConnectionFactory();
        rtcConfig = new RTCConfiguration();
        List<RTCIceServer> iceServers = new ArrayList<>();
        //Adding public STUN server
        RTCIceServer stunServer = new RTCIceServer();
        stunServer.urls = List.of("stun:stun.l.google.com:19302");
        iceServers.add(stunServer);

        rtcConfig.iceServers = iceServers;

        //Create a peer connection
        peerConnection = factory.createPeerConnection(rtcConfig, new PeerConnectionObserver() {
            @Override
            public void onIceCandidate(RTCIceCandidate rtcIceCandidate) {
                System.out.println("New ICE Candidate : " + rtcIceCandidate.toString());
                sendIcetoSignaling(rtcIceCandidate);
            }

            @Override
            public void onConnectionChange(RTCPeerConnectionState state) {
                PeerConnectionObserver.super.onConnectionChange(state);
                System.out.println("Connection state changed to: " + state);
            }

            @Override
            public void onIceConnectionChange(RTCIceConnectionState state) {
                PeerConnectionObserver.super.onIceConnectionChange(state);
                System.out.println("ICE connection state changed to: " + state);
            }

            @Override
            public void onSignalingChange(RTCSignalingState state) {
                PeerConnectionObserver.super.onSignalingChange(state);
                System.out.println("Signaling state changed to: " + state);
            }

        });

        try{
            URI uri = new URI("ws://localhost:8887");
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    System.out.println("Connection opened");
                }

                @Override
                public void onMessage(String s) {
                    System.out.println("Message received: " + s);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    System.out.println("Connection closed");
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e){
            e.printStackTrace();
        }
    }



    //Create an SDP offer
    public void createOffer() {
        if (peerConnection == null) {
            System.err.println("PeerConnection is null");
            return;
        }
        peerConnection.createOffer(new RTCOfferOptions(),new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription rtcSessionDescription) {
                System.out.println("SDP offer created locally");
                sendToSignalingServer(rtcSessionDescription);
            }

            @Override
            public void onFailure(String error) {
                System.err.println("Failed to create the offer: " + error);
            }
        });
    }

    //Create an SDP answer
    public void createAnswer() {
        if (peerConnection == null) {
            System.err.println("PeerConnection is null");
            return;
        }
        peerConnection.createAnswer(new RTCAnswerOptions(),new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription rtcSessionDescription) {
                System.out.println("SDP answer created locally");
                sendToSignalingServer(rtcSessionDescription);
            }

            @Override
            public void onFailure(String s) {
                System.err.println("Failed to create the answer: " + s);
            }
        });
    }

    //Set the remote SDP answer to the peerConnetion
    public void handleRemoteSDPAnswer(String sdp, String type){
        RTCSessionDescription remoteSDP = new RTCSessionDescription(
                type.equals("offer") ? RTCSdpType.OFFER : RTCSdpType.ANSWER,
                sdp
        );
        peerConnection.setRemoteDescription(remoteSDP, new SetSessionDescriptionObserver(){
            @Override
            public void onSuccess() {
                System.out.println("Remote SDP set successfully");
            }

            @Override
            public void onFailure(String s) {
                System.out.println("Failed to set remote SDP: " + remoteSDP.toString());
            }
        });

    }

    //Add the ICE candidate received from the signaling server to the peer connection.
    public void handleRemoteICECandidate(RTCIceCandidate remoteIceCandidate){
        peerConnection.addIceCandidate(remoteIceCandidate);
    }

    //Send SDP to Signaling server
    private void sendToSignalingServer(RTCSessionDescription rtcSessionDescription) {
        if (rtcSessionDescription == null || rtcSessionDescription.sdp == null) {
            System.err.println("RTCSessionDescription or SDP is null");
            return;
        }
        //logic to send the offer to Signaling server
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(rtcSessionDescription.sdp);
        }
        System.out.println("Sending Offer to signaling server: " + rtcSessionDescription.sdp);
    }

    //Send ICE to Signaling server
    private void sendIcetoSignaling(RTCIceCandidate rtcIceCandidate) {
        if (rtcIceCandidate != null) {
            System.out.println("Sending ICE to signaling server: " + rtcIceCandidate.toString());
            //logic to send ICE candidate to SignalingServer . . .
        }
    }

}