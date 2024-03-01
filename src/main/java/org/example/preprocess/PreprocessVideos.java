package org.example.preprocess;

import org.example.RGBFeatureExtraction;
import org.example.VisualFeatureExtraction;
import org.example.db.ES;
import org.example.entities.ShotBoundaryFeatures;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PreprocessVideos {

    public void initializeIndex() {
        ES es = new ES();
        es.emptyIndex();
    }

    public void preprocessVideos(String dbVideosPath) {
        RGBFeatureExtraction vfe = new RGBFeatureExtraction();
        ES es = new ES();

        // change this to the video db directory
        File directory = new File(dbVideosPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] fileArray = directory.listFiles();
            if (fileArray != null) {
                for (File file : fileArray) {
                    if (!file.isFile()) {
                        continue;
                    }
                    try {
                        List<ShotBoundaryFeatures> keyFrames = vfe.processRGBVideo(file.getAbsolutePath());
                        for (ShotBoundaryFeatures keyFrame: keyFrames) {
                            es.storeShot(keyFrame);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {
            System.out.println("The directory does not exist or is not a directory.");
        }
    }

    public static void main(String[] args) throws IOException {
        String dbVideoPath = args[0];
        PreprocessVideos p = new PreprocessVideos();
        p.initializeIndex();
        p.preprocessVideos(dbVideoPath);

        ES es = new ES();
        es.getAllShots();
    }
}
