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
import dev.onvoid.webrtc.RTCPeerConnection.*;
import dev.onvoid.webrtc.media.MediaStream;

import java.util.ArrayList;
import java.util.List;

public class WebRTCHandler {
    private PeerConnectionFactory factory;
    private RTCPeerConnection peerConnection;
    private RTCConfiguration rtcConfig;

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
    }

}