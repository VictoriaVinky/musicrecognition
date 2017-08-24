
package com.musicrecognizer.audio;

import org.jtransforms.fft.DoubleFFT_1D;

public class FFTWrapper {
    private DoubleFFT_1D mDoubleFFT;

    public FFTWrapper(int fftSize) {
        mDoubleFFT = new DoubleFFT_1D(fftSize);
    }

    public void complexForward(double[] data) {
        mDoubleFFT.complexForward(data);
    }
}
