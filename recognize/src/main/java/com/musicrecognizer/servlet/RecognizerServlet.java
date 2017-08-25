package com.musicrecognizer.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
import com.musicrecognizer.data.MediaFile;
import com.musicrecognizer.data.MetadataResponse;
import com.musicrecognizer.utilities.AudioFingerprintExtractor;
import com.musicrecognizer.utilities.Constants;

/**
 * @author Victoria Vinky
 *
 */
public class RecognizerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private ArrayList<String> mFingerprints;
    private double[] mCurrentEnergy, mPreEnergy;
    private String mMediaId;

    public RecognizerServlet() {
        mFingerprints = new ArrayList<>();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("true".equals(request.getParameter("reset"))) {
            mFingerprints.clear();
            mCurrentEnergy = null;
            mPreEnergy = null;
            mMediaId = null;
            return;
        }
        generateAudioFingerprint(getData(request.getParameter("samples")));
        MetadataResponse metadataResponse = new MetadataResponse();
        switch (searchEntity(request, response)) {
        case 0:
            metadataResponse.mStatusCode = Constants.RESPONSE_STATUS_CODE_CONTINUE;
            break;
        case 1:
            metadataResponse.mStatusCode = Constants.RESPONSE_STATUS_CODE_NOT_FOUND;
            break;
        case 2:
            metadataResponse.mStatusCode = Constants.RESPONSE_STATUS_CODE_SUCCESS;
            metadataResponse.mMediaFile = searchMetadata();
            break;
        }

        String json = new Gson().toJson(metadataResponse);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    /**
     * @return 0 - no enough size, 1 - cannot find, 2 - found
     */
    private int searchEntity(HttpServletRequest request, HttpServletResponse response) {
        if (mFingerprints.size() < 256) {
            return 0;
        }
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        for (int i = 0; i < 256; i++) {
            String fingerprint = mFingerprints.get(i);
            try {
                Key keyLUT = KeyFactory.createKey(Constants.KIND_LOOK_UP_TABLE, fingerprint);
                Entity entityLUT = datastore.get(keyLUT);
                for (int j = 0, size = entityLUT.getProperties().size(); j < size; j++) {
                    EmbeddedEntity embeddedEntity = (EmbeddedEntity) entityLUT.getProperty(j + "");
                    String mediaId = (String) embeddedEntity.getProperty("mediaId");
                    String index = (String) embeddedEntity.getProperty("index");

                    Key keyAF = KeyFactory.createKey(Constants.KIND_AUDIO_FINGERPRINT, mediaId);
                    Entity entityAF = datastore.get(keyAF);
                    ArrayList<String> fingerprints = new ArrayList<>();
                    for (int k = 0, start = Integer.parseInt(index) - i; k < 256; k++) {
                        fingerprints.add((String) entityAF.getProperty((start + k) + ""));
                    }
                    if (compare2Fingerprint(mFingerprints, fingerprints)) {
                        mMediaId = mediaId;
                        return 2;
                    }
                }
            } catch (EntityNotFoundException expected) {
            }
        }
        mFingerprints.remove(0);
        return 1;
    }

    private MediaFile searchMetadata() {
        MediaFile mediaFile = null;
        try {
            Key keyMetadata = KeyFactory.createKey(Constants.KIND_METADATA, mMediaId);
            Entity entityMetadata = DatastoreServiceFactory.getDatastoreService().get(keyMetadata);
            mediaFile = new MediaFile();
            mediaFile.title = (String) entityMetadata.getProperty("title");
            mediaFile.artist = (String) entityMetadata.getProperty("artist");
            mediaFile.album = (String) entityMetadata.getProperty("album");
            mediaFile.albumArtist = (String) entityMetadata.getProperty("albumArtist");
            mediaFile.genre = (String) entityMetadata.getProperty("genre");
            mediaFile.trackNumber = (String) entityMetadata.getProperty("trackNumber");
            mediaFile.releaseDate = (String) entityMetadata.getProperty("releaseDate");
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        return mediaFile;
    }

    private boolean compare2Fingerprint(List<String> src, List<String> dest) {
        int count = 0;
        for (int i = 0; i < 256; i++) {
            if (src.get(i).equals(dest.get(i))) {
                count++;
            }
        }
        return count >= 192 ? true : false; // 192 = 256 * 75%
    }

    private short[] getData(String sampleStr) {
        System.out.println(sampleStr);
        String[] sampleStrs = sampleStr.split(",");
        short[] samples = new short[2048];
        for (int i = 0; i < 2048; i++) {
            samples[i] = Short.parseShort(sampleStrs[i]);
        }
        return samples;
    }

    private void generateAudioFingerprint(short[] samples) {
        String fingerprint = null;
        mCurrentEnergy = AudioFingerprintExtractor.getIntance().computeEnergyForAllBand(samples);
        if (mPreEnergy != null) {
            fingerprint = AudioFingerprintExtractor.getIntance().extract(mPreEnergy, mCurrentEnergy);
            mFingerprints.add(fingerprint);
        }
        mPreEnergy = mCurrentEnergy;
    }
}
