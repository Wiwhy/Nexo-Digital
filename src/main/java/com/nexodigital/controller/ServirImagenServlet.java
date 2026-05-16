package com.nexodigital.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/uploads/*")
public class ServirImagenServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = pathInfo.substring(1);
        
        // Primero intentamos buscar en el disco duro persistente de Railway
        File file = new File("/datos_persistentes", fileName);
        
        // Si no existe ahí (ej. entorno local), buscamos en la carpeta interna
        if (!file.exists()) {
            String localPath = getServletContext().getRealPath("") + File.separator + "uploads";
            file = new File(localPath, fileName);
        }

        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = getServletContext().getMimeType(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        
        response.setContentType(mimeType);
        response.setContentLength((int) file.length());

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
