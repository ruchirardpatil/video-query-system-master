package org.example;

import org.example.db.ES;
import org.example.entities.ShotBoundaryFeatures;
import org.opencv.core.*;
import org.opencv.imgproc.*;
import org.opencv.videoio.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.util.ArrayList;
import java.util.List;

public class VisualFeatureExtraction {

    public static String getFileName(String filePath) {
        Path path = Paths.get(filePath);
        return path.getFileName().toString();
    }

    public List<ShotBoundaryFeatures> processVideo(String videoPath) {
        Date startingTime = new Date();

        // Load OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Open video
        VideoCapture videoCapture = new VideoCapture(videoPath);
        String videoName = getFileName(videoPath);

        if (!videoCapture.isOpened()) {
            System.out.println("Error: Could not open video file.");
        }

        double frameRate = videoCapture.get(Videoio.CAP_PROP_FPS);

        Mat currentFrame = new Mat();
        Mat lastShotBoundary = new Mat();
        ES es = new ES();

        List<Integer> shotBoundaryFrames = new ArrayList<>();
        int currentFrameNumber = 0;

        List<Mat> boundaryHistograms = new ArrayList<>();
        List<Scalar> colorAverages = new ArrayList<>();
        List<Float> entropies = new ArrayList<>();
        List<ShotBoundaryFeatures> boundaryFeaturesList = new ArrayList<>();

        // Add first frame as first shot boundary
        shotBoundaryFrames.add(currentFrameNumber);

        int boundaryIndex = 0;

        // Read frame by frame
        while (videoCapture.read(currentFrame)) {
            if (!lastShotBoundary.empty()) {
                // Color difference between frames
                double colorDifference = compareColorHistograms(boundaryHistograms.get(boundaryIndex), currentFrame);

                // Threshold for color difference
                double threshold = 0.3;

                if (colorDifference > threshold) {
                    // If significant difference, current frame is shot boundary
                    shotBoundaryFrames.add(currentFrameNumber);

                    // Current frame becomes previous frame
                    currentFrame.copyTo(lastShotBoundary);

                    // Add boundary frame's histogram
                    boundaryHistograms.add(getColorHistogram(lastShotBoundary));

                    // Add average colors
                    colorAverages.add(Core.mean(lastShotBoundary));

                    // Add entropy of frame
                    entropies.add(calculateEntropy(lastShotBoundary));

                    boundaryIndex++;
                }
            }
            else {
                // Current frame becomes previous frame
                currentFrame.copyTo(lastShotBoundary);

                // Add boundary frame's histogram
                boundaryHistograms.add(getColorHistogram(lastShotBoundary));

                // Add average colors
                colorAverages.add(Core.mean(lastShotBoundary));

                // Add entropy of frame
                entropies.add(calculateEntropy(lastShotBoundary));
            }

            currentFrameNumber++;
        }

        videoCapture.release();

        // Total frames
        System.out.println("Total Frames: " + currentFrameNumber);
        System.out.println();

        // Print frames and colors
        for (int i = 0; i < shotBoundaryFrames.size(); i++) {
            Scalar scalar = colorAverages.get(i);

            // Normalize values
            float R = (float) scalar.val[2] / 255.0f;
            R = (float)(Math.round(R * 1000.0) / 1000.0);

            float G = (float) scalar.val[1] / 255.0f;
            G = (float)(Math.round(G * 1000.0) / 1000.0);

            float B = (float) scalar.val[0] / 255.0f;
            B = (float)(Math.round(B * 1000.0) / 1000.0);

            long timestamp = (long) ((long) shotBoundaryFrames.get(i) / frameRate);

            ShotBoundaryFeatures shot = new ShotBoundaryFeatures(videoName, shotBoundaryFrames.get(i), R, G, B, entropies.get(i), timestamp);
            // Add to list of boundary shots + features
            boundaryFeaturesList.add(shot);

            System.out.println("Video Name: " + videoName);
            System.out.println("Frame Number: " + shotBoundaryFrames.get(i));
            System.out.println("Timestamp: " + timestamp);
            System.out.println("Colors: R=" + R + " G=" + G + " B=" + B);
            System.out.println("Entropy: " + entropies.get(i));
            System.out.println();
        }

//        Display shot boundary frames
//        for (Integer frameNumber : shotBoundaryFrames) {
//            if (frameNumber >= 0 && frameNumber < currentFrameNumber) {
//                // Load and display the frame
//                videoCapture.open(videoPath);
//                videoCapture.set(Videoio.CAP_PROP_POS_FRAMES, frameNumber);
//                videoCapture.read(currentFrame);
//                HighGui.imshow("Shot Boundary Frame", currentFrame);
//                HighGui.waitKey(0);
//                HighGui.destroyAllWindows();
//            }
//        }

        return boundaryFeaturesList;
    }

    // Calculate the color histogram of a frame
    private static Mat getColorHistogram(Mat frame) {
        Mat hist = new Mat();

        Imgproc.calcHist(
                Arrays.asList(frame),
                new MatOfInt(0, 1, 2),
                new Mat(),
                hist,
                new MatOfInt(8, 8, 8),
                new MatOfFloat(0, 256, 0, 256, 0, 256)
        );

        return hist;
    }

    // Function to compare color histograms of two frames
    private static double compareColorHistograms(Mat hist1, Mat frame2) {
        Mat hist2 = new Mat();

        // Calculate color histogram for second frame
        hist2 = getColorHistogram(frame2);

        // Calculate Bhattacharyya distance between histograms
        return Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_BHATTACHARYYA);
    }

    private static float calculateEntropy(Mat frame) {
        // Convert to grayscale
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

        // Calculate histogram
        List<Mat> images = new ArrayList<>();
        images.add(grayFrame);
        Mat hist = new Mat();
        Imgproc.calcHist(images, new MatOfInt(0), new Mat(), hist, new MatOfInt(256), new MatOfFloat(0, 256));

        // Normalize the histogram
        Core.normalize(hist, hist, 1, 0, Core.NORM_L1);

        // Calculate entropy
        float entropy = 0;
        for (int i = 0; i < hist.rows(); i++) {
            double p = hist.get(i, 0)[0];
            if (p != 0) {
                entropy -= p * Math.log(p) / Math.log(2);
            }
        }

        entropy /= 8.0f;

        // Round to three decimal places
        entropy = (float) (Math.round(entropy * 1000) / 1000.0);

        return entropy;
    }
}
