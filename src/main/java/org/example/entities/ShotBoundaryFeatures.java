package org.example.entities;

import com.alibaba.fastjson.annotation.JSONField;
import org.opencv.core.*;

import java.util.Date;

public class ShotBoundaryFeatures {

    @JSONField(name = "vidName")
    private String vidName;

    @JSONField(name = "frameNumber")
    private int frameNumber;

    @JSONField(name = "R")
    private float R;

    @JSONField(name = "G")
    private float G;

    @JSONField(name = "B")
    private float B;

    @JSONField(name = "entropy")
    private float entropy;

    @JSONField(name = "timestamp")
    private long timestamp;

    public ShotBoundaryFeatures(String vidName, int frameNumber, float r, float g, float b, float entropy, long timestamp) {
        this.vidName = vidName;
        this.frameNumber = frameNumber;
        R = r;
        G = g;
        B = b;
        this.entropy = entropy;
        this.timestamp = timestamp;
    }

    public String getVidName() {
        return vidName;
    }

    public void setVidName(String vidName) {
        this.vidName = vidName;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    public void setR(float r) {
        R = r;
    }

    public void setG(float g) {
        G = g;
    }

    public void setB(float b) {
        B = b;
    }

    public void setEntropy(float entropy) {
        this.entropy = entropy;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public float getR() { return R; }

    public float getG() { return G; }

    public float getB() { return B; }

    @Override
    public String toString() {
        return "ShotBoundaryFeatures{" +
                "vidName='" + vidName + '\'' +
                ", frameNumber=" + frameNumber +
                ", R=" + R +
                ", G=" + G +
                ", B=" + B +
                ", entropy=" + entropy +
                ", timestamp=" + timestamp +
                '}';
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getEntropy() {
        return entropy;
    }
}
