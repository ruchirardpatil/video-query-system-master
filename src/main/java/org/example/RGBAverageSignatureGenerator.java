package org.example;

import org.example.util.Constants;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RGBAverageSignatureGenerator implements FrameSignatureGenerator<Integer> {
    @Override
    public Integer getFrameSignature(BufferedImage frame) {
        int pixelCount = frame.getWidth() * frame.getHeight();
        double[] runningSum = new double[3];
        for (int y = 0; y < frame.getHeight(); y++) {
            for (int x = 0; x < frame.getWidth(); x++) {
                int rgb = frame.getRGB(x, y);
                runningSum[0] += (rgb >> 16) & 0xFF;
                runningSum[1] += (rgb >> 8) & 0xFF;
                runningSum[2] += rgb & 0xFF;
            }
        }
        int r = (int) (runningSum[0] / pixelCount);
        int g = (int) (runningSum[1] / pixelCount);
        int b = (int) (runningSum[2] / pixelCount);
        int res = (r << 16) | (g << 8) | b;
        return res;
    }

    /**
     * Get the kmp data for all frames in a video
     *
     * @param rgbVideoFilePath the path of the rgb video file
     * @return the kmp data for a video (an array of integers)
     */
    public int[] getVideoSignature(String rgbVideoFilePath) {
        List<Integer> rgbAverage = new ArrayList<>();
        try {
            RGBVideoReader rgbVideoReader = new RGBVideoReader();
            rgbVideoReader.open(rgbVideoFilePath);
            BufferedImage frame = new BufferedImage(Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
            while (rgbVideoReader.readFrame(frame)) {
                rgbAverage.add(getFrameSignature(frame));
            }
            rgbVideoReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int[] rgbAverageArray = new int[rgbAverage.size()];
        for (int i = 0; i < rgbAverage.size(); i++) {
            rgbAverageArray[i] = rgbAverage.get(i);
        }
        return rgbAverageArray;
    }

    /**
     * Get the kmp data for the first frameLimit frames in a video
     *
     * @param frameLimit       the number of frames to get the kmp data
     * @param rgbVideoFilePath the path of the rgb video file
     * @return the kmp data for a video (an array of integers)
     */
    public int[] getVideoSignature(String rgbVideoFilePath, int frameLimit) {
        int[] rgbAverageArray = new int[frameLimit];
        int i = 0;
        try {
            RGBVideoReader rgbVideoReader = new RGBVideoReader();
            rgbVideoReader.open(rgbVideoFilePath);
            BufferedImage frame = new BufferedImage(Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
            for (; i < frameLimit; i++) {
                if (rgbVideoReader.readFrame(frame)) {
                    rgbAverageArray[i] = getFrameSignature(frame);
                } else {
                    int[] rgbAverageArray2 = new int[i];
                    System.arraycopy(rgbAverageArray, 0, rgbAverageArray2, 0, i);
                    return rgbAverageArray2;
                }
            }
            rgbVideoReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rgbAverageArray;
    }

    /**
     * Deserialize(load) the kmp data from the file
     *
     * @param kmpDataFilePath .ser file path
     * @return the kmp data for a video (an array of integers)
     */
    public static int[] deserialize(String kmpDataFilePath) {
        int[] arr = null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(kmpDataFilePath))) {
            arr = (int[]) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return arr;
    }

    /**
     * Serialize(save) the kmp data to the file
     *
     * @param path            .ser file path
     * @param rgbAverageArray the kmp data for a video (an array of integers)
     * @return
     */
    public static boolean serialize(String path, int[] rgbAverageArray) {
        // If path not exist, create the folder
        File file = new File(path);
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            return false;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(rgbAverageArray);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void preprocessing(String rgbVideoFolder, String kmpDataFolder) {
        // For all .rgb files in the folder, get the kmp data and save it to .ser files
        File folder = new File(rgbVideoFolder);
        File[] listOfFiles = folder.listFiles();
        RGBAverageSignatureGenerator rgbAverageSignatureGenerator = new RGBAverageSignatureGenerator();
        System.out.println("Start preprocessing, video count: " + listOfFiles.length);
        int i = 1;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                System.out.println("Preprocessing: " + file.getName() + " this is the " + i + "th video");
                String rgbVideoFilePath = file.getAbsolutePath();
                String kmpDataFilePath = kmpDataFolder + "/" + file.getName().replace(".rgb", ".ser");
                int[] rgbAverageArray = rgbAverageSignatureGenerator.getVideoSignature(rgbVideoFilePath);
                RGBAverageSignatureGenerator.serialize(kmpDataFilePath, rgbAverageArray);
                System.out.println("Preprocessed: " + file.getName() + " file saved to " + kmpDataFilePath);
                i++;
            }
        }
    }

    public static void main(String[] args) {
        preprocessing(args[0], args[1]);
//        for (int i = 1; i <= 20; i++) {
//            int[] test = deserialize("dataset/kmp/video" + i + ".ser");
//            System.out.println(test.length);
//        }
    }

}
