package com.nexodigital.controller;

import com.nexodigital.model.CrearNoticiaDAO;
import com.nexodigital.model.Noticia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

@WebServlet("/api/noticias")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 50)
public class CrearNoticiaServlet extends HttpServlet {

    private String leerParte(HttpServletRequest request, String nombre) throws IOException, ServletException {
        Part part = request.getPart(nombre);
        if (part == null) return null;
        return new String(part.getInputStream().readAllBytes(), "UTF-8");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (request.getSession(false) == null || request.getSession(false).getAttribute("adminLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"status\":\"error\", \"message\":\"No tienes permisos para realizar esta acción\"}");
            out.flush();
            return;
        }

        try {
            String titulo = leerParte(request, "titulo");
            String contenido = leerParte(request, "contenido");
            String autor = leerParte(request, "autor");

            Part filePart = request.getPart("imagen");
            String nombreImagenFinal = null;

            if (filePart != null && filePart.getSize() > 0) {
                String nombreOriginal = filePart.getSubmittedFileName();
                String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
                nombreImagenFinal = UUID.randomUUID().toString() + extension;

                String uploadPath = "/datos_persistentes";
                File uploadDir = new File(uploadPath);

                if (!uploadDir.exists()) {
                    try {
                        uploadDir.mkdirs();
                    } catch (SecurityException ignored) {}
                }

                if (!uploadDir.exists() || !uploadDir.canWrite()) {
                    uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
                    uploadDir = new File(uploadPath);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
                    }
                }

                filePart.write(uploadPath + File.separator + nombreImagenFinal);
            }

            Noticia nuevaNoticia = new Noticia(titulo, contenido, nombreImagenFinal, autor);
            CrearNoticiaDAO dao = new CrearNoticiaDAO();
            boolean guardadoExitoso = dao.crear(nuevaNoticia);

            if (guardadoExitoso) {
                out.print("{\"status\":\"success\", \"message\":\"Noticia publicada correctamente\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\", \"message\":\"Error al guardar en la base de datos.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }
}