package org.example;

import org.example.entities.QueryResult;
import org.example.util.Constants;
import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// A customized player that can display both query and result videos
// It plays query video on the left and result video on the right
// It also plays the audio of the result video
public class ResultPlayer {
    private QueryResult queryResult;
    private JFrame frameWindow;
    private JPanel queryVideoPanel;
    private JPanel databaseVideoPanel;
    private VideoReader queryVideoReader;
    private VideoReader databaseVideoReader;
    private Clip databaseAudioClip;
    private long databaseVideoMicrosecondPosition;
    private boolean isPaused = true;
    private int frameCounter;
    private boolean initialezed = false;

    ResultPlayer(QueryResult queryResult) {
        this.queryResult = queryResult;
    }

    private ActionListener resetButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!initialezed) return;
            initialezed = false;
            isPaused = true;

            queryVideoPanel.removeAll();
            databaseVideoPanel.removeAll();

            queryVideoPanel.add(new JLabel("Result found! Video name: "+ queryResult.getDatabaseVideo().getVideoName() +" Start frame: " + queryResult.getStartTimeInFrames() + ",  Video Loading..."));

            frameWindow.revalidate();
            frameWindow.repaint();

            new Thread(() -> {
                databaseAudioClip.stop();
                databaseVideoMicrosecondPosition = (long) queryResult.getStartTimeInSeconds() * 1_000_000;
                databaseAudioClip.setMicrosecondPosition(databaseVideoMicrosecondPosition);

                queryVideoReader.seek(0.0);
                databaseVideoReader.seek(databaseVideoMicrosecondPosition / 1000.0);


                loadNextFrame();
                frameCounter = queryResult.getStartTimeInFrames();

                initialezed = true;
            }).start();

            System.out.println("Reset button clicked!");
        }
    };

    private ActionListener playButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!initialezed) return;
            if (!isPaused) return;
            isPaused = false;
            databaseAudioClip.setMicrosecondPosition(databaseVideoMicrosecondPosition);
            databaseAudioClip.start();
            System.out.println("Play action preformed, time: " + databaseVideoMicrosecondPosition / 1_000_000.0 + "s");
        }
    };

    private ActionListener pauseButtonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!initialezed) return;
            if (isPaused) return;
            isPaused = true;
            databaseAudioClip.stop();
            databaseVideoMicrosecondPosition = databaseAudioClip.getMicrosecondPosition();
//            databaseVideoReader.seek(databaseVideoMicrosecondPosition / 1000.0);
            // too extensive to seek, so we don't do it anymore
            // we need to find another way to sync the video and audio
            System.out.println("Pause action preformed, time: " + databaseVideoMicrosecondPosition / 1_000_000.0 + "s");
        }
    };

    private void uiSetup() {
        frameWindow = new JFrame("Video Player");
        frameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameWindow.setSize(800, 480);

        // Create a panel for the button
        queryVideoPanel = new JPanel();
        queryVideoPanel.add(new JLabel("Result found! Video name: "+ queryResult.getDatabaseVideo().getVideoName() +" Start frame: " + queryResult.getStartTimeInFrames() + ",  Video Loading..."));
        databaseVideoPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        JButton resetButton = new JButton("Reset");
        JButton playButton = new JButton("Play");
        JButton pauseButton = new JButton("Pause");
        buttonPanel.add(resetButton);
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);

        // Add a ActionListener to the buttons
        resetButton.addActionListener(resetButtonListener);
        playButton.addActionListener(playButtonListener);
        pauseButton.addActionListener(pauseButtonListener);

        // Add the button panel to the frame
        frameWindow.getContentPane().setLayout(new BorderLayout());
        frameWindow.getContentPane().add(queryVideoPanel, BorderLayout.WEST);
        frameWindow.getContentPane().add(databaseVideoPanel, BorderLayout.EAST);
        frameWindow.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        frameWindow.setVisible(true);
    }

    private static VideoCapture loadMp4Video(String path) throws IOException {
        VideoCapture videoCapture = new VideoCapture(path);
        if (!videoCapture.isOpened()) {
            throw new IOException("Error: Could not open video file.");
        }
        return videoCapture;
    }

    private static Clip loadWavAudio(String path) throws IOException {
        try {
            File audioFile = new File(path);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            Clip audioClip = AudioSystem.getClip();
            audioClip.open(audioInputStream);
            return audioClip;
        } catch (UnsupportedAudioFileException | LineUnavailableException e) {
            throw new IOException("Error: Could not open audio file.");
        }
    }

    private void init() throws IOException {
        uiSetup();

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);   // For OpenCV

        queryVideoReader = new RGBVideoReader();
        queryVideoReader.open(queryResult.getQueryVideo().getVideoPath());

        databaseVideoReader = new Mp4VideoReader();
        databaseVideoReader.open(queryResult.getDatabaseVideo().getMp4Path());

        databaseAudioClip = loadWavAudio(queryResult.getDatabaseVideo().getWavPath());

        double desiredStartTimeInSeconds = queryResult.getStartTimeInSeconds();
        databaseVideoMicrosecondPosition = (long) (desiredStartTimeInSeconds * 1_000_000);
        databaseVideoReader.seek(desiredStartTimeInSeconds * 1000);
        databaseAudioClip.setMicrosecondPosition(databaseVideoMicrosecondPosition);

        loadNextFrame();
        frameCounter = queryResult.getStartTimeInFrames();

        initialezed = true;
    }

    private boolean timeToChangeVideoFrames(){
        databaseVideoMicrosecondPosition = databaseAudioClip.getMicrosecondPosition();
        int frameRate = Constants.FRAME_RATE;
        int desiredFrameCounter = (int) (databaseVideoMicrosecondPosition / 1_000_000.0 * frameRate);
        return frameCounter < desiredFrameCounter;
    }

    private void loadNextFrame(){
        BufferedImage queryVideoBuffer = new BufferedImage(RGBVideoReader.FRAME_WIDTH, RGBVideoReader.FRAME_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage databaseVideoBuffer = new BufferedImage(Mp4VideoReader.FRAME_WIDTH, Mp4VideoReader.FRAME_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);

        if (queryVideoReader.readFrame(queryVideoBuffer)) {
            ImageIcon icon = new ImageIcon(queryVideoBuffer);
            JLabel label = new JLabel(icon);
            queryVideoPanel.removeAll();
            queryVideoPanel.add(label);
        }

        if (databaseVideoReader.readFrame(databaseVideoBuffer)) {
            ImageIcon icon = new ImageIcon(databaseVideoBuffer);
            JLabel label = new JLabel(icon);
            databaseVideoPanel.removeAll();
            databaseVideoPanel.add(label);
        }

        frameWindow.revalidate();
        frameWindow.repaint();
    }

    public void play() throws IOException, InterruptedException {
        init();
        try {
            while (true) {
                if (!isPaused) {
                    if(timeToChangeVideoFrames()){
                        loadNextFrame();
                        frameCounter++;
                    }
                    Thread.sleep(16); // Sleep for a short time to reduce CPU usage
                } else {
                    // Paused, do nothing
                    Thread.sleep(100); // Sleep for a short time to reduce CPU usage
                }
            }
        } catch (InterruptedException e) {
            databaseAudioClip.close();
            queryVideoReader.close();
            databaseVideoReader.close();
            throw e;
        }
    }
}
