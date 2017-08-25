
package com.musicrecognizer;

import java.io.File;

import com.musicrecognizer.utilities.AudioFingerprintExtractor;
import com.musicrecognizer.utilities.Constants;

public class MusicRecognizer {
    private static MusicRecognizer sIntance;
    private double[] mCurrentEnergy, mPreEnergy;
    private AudioFingerprintExtractor mAudioFingerPrintExtractor;

    public static MusicRecognizer getInstance() {
        if (sIntance == null) {
            sIntance = new MusicRecognizer();
        }
        return sIntance;
    }

    public MusicRecognizer() {
        new File(Constants.TEMP_FOLDER).mkdir();
        new File(Constants.AUDIO_FINGERPRINT_FOLDER).mkdir();
        mAudioFingerPrintExtractor = new AudioFingerprintExtractor();
    }

    /**
     * @param samples short[2048]
     * @throws Exception
     */
    public String recognize(short[] samples) throws Exception {
        if (samples.length < 2048) {
            throw new Exception("Must 2048 elements!!!");
        }
        mCurrentEnergy = mAudioFingerPrintExtractor.computeEnergyForAllBand(samples);
        if (mPreEnergy != null) {
            return mAudioFingerPrintExtractor.extract(mPreEnergy, mCurrentEnergy);
        }
        mPreEnergy = mCurrentEnergy;
        return null;
    }
}
