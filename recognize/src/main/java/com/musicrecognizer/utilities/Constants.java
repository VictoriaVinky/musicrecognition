
package com.musicrecognizer.utilities;

public interface Constants {
    public static final String BUCKET_NAME = "music-recognizer.appspot.com";
    public static final String ROOT_FOLDER = "D:/Work/GoogleAppEnginee/MusicFile/";
    public static final String TRAINED_MUSIC_FOLDER = ROOT_FOLDER + "Trained/";
    public static final String TEMP_FOLDER = ROOT_FOLDER + "tmp/";
    public static final String AUDIO_FINGERPRINT_FOLDER = ROOT_FOLDER + "auf/";

    public static final String KIND_AUDIO_FINGERPRINT = "AudioFingerprint";
    public static final String KIND_LOOK_UP_TABLE = "LookUpTable";
    public static final String KIND_METADATA = "Metadata";

    /**
     * pcm signed 16 bit little endian
     */
    public static final String CODEC_PCM_S16LE = "pcm_s16le";

    /**
     * pcm unsigned 16 bit little endian
     */
    public static final String FORMAT_U16LE = "u16le";

    public static final int CHANNEL_COUNT = 1;

    public static final int SAMPLE_RATE = 5512;

    public static final int RESPONSE_STATUS_CODE_CONTINUE = 100;
    public static final int RESPONSE_STATUS_CODE_SUCCESS = 200;
    public static final int RESPONSE_STATUS_CODE_NOT_FOUND = 404;
}
