package com.streamtui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class VideoPlayer extends JPanel {
    private BufferedImage currentFrame;

    public VideoPlayer() {
        JFrame frame = new JFrame("Live Streaming");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1020, 720);
        frame.add(this);
        frame.setVisible(true);
    }

    public void updateFrame(byte[] framedata, int width, int height) {
        try{
            ByteArrayInputStream bais = new ByteArrayInputStream(framedata);
            BufferedImage frameimage = ImageIO.read(bais);
            if (frameimage != null) {
                this.currentFrame = frameimage;
                repaint();  // Trigger re-rendering
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentFrame != null) {
            // Draw the current frame
            g.drawImage(currentFrame, 0, 0, this.getWidth(), this.getHeight(), null);
        }
    }

    public static void startVideoPlayer(VideoPlayer videoPlayer) {
        JFrame frame = new JFrame("Live Streaming");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(videoPlayer);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        VideoPlayer videoPlayer = new VideoPlayer();
    }
}
