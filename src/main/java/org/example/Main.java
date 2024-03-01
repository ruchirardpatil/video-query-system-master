package org.example;

import org.example.entities.QueryResult;
import org.example.entities.QueryVideo;
import org.example.entities.ShotBoundaryFeatures;
import org.example.services.DummySearchEngine;
import org.example.services.VideoSearchEngine;

import java.io.IOException;
import java.util.List;

public class Main {
    //    public static void main(String[] args) throws IOException {
//
//        String videoPath = args[0];
////        VisualFeatureExtraction visualFeatureExtraction = new VisualFeatureExtraction();
////        visualFeatureExtraction.processVideo(videoPath);
//
//        ES es = new ES();
//        es.store();
//        es.search();
//
//    }


    public static void main(String[] args) throws IOException, InterruptedException {
        String videoPath = args[0];
        String audioPath = args[1];
        QueryVideo queryVideo = new QueryVideo(videoPath, audioPath);

        VideoSearchEngine engine = new VideoSearchEngine();
        QueryResult queryResult = engine.query(queryVideo);

        ResultPlayer resultPlayer = new ResultPlayer(queryResult);
        resultPlayer.play();
    }


}