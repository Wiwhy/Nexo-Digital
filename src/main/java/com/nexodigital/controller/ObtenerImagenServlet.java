package com.nexodigital.controller;

import com.nexodigital.service.S3Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/noticias/imagen/*")
public class ObtenerImagenServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Extraer el nombre del archivo de la URL
        // /api/noticias/imagen/uuid-123.jpg -> uuid-123.jpg
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().print("{\"error\":\"Imagen no encontrada\"}");
            return;
        }
        
        String nombreImagen = pathInfo.substring(1); // Quitar el primer /
        
        try {
            // Obtener la URL de S3 y redirigir
            String urlS3 = S3Service.getImageUrl(nombreImagen);
            response.sendRedirect(urlS3);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"Error al obtener la imagen\"}");
        }
    }
}
