package org.example.services;

import org.example.entities.DatabaseVideo;
import org.example.entities.QueryResult;
import org.example.entities.QueryVideo;

/* This is a dummy search engine that always returns the same result */
public class DummySearchEngine extends VideoSearchEngine {
    @Override
    public QueryResult query(QueryVideo queryVideo) {
        QueryResult result = new QueryResult(queryVideo,
                new DatabaseVideo("dataset/videos/video5.mp4", "dataset/audios/video5.wav"),
                7 * 60 + 57 // According to https://piazza.com/class/llv3og8eciz3zc/post/366 the result should start at 7:57
        );
        return result;
    }
}
