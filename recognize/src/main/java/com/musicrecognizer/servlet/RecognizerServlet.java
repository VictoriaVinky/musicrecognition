package com.musicrecognizer.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

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

public class RecognizerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        java.io.PrintWriter out = response.getWriter();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        if ("true".equals(request.getParameter("isRecorded"))) {

        } else {
            String fingerprint = request.getParameter("fingerprint");
            Key keyLUT = KeyFactory.createKey(Constants.KIND_LOOK_UP_TABLE, fingerprint);
            try {
                Entity entityLUT = datastore.get(keyLUT);
                for (int i = 0, size = entityLUT.getProperties().size(); i < size; i++) {
                    EmbeddedEntity embeddedEntity = (EmbeddedEntity) entityLUT.getProperty(i + "");
                    
                }
            } catch (EntityNotFoundException expected) {
            }
        }
    }
}
