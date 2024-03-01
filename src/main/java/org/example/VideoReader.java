package org.example;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface VideoReader {
    void open(String videoFilePath) throws IOException;
    boolean readFrame(BufferedImage buffer);
    void seek(double timeInMilliSeconds);
    void close();
}
