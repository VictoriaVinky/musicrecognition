
package com.trainingmusic.audio;

public class Windows {
    public enum TYPE {
        HAMMING
    }

    public static double[] getWindowsCoef(int size, TYPE type) {
        double[] w = new double[size];
        if (type == TYPE.HAMMING) {
            int m = size / 2;
            double r = Math.PI / m;
            for (int n = -m; n < m; n++) {
                w[m + n] = 0.54 + 0.46 * Math.cos(n * r);
            }
        }
        return w;
    }
}
