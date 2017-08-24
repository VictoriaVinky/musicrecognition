package com.musicrecognizer.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.musicrecognizer.utilities.Constants;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@WebServlet(urlPatterns = {
        "/upload/*"
})
public class UploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Storage storage = null;
    static {
        storage = (Storage) StorageOptions.getDefaultInstance().getService();
    }

    public UploadServlet() {}

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/WEB-INF/jsps/upload.jsp");
        dispatcher.forward(request, response);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (ServletFileUpload.isMultipartContent(request)) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            // factory.setSizeThreshold(10 * 1024 * 1024);
            // factory.setRepository(new File("c:\\temp"));
            ServletFileUpload upload = new ServletFileUpload(factory);
            // upload.setSizeMax(10 * 1024 * 1024);
            // Parse the request to get file items.
            List<FileItem> fileItems = null;
            try {
                fileItems = upload.parseRequest(request);
            } catch (FileUploadException e) {
                e.printStackTrace();
            }

            // Process the uploaded file items
            Iterator<FileItem> i = fileItems.iterator();

            while (i.hasNext()) {
                FileItem fi = i.next();
                if (!fi.isFormField()) {
                    // Get the uploaded file parameters
                    String fileName = fi.getName();
                    BlobInfo blobInfo = storage.create(BlobInfo.newBuilder(Constants.BUCKET_NAME, fileName).build(),
                            fi.getInputStream());
                }
            }
        }
    }
}
