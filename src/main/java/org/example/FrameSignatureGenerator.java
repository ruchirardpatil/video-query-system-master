package org.example;

import java.awt.image.BufferedImage;

public interface FrameSignatureGenerator<SigType> {
    public SigType getFrameSignature(BufferedImage frame);
}
