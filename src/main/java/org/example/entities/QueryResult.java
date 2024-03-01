package org.example.entities;

import org.example.util.Constants;

public class QueryResult {
    private QueryVideo queryVideo;
    private DatabaseVideo databaseVideo;
    private int startTimeInFrames;  // I changed startTimeInSeconds to startTimeInFrames because the grading rubric requires it

    public QueryResult(QueryVideo queryVideo, DatabaseVideo databaseVideo, double startTimeInSeconds) {
        this.queryVideo = queryVideo;
        this.databaseVideo = databaseVideo;
        this.startTimeInFrames = (int) startTimeInSeconds * Constants.FRAME_RATE;
    }

    // The TimeResult class is defined to distinguish between the two constructors
    // Because integer and double may be converted to each other implicitly
    public QueryResult(QueryVideo queryVideo, DatabaseVideo databaseVideo, TimeResult timeResult) {
        this.queryVideo = queryVideo;
        this.databaseVideo = databaseVideo;
        this.startTimeInFrames = timeResult.startFrame;

    }

    public QueryVideo getQueryVideo() {
        return queryVideo;
    }

    public DatabaseVideo getDatabaseVideo() {
        return databaseVideo;
    }

    public double getStartTimeInSeconds() {
        return (double) startTimeInFrames / Constants.FRAME_RATE;
    }

    public int getStartTimeInFrames() {
        return startTimeInFrames;
    }

    // The TimeResult class is defined to distinguish between the two constructors
    // Because integer and double may be converted to each other implicitly
    public static class TimeResult{
        public int startFrame;
        public TimeResult(int startFrame){
            this.startFrame = startFrame;
        }
    }
}
