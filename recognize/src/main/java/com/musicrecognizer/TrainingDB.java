
package com.musicrecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import com.musicrecognizer.data.MediaFile;
import com.musicrecognizer.utilities.AudioFingerprintExtractor;
import com.musicrecognizer.utilities.Constants;
import com.musicrecognizer.utilities.Utility;

public class TrainingDB {

    private static TrainingDB sIntance;

    public static TrainingDB getInstance() {
        if (sIntance == null) {
            sIntance = new TrainingDB();
        }
        return sIntance;
    }

    public TrainingDB() {
        new File(Constants.TRAINED_MUSIC_FOLDER).mkdir();
        new File(Constants.TEMP_FOLDER).mkdir();
        new File(Constants.AUDIO_FINGERPRINT_FOLDER).mkdir();
    }

    public static void main(String[] args) {
        try {
            getInstance().train(new File(Constants.ROOT_FOLDER));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void train(File dir) throws IOException {
        File[] fileList = dir.listFiles();
        for (File file : fileList) {
            if (file.isFile()) {
                processFile(file);
                File trainedFile = new File(Constants.TRAINED_MUSIC_FOLDER + file.getName());
                if (trainedFile.exists()) {
                    trainedFile.delete();
                }
                file.renameTo(trainedFile);
            }
        }
    }

    public void processFile(File audioFile) throws IOException {
        MediaFile mediaFile;
        try {
            mediaFile = Utility.parserMetadata(audioFile);
            System.out.println(mediaFile.toString());
        } catch (IOException | SAXException | TikaException e) {
            e.printStackTrace();
            return;
        }

        File tmpFile = File.createTempFile(audioFile.getName(), ".tmp", new File(Constants.TEMP_FOLDER));
        tmpFile.deleteOnExit();

        File fingerprintFile = new File(Constants.AUDIO_FINGERPRINT_FOLDER + "_" + mediaFile.id + ".auf");
        PrintStream outStream = new PrintStream(new FileOutputStream(fingerprintFile));

        Utility.decodeAndResample(audioFile, tmpFile, Constants.CHANNEL_COUNT, Constants.SAMPLE_RATE);

        RandomAccessFile randomAccessFile = new RandomAccessFile(tmpFile, "r");

        int offset = 0;
        long endIndex = randomAccessFile.length() - 4096;

        double[] currentEnergy = null;
        double[] preEnergy = null;

        while (randomAccessFile.getFilePointer() < endIndex) {
            byte[] raw = new byte[4096]; // 2048 samples of short
            short[] samples = new short[2048];
            randomAccessFile.readFully(raw);
            ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
            currentEnergy = AudioFingerprintExtractor.getIntance().computeEnergyForAllBand(samples);
            if (preEnergy != null) {
                String fingerprint = AudioFingerprintExtractor.getIntance().extract(preEnergy, currentEnergy);
                outStream.println(fingerprint);
            }
            preEnergy = currentEnergy;
            offset += 128;
            randomAccessFile.seek(offset);
        }
        randomAccessFile.close();
        outStream.close();
    }
}
