package com.streamtui;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class VideoPlayerManager {
    private VideoPlayer videoPlayer;

    public VideoPlayerManager() {
        SwingUtilities.invokeLater(() -> {
            videoPlayer = new VideoPlayer();
        });
    }

    public void updateFrame(byte[] frameData, int width, int height) {
        SwingUtilities.invokeLater(() -> {
            videoPlayer.updateFrame(frameData, width, height);
        });
    }

    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }
}