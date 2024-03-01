package org.example.services;

import org.example.KMPMatcher;
import org.example.RGBAverageSignatureGenerator;
import org.example.RGBFeatureExtraction;
import org.example.db.ES;
import org.example.entities.DatabaseVideo;
import org.example.entities.QueryResult;
import org.example.entities.QueryVideo;
import org.example.entities.ShotBoundaryFeatures;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.example.util.Constants.AUDIO_FOLDER_PATH;
import static org.example.util.Constants.VIDEO_FOLDER_PATH;

public class VideoSearchEngine {
    public static class ScoreVideo {
        public int score;
        public int number;

        public ScoreVideo(int score, int number) {
            this.score = score;
            this.number = number;
        }

        public int getScore() {
            return score;
        }

        public int getNumber() {
            return number;
        }
    }

    private List<ScoreVideo> voteForBestMatchVideo(List<ShotBoundaryFeatures> queryVideoFrameFeatures) throws IOException {
        // Give matches a score
        ES es = new ES();
        List<ScoreVideo> scoreVideos = new ArrayList<>();
        for(int i=1; i<=20; i++){
            scoreVideos.add(new ScoreVideo(0, i));
        }

        for (ShotBoundaryFeatures frameFeature : queryVideoFrameFeatures) {
            List<ShotBoundaryFeatures> candidateFrameFeatures = es.searchFrames(frameFeature);
            for (int j = 0; j < candidateFrameFeatures.size(); j++) {
                scoreVideos.get(getVideoNumber(candidateFrameFeatures.get(j).getVidName())).score += (int) Math.pow((10 - j), 2);
            }
        }

        // return the videos descending with score
        scoreVideos.sort((o1, o2) -> o2.score - o1.score);
        return scoreVideos;
    }

    private DatabaseVideo getDatabaseVideoByNumber(int videoNumber) {
        String databaseVideoFolderPath = VIDEO_FOLDER_PATH;
        String databaseAudioFolderPath = AUDIO_FOLDER_PATH;

        String videoPath = databaseVideoFolderPath + "/video" + videoNumber + ".mp4";
        String audioPath = databaseAudioFolderPath + "/video" + videoNumber + ".wav";

        DatabaseVideo dbVideo = new DatabaseVideo(videoPath, audioPath);

        return dbVideo;
    }

    public QueryResult query(QueryVideo queryVideo) throws IOException {
        RGBAverageSignatureGenerator generator = new RGBAverageSignatureGenerator();
        int[] pat = generator.getVideoSignature(queryVideo.getVideoPath(), 300);
        RGBFeatureExtraction visualFeatureExtraction = new RGBFeatureExtraction();
        List<ShotBoundaryFeatures> boundaryFeaturesList = visualFeatureExtraction.processRGBVideo(queryVideo.getVideoPath());
        List<ScoreVideo> scoreVideos = voteForBestMatchVideo(boundaryFeaturesList);
        for (ScoreVideo scoreVideo : scoreVideos) {
            DatabaseVideo dbVideo = getDatabaseVideoByNumber(scoreVideo.number);

            String serFilePath = "dataset/kmp/video" + scoreVideo.number + ".ser";
            int[] txt = RGBAverageSignatureGenerator.deserialize(serFilePath);
            List<Integer> matches = KMPMatcher.match(pat, txt);

            if(matches.size()==1){
                QueryResult queryResult = new QueryResult(queryVideo, dbVideo, new QueryResult.TimeResult(matches.get(0)));

                //        Print out video match
                System.out.println();
                System.out.println("Best Match: video" + dbVideo.getVideoName());

                //        Print Timestamp
                System.out.println("Frame idx: " + queryResult.getStartTimeInFrames());
                System.out.println("Timestamp: " + queryResult.getStartTimeInSeconds());

                return queryResult;
            }
        }
        return null;
    }

    public int getVideoNumber(String videoName) {
        StringBuilder number = new StringBuilder();
        boolean numberFound = false;

        for (char ch : videoName.toCharArray()) {
            if (Character.isDigit(ch)) {
                number.append(ch);
                numberFound = true;
            } else if (numberFound) {
                // Break once the first sequence of digits is completed
                break;
            }
        }

        if (number.length() > 0) {
            return Integer.parseInt(number.toString()) - 1;
        } else {
            throw new IllegalArgumentException("No number found in the string");
        }
    }
}
