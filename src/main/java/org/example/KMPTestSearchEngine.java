package org.example;

import org.example.entities.DatabaseVideo;
import org.example.entities.QueryResult;
import org.example.entities.QueryVideo;
import org.example.services.VideoSearchEngine;

import java.util.List;

public class KMPTestSearchEngine extends VideoSearchEngine {
    @Override
    public QueryResult query(QueryVideo queryVideo) {
        DatabaseVideo assumeWeFoundTheVideo = new DatabaseVideo("dataset/videos/video5.mp4", "dataset/audios/video5.wav");
        List<Integer> startFrames = KMPMatcher.match(queryVideo, assumeWeFoundTheVideo, true);

        assert startFrames.size() == 1; // If this fails, it means that the KMPMatcher is not working properly

        QueryResult result = new QueryResult(
                queryVideo,
                assumeWeFoundTheVideo,
                new QueryResult.TimeResult(startFrames.get(0))
        );
        return result;
    }
}
