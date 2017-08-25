
package com.musicrecognizer.utilities;

import com.musicrecognizer.audio.FFTWrapper;
import com.musicrecognizer.audio.Windows;

public class AudioFingerprintExtractor {
    @SuppressWarnings("unused")
    private static final int SHIFT_FRAME_STEP = 64;
    private static final int FFT_SIZE = 2048;
    private static final int BAND_COUNT = 33;
    private static final int FINGERPRINT_LENGTH = BAND_COUNT - 1;

    private FFTWrapper mTransformer;

    private double[] mWindow;

    private int[] mBandIndex = new int[] {
            110, 117, 124, 132, 139, 148,
            157, 166, 176, 187, 198, 210,
            223, 236, 250, 266, 282, 299,
            317, 335, 356, 377, 400, 424,
            450, 477, 505, 536, 568, 602,
            638, 677, 717, 760
    };

    private static AudioFingerprintExtractor sIntance;

    public static AudioFingerprintExtractor getIntance() {
        if (sIntance == null) {
            sIntance = new AudioFingerprintExtractor();
        }
        return sIntance;
    }

    public AudioFingerprintExtractor() {
        mTransformer = new FFTWrapper(FFT_SIZE);
        mWindow = Windows.getWindowsCoef(FFT_SIZE, Windows.TYPE.HAMMING);
    }

    private void applyWindow(short[] samples, double[] complexSamples) {
        for (int i = 0; i < samples.length; i++) {
            complexSamples[i << 1] = samples[i] * mWindow[i];
        }
    }

    private void fft(short[] samples, double[] fft) {
        applyWindow(samples, fft);
        mTransformer.complexForward(fft);
    }

    public double[] computeEnergyForAllBand(short[] samples) {
        double[] fftData = new double[samples.length * 2];
        fft(samples, fftData);
        double[] energy = new double[samples.length];
        getSpectralEnergyDensity(fftData, energy);
        return getEnergyForAllBand(energy);
    }

    private double[] getEnergyForAllBand(double[] energyDensity) {
        double[] energyByBand = new double[BAND_COUNT];
        for (int i = 0; i < energyByBand.length; i++) {
            energyByBand[i] = getSpectralEnergyByBand(energyDensity, mBandIndex[i], mBandIndex[i + 1]);
        }
        return energyByBand;
    }

    private void getSpectralEnergyDensity(double[] fftData, double[] energy) {
        for (int i = 0; i < energy.length; i++) {
            double re = fftData[i << 1];
            double im = fftData[(i << 1) + 1];
            energy[i] = re * re + im * im;
        }
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

    public String extract(double[] preFrameEnergies, double[] thisFrameEnergies) {
        StringBuilder fingerprint = new StringBuilder(FINGERPRINT_LENGTH);
        for (int m = 0; m < FINGERPRINT_LENGTH; m++) {
            double ed = (thisFrameEnergies[m] - thisFrameEnergies[m + 1]) - (preFrameEnergies[m] - preFrameEnergies[m + 1]);
            if (ed > 0) {
                fingerprint.append('1');
            } else {
                fingerprint.append('0');
            }
        }
        return convertBinToHex(fingerprint.toString());
    }

    private String convertBinToHex(String bin) {
        StringBuilder hex = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            hex.append(Integer.toHexString(Integer.parseInt(bin.substring(i << 2, (i << 2) + 4), 2)));
        }
        return hex.toString();
    }
}
