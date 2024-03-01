package org.example.entities;

public class QueryVideo {
    // OurProgram.exe example.rgb example.wav
    private String rgbPath;   // The path to the input query video (.rgb file)
    private String wavPath;   // The path to the input query audio (.wav file)

    public QueryVideo(String videoPath, String audioPath) {
        this.rgbPath = videoPath;
        this.wavPath = audioPath;
    }

    // Support previous defined names
    public String getVideoPath() {
        return rgbPath;
    }
    public String getAudioPath() {
        return wavPath;
    }

    public String getRgbPath() {
        return rgbPath;
    }

    public String getWavPath() {
        return wavPath;
    }

}
