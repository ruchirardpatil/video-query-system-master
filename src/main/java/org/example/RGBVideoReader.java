package org.example;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RGBVideoReader implements VideoReader{
    public static final int FRAME_WIDTH = 352;
    public static final int FRAME_HEIGHT = 288;
    private RandomAccessFile raf;

    @Override
    public void open(String videoFilePath) throws IOException {
        raf = new RandomAccessFile(videoFilePath, "r");
    }

    @Override
    public boolean readFrame(BufferedImage buffer) {
        // Read the next frame
        byte[] frameBytes = new byte[FRAME_WIDTH * FRAME_HEIGHT * 3];
        try {
            if(raf.read(frameBytes)>0){
                buffer.getRaster().setDataElements(0, 0, FRAME_WIDTH, FRAME_HEIGHT, frameBytes);
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void seek(double timeInMilliSeconds) {
        if(timeInMilliSeconds != 0.0) throw new UnsupportedOperationException("Seeking is not supported in RGBVideoReader");
        try {
            raf.seek(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            raf.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
