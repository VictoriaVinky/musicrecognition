package com.musicrecognizer.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

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
import com.musicrecognizer.utilities.Constants;

public class TrainingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int BUFFER_SIZE = 9 * 1024 * 1024;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        if ("true".equals(request.getParameter("newMediaFile"))) {
            insertMetadataStore(datastore, request, response);
        } else {
            // "mediaId=" + mediaFile.id + "&index=" + index + "&fingerprint=" + fingerprint;
            insertLUTStore(datastore, request, response);
            insertAudioFingerprintStore(datastore, request, response);
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

    private void insertLUTStore(DatastoreService datastore, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        String index = request.getParameter("index");
        String mediaId = request.getParameter("mediaId");
        String fingerprint = request.getParameter("fingerprint");

        Key keyLUT = KeyFactory.createKey(Constants.KIND_LOOK_UP_TABLE, fingerprint);
        Entity entityLUT;
        int size = 0;
        try {
            entityLUT = datastore.get(keyLUT);
            size = entityLUT.getProperties().size();
        } catch (EntityNotFoundException expected) {
            entityLUT = new Entity(Constants.KIND_LOOK_UP_TABLE, fingerprint);
        }

        EmbeddedEntity embeddedEntity = new EmbeddedEntity();
        embeddedEntity.setProperty("mediaId", mediaId);
        embeddedEntity.setProperty("index", index);
        entityLUT.setProperty(size + "", embeddedEntity);
        datastore.put(entityLUT);
        out.println("insertLUTStore: Success!");
    }

    private void insertAudioFingerprintStore(DatastoreService datastore, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        String index = request.getParameter("index");
        String mediaId = request.getParameter("mediaId");
        String fingerprint = request.getParameter("fingerprint");
        Key keyAF = KeyFactory.createKey(Constants.KIND_AUDIO_FINGERPRINT, mediaId);
        Entity entityAF;
        try {
            entityAF = datastore.get(keyAF);
        } catch (EntityNotFoundException expected) {
            entityAF = new Entity(Constants.KIND_AUDIO_FINGERPRINT, mediaId);
//            entityAF.setProperty("mediaId", mediaId);
        }
        entityAF.setProperty(index, fingerprint);
        datastore.put(entityAF);
        out.println("insertAudioFingerprintStore: Success!");
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
