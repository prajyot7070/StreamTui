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
import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.video.*;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

import javax.swing.JPanel;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.embed.swing.JFXPanel;
import javafx.scene.layout.StackPane;


import java.nio.ByteBuffer;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;

public class WebRTCHandler {
    private PeerConnectionFactory factory;
    private RTCPeerConnection peerConnection;
    private RTCConfiguration rtcConfig;
    private WebSocketClient webSocketClient;
    private MediaStreamTrack mediaStreamTrack;
    private VideoCapture videoCapture;
    private VideoDeviceSource videoDeviceSource;
    private ImageView imageView;
    private WritableImage writableImage;
    private Stage stage;


    //Subclasss that extends the  VideoDevice and provides a public constructor
    public class CustomVideoDevice extends VideoDevice {
        public CustomVideoDevice(String name, String id) {
            super(name, id);
        }
    }

    public class CustomVideoCaptureCapability extends VideoCaptureCapability {
        public CustomVideoCaptureCapability(int fps, int height, int width) {
            super(fps, height, width);
        }
    }

    // Constructor
    // Constructor
    public WebRTCHandler() {
        factory = new PeerConnectionFactory();
        rtcConfig = new RTCConfiguration();
        List<RTCIceServer> iceServers = new ArrayList<>();
        RTCIceServer stunServer = new RTCIceServer();
        stunServer.urls = List.of("stun:stun.l.google.com:19302");
        iceServers.add(stunServer);
        rtcConfig.iceServers = iceServers;

        initializePeerConnection();
        setupWebSocket();
    }

    private void initializePeerConnection() {
        peerConnection = factory.createPeerConnection(rtcConfig, new PeerConnectionObserver() {
            @Override
            public void onIceCandidate(RTCIceCandidate rtcIceCandidate) {
                System.out.println("New ICE Candidate: " + rtcIceCandidate.toString());
                String iceCandidateMessage = String.format("%s;%s;%d",
                        rtcIceCandidate.sdp, rtcIceCandidate.sdpMid, rtcIceCandidate.sdpMLineIndex);
                sendToSignalingServer("ICE_CANDIDATE", iceCandidateMessage);
            }

            @Override
            public void onConnectionChange(RTCPeerConnectionState state) {
                System.out.println("Connection state changed to: " + state);
            }

            @Override
            public void onIceConnectionChange(RTCIceConnectionState state) {
                System.out.println("ICE connection state changed to: " + state);
            }

            @Override
            public void onSignalingChange(RTCSignalingState state) {
                System.out.println("Signaling state changed to: " + state);
            }

            @Override
            public void onTrack(RTCRtpTransceiver transceiver) {
                if (transceiver.getReceiver().getTrack() instanceof VideoTrack) {
                    VideoTrack remoteVideoTrack = (VideoTrack) transceiver.getReceiver().getTrack();
                    System.out.println("Received remote video track on Client B.");

                    // Display the remote video on Client B
                    handleRemoteVideo(remoteVideoTrack);
                }
            }
        });
    }

    private void setupWebSocket() {
        try {
            URI uri = new URI("ws://localhost:8887");
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    System.out.println("WebSocket connection opened");
                }

                @Override
                public void onMessage(String message) {
                    handleSignalingMessage(message);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    System.out.println("WebSocket connection closed");
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            e.printStackTrace();
        }
    }

    public void setupLocalMedia() {

        videoDeviceSource = new VideoDeviceSource();
        CustomVideoDevice videoDevice = new CustomVideoDevice("UVC Camera (046d:0825)", "/dev/video0");
        VideoCaptureCapability capability = new VideoCaptureCapability(640, 480, 30);

        videoCapture = new VideoCapture();
        videoCapture.setVideoCaptureDevice(videoDevice);
        videoCapture.setVideoCaptureCapability(capability);


        videoDeviceSource.setVideoCaptureDevice(videoDevice);
        videoDeviceSource.setVideoCaptureCapability(capability);

        mediaStreamTrack = factory.createVideoTrack("video_track", videoDeviceSource);

        peerConnection.addTrack(mediaStreamTrack, List.of(mediaStreamTrack.getId()));

        try{videoDeviceSource.start();}catch(Exception e){e.getMessage();}


        System.out.println("Local media setup completed");
    }

    public void stopLocalMedia() {
        if(videoDeviceSource != null){
            videoDeviceSource.stop();
            System.out.println("Local media stopped");
            videoDeviceSource.dispose();
        }
    }

    private void handleSignalingMessage(String message) {
        String[] parts = message.split(":", 2);
        String type = parts[0];
        String payload = parts[1];

        switch (type) {
            case "LOGIN_SUCCESS":
                System.out.println("Logged in successfully as: " + payload);
                break;
            case "ROOM_JOINED":
            case "ROOM_CREATED":
                System.out.println(type + ": " + payload);
                break;
            case "USER_JOINED":
                System.out.println("User joined: " + payload);
                createOffer(); // Automatically create an offer when a new user joins
                break;
            case "OFFER":
                handleRemoteSDPOffer(payload);
                break;
            case "ANSWER":
                handleRemoteSDPAnswer(payload);
                break;
            case "ICE_CANDIDATE":
                String[] iceCandidateParts = payload.split(";");
                RTCIceCandidate iceCandidate = new RTCIceCandidate(
                        iceCandidateParts[1],
                        Integer.parseInt(iceCandidateParts[2]),
                        iceCandidateParts[0]
                );
                handleRemoteICECandidate(iceCandidate);
                break;
            default:
                System.err.println("Unknown message type: " + type);
        }
    }

