
package com.trainingmusic.utilities;

import com.trainingmusic.audio.FFTWrapper;
import com.trainingmusic.audio.Windows;

public class FingerPrintExtractor {
    private static final int FFT_SIZE = 2048;

    @SuppressWarnings("unused")
    private static final int SHIFT_FRAME_STEP = 64;

    private static final int BAND_COUNT = 33;

    private static final int FINGERPRINT_LENGTH = BAND_COUNT - 1;

    private FFTWrapper mTransformer;

    private double[] window;

    private int[] mBandIndex = new int[]{
        110, 117, 124, 132, 139, 148, 157, 166, 176, 187, 198, 210, 223, 236, 250,
        266, 282, 299, 317, 335, 356, 377, 400, 424, 450, 477, 505, 536, 568, 602, 638, 677, 717, 760
    };

    private static FingerPrintExtractor sIntance;

    public static FingerPrintExtractor getIntance() {
        if (sIntance == null) {
            sIntance = new FingerPrintExtractor();
        }
        return sIntance;
    }

    public FingerPrintExtractor() {
        mTransformer = new FFTWrapper(FFT_SIZE);
        window = Windows.getWindowsCoef(FFT_SIZE, Windows.TYPE.HAMMING);
    }

    public int extractIntFingerprint(double[] preFrameEnergies, double[] thisFrameEnergies) {
        return (int) Long.parseLong(extractBinFingerprint(preFrameEnergies, thisFrameEnergies), 2);
    }

    public String extractHexFingerprint(double[] preFrameEnergies, double[] thisFrameEnergies) {
        return convertBinToHex(extractBinFingerprint(preFrameEnergies, thisFrameEnergies));
    }

    public String extractBinFingerprint(double[] preFrameEnergies, double[] thisFrameEnergies) {
        StringBuilder fingerprint = new StringBuilder(FINGERPRINT_LENGTH);
        for (int m = 0; m < FINGERPRINT_LENGTH; m++) {
            double ed = (thisFrameEnergies[m] - thisFrameEnergies[m + 1])
                    - (preFrameEnergies[m] - preFrameEnergies[m + 1]);
            if (ed > 0) {
                fingerprint.append('1');
            } else {
                fingerprint.append('0');
            }
        }
        return fingerprint.toString();
    }

    public double[] computeEnergyForAllBand(short[] samples) {
        double[] data = new double[samples.length * 2];
        applyWindow(samples, data);
        mTransformer.complexForward(data);
        return getEnergyForAllBand(getSpectralEnergyDensity(data));
    }

    private void applyWindow(short[] samples, double[] complexSamples) {
        for (int i = 0; i < samples.length; i++) {
            complexSamples[i << 1] = samples[i] * window[i];
        }
    }

    private double[] getEnergyForAllBand(double[] energyDensity) {
        double[] energyByBand = new double[BAND_COUNT];
        for (int i = 0; i < energyByBand.length; i++) {
            energyByBand[i] = getSpectralEnergyByBand(energyDensity, mBandIndex[i], mBandIndex[i + 1]);
        }
        return energyByBand;
    }

    private double[] getSpectralEnergyDensity(double[] fftData) {
        double[] energy = new double[fftData.length / 2];
        for (int i = 0; i < energy.length; i++) {
            double re = fftData[i << 1];
            double im = fftData[(i << 1) + 1];
            energy[i] = re * re + im * im;
        }
        return energy;
    }

    /**
     * Compute energy for band from startIdx to endIdx, include start but
     * exclude end
     */
    private double getSpectralEnergyByBand(double[] energyDensity, int startIdx, int endIdx) {
        double e = 0;
        for (int i = startIdx; i < endIdx; i++) {
            e += energyDensity[i];
        }
        return e;
    }

    private String convertBinToHex(String bin) {
        StringBuilder hex = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            hex.append(Integer.toHexString(Integer.parseInt(bin.substring(i << 2, (i << 2) + 4), 2)));
        }
        return hex.toString();
    }
}
