
package com.trainingmusic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.trainingmusic.data.MediaFile;
import com.trainingmusic.utilities.Constants;
import com.trainingmusic.utilities.FingerPrintExtractor;
import com.trainingmusic.utilities.Utility;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void train(File dir) throws IOException {
        File[] fileList = dir.listFiles();
        for (File file : fileList) {
            if (file.isFile()) {
                // sendPost2("&fingerprint=939956ad");
                // sendPost2("&fingerprint=931d56ad");
                processFile(file);
                // File trainedFile = new File(Constants.TRAINED_MUSIC_FOLDER + file.getName());
                // if (trainedFile.exists()) {
                // trainedFile.delete();
                // }
                // file.renameTo(trainedFile);
            }
        }
    }

    boolean test = true;

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

        File fingerprintFile = new File(Constants.AUDIO_FINGERPRINT_FOLDER + mediaFile.title + "_" + mediaFile.id + ".auf");
        PrintStream outStream = new PrintStream(new FileOutputStream(fingerprintFile));

        Utility.decodeAndResample(audioFile, tmpFile, Constants.CHANNEL_COUNT, Constants.SAMPLE_RATE);

        RandomAccessFile randomAccessFile = new RandomAccessFile(tmpFile, "r");

        int offset = 0;
        long endIndex = randomAccessFile.length() - 4096;

        double[] currentEnergy = null;
        double[] preEnergy = null;
        ArrayList<String> fingerprints = new ArrayList<>();
        sendPost2("reset=true");
        while (randomAccessFile.getFilePointer() < endIndex) {
            byte[] raw = new byte[4096]; // 2048 samples of short
            short[] samples = new short[2048];
            randomAccessFile.readFully(raw);
            ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
            currentEnergy = FingerPrintExtractor.getIntance().computeEnergyForAllBand(samples);
            sendPost3(samples);
            if (preEnergy != null) {
                String fingerprint = FingerPrintExtractor.getIntance().extractHexFingerprint(preEnergy, currentEnergy);
                System.out.println("fingerprint local: " + fingerprint);
                fingerprints.add(fingerprint);
                if (fingerprints.size() == 256)
                    break;
                outStream.println(fingerprint);
            }
            preEnergy = currentEnergy;
            offset += 128;
            randomAccessFile.seek(offset);
        }
        randomAccessFile.close();
        outStream.close();

        // StringBuilder builder = new StringBuilder("");
        // for (int i = 0, size = fingerprints.size(); i < size && i < 64; i++) {
        // builder.append(fingerprints.get(i));
        // }
        // mediaFile.id = builder.toString();
        // String urlParameters = "newMediaFile=true&" + mediaFile.createUrlParameter();
        // sendPost(urlParameters);
        //
        // for (int i = 0, size = fingerprints.size(); i < size; i++) {
        // urlParameters = "mediaId=" + mediaFile.id + "&index=" + i + "&fingerprint=" + fingerprints.get(i);
        // sendPost(urlParameters);
        // }
    }

    private final String USER_AGENT = "Mozilla/5.0";

    // HTTP POST request
    private void sendPost(String urlParameters) throws IOException {
        String url = "https://music-recognizer.appspot.com/train";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        // add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
        writer.write(urlParameters);
        wr.flush();
        writer.close();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("Sending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // print result
        System.out.println(response.toString());
    }

    private void sendPost2(String urlParameters) throws IOException {
        String url = "https://music-recognizer.appspot.com/recognize";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        // add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
        writer.write(urlParameters);
        wr.flush();
        writer.close();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // print result
        System.out.println(response.toString());
    }

    private boolean sendPost3(short[] samples) throws IOException {
        String url = "https://music-recognizer.appspot.com/recognize";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        // add reuqest header
        con.setRequestMethod("POST");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        StringBuilder builder = new StringBuilder("samples=" + samples[0]);
        for (int i = 1; i < 2048; i++) {
            builder.append("," + samples[i]);
        }
        // wr.writeShort(samples[i]);
        String urlParameters = builder.toString();
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\n\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // print result
        System.out.println(response.toString());
        return true;
    }
}
