package com.velocityllm.util;

public final class MathUtil {

    private MathUtil() {
    }

    public static double cosineSimilarity(float[] left, float[] right) {
        if (left.length == 0 || right.length == 0 || left.length != right.length) {
            return 0;
        }

        double dot = 0;
        double leftNorm = 0;
        double rightNorm = 0;
        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }

        if (leftNorm == 0 || rightNorm == 0) {
            return 0;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }
}