    // TESTING CODE
    public void login(String userId) {
        System.out.println("Logging in user: " + userId);
        sendToSignalingServer("LOGIN", userId);
    }


    public void createRoom(String roomId) {
        if (webSocketClient.isOpen()) {
            sendToSignalingServer("CREATE_ROOM", roomId);
            System.out.println("Request to create room sent: " + roomId);
        } else {
            System.err.println("WebSocket connection is not open. Cannot create room.");
        }
    }

    public void joinRoom(String roomId) {
        sendToSignalingServer("JOIN_ROOM", roomId);
    }

    public void checkConnectionStatus() {
        if (peerConnection != null) {
            System.out.println("Connection state: " + peerConnection.getConnectionState());
            System.out.println("ICE connection state: " + peerConnection.getIceConnectionState());
            System.out.println("Signaling state: " + peerConnection.getSignalingState());
        } else {
            System.out.println("PeerConnection is null");
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
                peerConnection.setLocalDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        System.out.println("OFFER | SDP created :" + rtcSessionDescription.sdp);
                        sendToSignalingServer("OFFER", rtcSessionDescription.sdp);
                    }

                    @Override
                    public void onFailure(String err) {
                        System.err.println("Failed to set local SDP: " + err);
                    }
                });
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
                peerConnection.setLocalDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        System.out.println("ANSWER | SDP created :" + rtcSessionDescription.sdp);
                        sendToSignalingServer("ANSWER", rtcSessionDescription.sdp);
                    }

                    @Override
                    public void onFailure(String s) {
                        System.err.println("Failed to set local SDP: " + s);
                    }
                });
            }

            @Override
            public void onFailure(String s) {
                System.err.println("Failed to create the answer: " + s);
            }
        });
    }

    //Handle incoming SDP offer and create an answer
    public void handleRemoteSDPOffer(String sdp){
        RTCSessionDescription remoteSDP = new RTCSessionDescription(RTCSdpType.OFFER, sdp);
        peerConnection.setRemoteDescription(remoteSDP, new SetSessionDescriptionObserver(){
            @Override
            public void onSuccess() {
                System.out.println("Recived SDP offer : " + remoteSDP.toString());
                System.out.println("Remote SDP set successfully");
                createAnswer();
            }

            @Override
            public void onFailure(String err) {
                System.out.println("Failed to set remote SDP answer : - " + remoteSDP.toString() + "\n Error : - " + err);
            }
        });
    }

    //Set the remote SDP answer to the peerConnetion
    public void handleRemoteSDPAnswer(String sdp){
        RTCSessionDescription remoteSDP = new RTCSessionDescription(RTCSdpType.ANSWER, sdp);
        peerConnection.setRemoteDescription(remoteSDP, new SetSessionDescriptionObserver(){
            @Override
            public void onSuccess() {
                System.out.println("Recived SDP answer : " + remoteSDP.toString());
                System.out.println("Remote SDP set successfully");
            }

            @Override
            public void onFailure(String err) {
                System.out.println("Failed to set remote SDP answer : - " + remoteSDP.toString() + "\n Error : - " + err);
            }
        });

    }


    //Set the remote ICE candidate
    public void handleRemoteICECandidate(RTCIceCandidate iceCandidate) {
        if (iceCandidate != null) {
            System.out.println("Received ICE candidate: " + iceCandidate.toString());
            peerConnection.addIceCandidate(iceCandidate);

        } else {
            System.out.println("ICE candidate is null");
        }
    }


    // Send signaling messages (SDP or ICE candidates) to the signaling server
    private void sendToSignalingServer(String type, String payload) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(type + ":" + payload);
        }
    }

    public void handleRemoteVideo(VideoTrack remoteVideoTrack) {
        if (remoteVideoTrack == null) {
            System.err.println("Remote video track is null");
            return;
        }

        remoteVideoTrack.addSink(new VideoTrackSink() {
            @Override
            public void onVideoFrame(VideoFrame frame) {
                try {
                    VideoFrameBuffer buffer = frame.buffer;
                    int width = buffer.getWidth();
                    int height = buffer.getHeight();
                    System.out.println("Received frame: " + width + "x" + height);

                    // Print additional frame information
                    System.out.println("Frame rotation: " + frame.rotation);
                    System.out.println("Frame timestamp: " + frame.timestampNs);

                    // Check buffer type
                    if (buffer instanceof I420Buffer) {
                        System.out.println("Buffer type: I420");
                        // You can add more specific I420 buffer handling here if needed
                    } else {
                        System.out.println("Buffer type: " + buffer.getClass().getSimpleName());
                    }

                    // Don't try to access or process buffer data yet
                } catch (Exception e) {
                    System.err.println("Error processing video frame: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // Ensure buffer is always released
                    if (frame.buffer != null) {
                        frame.buffer.release();
                    }
                }
            }
        });

        System.out.println("Remote video handler set up");
    }

}

