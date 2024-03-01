package org.example;

import org.example.entities.ShotBoundaryFeatures;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoWriter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RGBFeatureExtraction {

    public List<ShotBoundaryFeatures> processRGBVideo(String rgbFilePath) {
        // Load OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Define video properties
        int width = 352;
        int height = 288;
        int fps = 30;

        // Convert the RGB video path to a Path object
        Path path = Paths.get(rgbFilePath);

        // Get the file name (including extension) from the path
        String videoName = path.getFileName().toString();

        List<Mat> boundaryHistograms = new ArrayList<>();
        List<ShotBoundaryFeatures> boundaryFeaturesList = new ArrayList<>();

        int boundaryIndex = 0;
        int frameNumber = 0;

        // Read the .rgb file and process each frame
        try (FileInputStream fis = new FileInputStream(rgbFilePath)) {
            byte[] frameData = new byte[width * height * 3];  // Each frame contains width x height x 3 bytes
            int bytesRead;

            while ((bytesRead = fis.read(frameData)) != -1) {
                // Create a Mat object from the raw frame data
                Mat frame = new Mat(height, width, CvType.CV_8UC3);
                frame.put(0, 0, frameData);

                if (boundaryHistograms.isEmpty()) {
                    boundaryFeaturesList.add(extractFeatures(frame, frameNumber, videoName));

                    boundaryHistograms.add(getColorHistogram(frame));
                } else {
                    // Color difference between frames
                    double colorDifference = compareColorHistograms(boundaryHistograms.get(boundaryIndex), frame);

                    // Threshold for color difference
                    double threshold = 0.3;

                    if (colorDifference > threshold) {
                        boundaryFeaturesList.add(extractFeatures(frame, frameNumber, videoName));

                        boundaryHistograms.add(getColorHistogram(frame));

                        boundaryIndex++;
                    }
                }
                frameNumber++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return boundaryFeaturesList;
    }

    // Custom method to extract features from a frame
    private ShotBoundaryFeatures extractFeatures(Mat frame, int frameNumber, String videoName) {
        // Calculate RGB averages
        Scalar scalar = Core.mean(frame);

        // Normalize values
        float R = (float) scalar.val[0] / 255.0f;
        R = (float)(Math.round(R * 1000.0) / 1000.0);

        float G = (float) scalar.val[1] / 255.0f;
        G = (float)(Math.round(G * 1000.0) / 1000.0);

        float B = (float) scalar.val[2] / 255.0f;
        B = (float)(Math.round(B * 1000.0) / 1000.0);

        // Calculate entropy
        float entropy = calculateEntropy(frame);

        // Calculate timestamp
        long timestamp = frameNumber / 30;

        // Print or store the extracted features as needed
        System.out.println("Video Name: " + videoName);
        System.out.println("Frame Number: " + frameNumber);
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Average RGB: R=" + R + " G=" + G + " B=" + B);
        System.out.println("Entropy: " + entropy);
        System.out.println();

        return new ShotBoundaryFeatures(videoName, frameNumber, R, G, B, entropy, timestamp);
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

    // Calculate entropy of frame
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

