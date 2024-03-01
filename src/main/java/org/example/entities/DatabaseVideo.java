package org.example.entities;

public class DatabaseVideo {
    private String videoName;   // The simple name of the video (assume video and audio share the same name, e.g. "video5")
    private String mp4Path;     // The path to the video file, This is the video playing on the right side of our player (.mp4)
    private String wavPath;     // The path to the audio file, this is used as the audio played in our player (.wav)


    public DatabaseVideo(String mp4Path, String wavPath) {
        this("", mp4Path, wavPath);
    }

    public DatabaseVideo(String videoName, String mp4Path, String wavPath) {
        this.videoName = videoName;
        this.mp4Path = mp4Path;
        this.wavPath = wavPath;
    }

    public String getVideoName() {
        if(videoName.equals("")) {
            return mp4Path.substring(mp4Path.lastIndexOf('/') + 1, mp4Path.lastIndexOf('.'));
        }
        return videoName;
    }

    public String getMp4Path() {
        return mp4Path;
    }

    public String getWavPath() {
        return wavPath;
    }
}
