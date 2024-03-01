package org.example;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

public class Mp4VideoReader implements VideoReader{
    public static final int FRAME_WIDTH = 352;
    public static final int FRAME_HEIGHT = 288;
    private VideoCapture videoCapture;
    private Mat frame = new Mat();

    @Override
    public void open(String videoFilePath) throws IOException {
        videoCapture = new VideoCapture(videoFilePath);
    }

    @Override
    public boolean readFrame(BufferedImage buffer) {
        if(videoCapture.read(frame)) {
            byte[] frameBytes = new byte[FRAME_WIDTH * FRAME_HEIGHT * 3];
            frame.get(0, 0, frameBytes);
            final byte[] bufferData = ((DataBufferByte) buffer.getRaster().getDataBuffer()).getData();
            System.arraycopy(frameBytes, 0, bufferData, 0, frameBytes.length);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void seek(double timeInMilliSeconds) {
        videoCapture.set(Videoio.CAP_PROP_POS_MSEC, timeInMilliSeconds);
    }

    @Override
    public void close() {
        videoCapture.release();
    }

}
