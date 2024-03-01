package org.example;

import org.example.entities.DatabaseVideo;
import org.example.entities.QueryVideo;
import org.example.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt Algorithm for Pattern Matching
 * <p>
 * Worst case complexity: O(n + m)
 * <p>
 * n: length of text
 * m: length of pattern
 * <p>
 * Space complexity: O(m)
 * <p>
 * https://www.geeksforgeeks.org/kmp-algorithm-for-pattern-searching/
 */
public class KMPMatcher {
    /**
     * This method is used after we found the best match video using other method
     * It will return a list of indices where the pattern is found in the video
     * @param queryVideo the query video
     * @param databaseVideo the database video
     * @return a list of indices where the pattern is found in the video
     */
    public static List<Integer> match(QueryVideo queryVideo, DatabaseVideo databaseVideo, boolean useQuickMatch) {
        String kmpDataFolderPath = Constants.KMP_DATA_FOLDER_PATH;
        String databaseVideoName = databaseVideo.getVideoName();
        String kmpFilePath = kmpDataFolderPath + "/" + databaseVideoName + ".ser";
        int[] txt = RGBAverageSignatureGenerator.deserialize(kmpFilePath);
        RGBAverageSignatureGenerator rgbAverageSignatureGenerator = new RGBAverageSignatureGenerator();
        int[] pat;
        if (useQuickMatch) {
            pat = rgbAverageSignatureGenerator.getVideoSignature(queryVideo.getVideoPath());
        } else {
            pat = rgbAverageSignatureGenerator.getVideoSignature(queryVideo.getVideoPath(), Constants.KMP_QUICK_MATCH_FRAME_LIMIT);
        }
        return match(pat, txt);
    }

    /**
     * Returns a list of indices where the pattern is found in the text
     */
    public static List<Integer> match(int[] pat, int[] txt) {
        List<Integer> res = new ArrayList<>();

        int M = pat.length;
        int N = txt.length;

        int[] lps = computeLpsArray(pat);

        int i = 0, j=0;
        while ((N - i) >= (M - j)) {
            if (pat[j] == txt[i]) {
                j++;
                i++;
            }
            if (j == M) {
                res.add(i - j);
                j = lps[j - 1];
            }

            // mismatch after j matches
            else if (i < N
                    && pat[j] != txt[i]) {
                // Do not match lps[0..lps[j-1]] characters,
                // they will match anyway
                if (j != 0)
                    j = lps[j - 1];
                else
                    i = i + 1;
            }
        }
        return res;
    }

    /**
     * Compute temporary array to maintain size of suffix which is same as prefix
     * Time/space complexity is O(size of pattern)
     */
    private static int[] computeLpsArray(int[] pat) {
        int[] lps = new int[pat.length];
        // length of the previous longest prefix suffix
        int len = 0, m = pat.length;
        int i = 1;
        lps[0] = 0; // lps[0] is always 0

        // the loop calculates lps[i] for i = 1 to M-1
        while (i < m) {
            if (pat[i] == pat[len]) {
                len++;
                lps[i] = len;
                i++;
            }
            else // (pat[i] != pat[len])
            {
                // This is tricky. Consider the example.
                // AAACAAAA and i = 7. The idea is similar
                // to search step.
                if (len != 0) {
                    len = lps[len - 1];

                    // Also, note that we do not increment
                    // i here
                }
                else // if (len == 0)
                {
                    lps[i] = len;
                    i++;
                }
            }
        }
        return lps;
    }

    public static void main(String[] args) {
        // Test
        int[] pat = {1, 2, 3};
        int[] txt = {1, 2, 3, 4, 5, 1, 2, 3, 6, 7, 8, 9, 1, 2, 3};
        List<Integer> res = match(pat, txt);
        System.out.println(res);
    }
}
