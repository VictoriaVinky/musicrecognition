package com.musicrecognizer.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
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
import com.musicrecognizer.utilities.Constants;

@WebServlet(urlPatterns = {
        "/train"
})
public class TrainingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int BUFFER_SIZE = 9 * 1024 * 1024;
    private String mMediaId;
    private ArrayList<String> mFingerprints;
    private HashMap<String, ArrayList<String>> mFingerprintHM;
    PrintWriter out;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        out = response.getWriter();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        if ("true".equals(request.getParameter("newMediaFile"))) {
            insertMetadataStore(datastore, request, response);
            mMediaId = null;
            mFingerprintHM = new HashMap<>();
            mFingerprints = new ArrayList<>();
        } else if ("true".equals(request.getParameter("start"))) {
            insertLUTStore(datastore);
            insertAudioFingerprintStore(datastore);
        } else {
            // "mediaId=" + mediaFile.id + "&index=" + index + "&fingerprint=" + fingerprint;
            mMediaId = request.getParameter("mediaId");
            String fingerprint = request.getParameter("fingerprint");
            if (mFingerprintHM.containsKey(fingerprint)) {
                ArrayList<String> temp = mFingerprintHM.get(fingerprint);
                temp.add(request.getParameter("index"));
                mFingerprintHM.put(fingerprint, temp);
            } else {
                ArrayList<String> temp = new ArrayList<>();
                temp.add(request.getParameter("index"));
                mFingerprintHM.put(fingerprint, temp);
            }
            mFingerprints.add(fingerprint);
        }
    }

    private void insertMetadataStore(DatastoreService datastore, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        // "mediaId=" + id + "&title=" + title + "&artist=" + artist + "&album=" + album + "&albumArtist="
        // + albumArtist + "&genre=" + genre + "&trackNumber=" + trackNumber + "&releaseDate=" + releaseDate;
        Entity entityMetadata = new Entity(Constants.KIND_METADATA, request.getParameter("mediaId"));
        entityMetadata.setProperty("title", request.getParameter("title"));
        entityMetadata.setProperty("artist", request.getParameter("artist"));
        entityMetadata.setProperty("album", request.getParameter("album"));
        entityMetadata.setProperty("albumArtist", request.getParameter("albumArtist"));
        entityMetadata.setProperty("genre", request.getParameter("genre"));
        entityMetadata.setProperty("trackNumber", request.getParameter("trackNumber"));
        entityMetadata.setProperty("releaseDate", request.getParameter("releaseDate"));
        datastore.put(entityMetadata);

        out.println("insertMetadataStore: Success!");
    }

    private void insertLUTStore(DatastoreService datastore)
            throws IOException {
        ArrayList<String> fingerprints = new ArrayList<>(mFingerprintHM.keySet());
        out.println("insertLUTStore: " + fingerprints.size());
        for (int i = 0, size = fingerprints.size(); i < size; i++) {
            Key keyLUT = KeyFactory.createKey(Constants.KIND_LOOK_UP_TABLE, fingerprints.get(i));
            Entity entityLUT;
            try {
                entityLUT = datastore.get(keyLUT);
            } catch (EntityNotFoundException expected) {
                entityLUT = new Entity(Constants.KIND_LOOK_UP_TABLE, fingerprints.get(i));
            }
            ArrayList<String> listIndex = mFingerprintHM.get(fingerprints.get(i));
            for (int j = 0, sizej = listIndex.size(); j < sizej; j++) {
                EmbeddedEntity embeddedEntity = new EmbeddedEntity();
                embeddedEntity.setProperty("mediaId", mMediaId);
                embeddedEntity.setProperty("index", listIndex.get(j));
                entityLUT.setProperty(j + "", embeddedEntity);
            }
            datastore.put(entityLUT);
        }
    }

    private void insertAudioFingerprintStore(DatastoreService datastore) throws IOException {
        Key keyAF = KeyFactory.createKey(Constants.KIND_AUDIO_FINGERPRINT, mMediaId);
        Entity entityAF;
        try {
            entityAF = datastore.get(keyAF);
        } catch (EntityNotFoundException expected) {
            entityAF = new Entity(Constants.KIND_AUDIO_FINGERPRINT, mMediaId);
            // entityAF.setProperty("mediaId", mediaId);
        }
        for (int i = 0, size = mFingerprints.size(); i < size; i++) {
            entityAF.setProperty(i + "", mFingerprints.get(i));
        }
        datastore.put(entityAF);
        out.println("insertAudioFingerprintStore: SUCCESS: " + mFingerprints.size());
    }

    /**
     * Transfer the data from the inputStream to the outputStream. Then close both streams.
     */
    @SuppressWarnings("unused")
    private void copy(InputStream input, OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = input.read(buffer);
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }
}
