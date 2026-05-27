// ============================================================
// PAQUETE: Capa CONTROLLER del proyecto.
// ============================================================
package com.nexodigital.controller;

import com.nexodigital.model.ActualizarNoticiaDAO;
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

// ============================================================
// ESTE SERVLET ES MUY SIMILAR A CrearNoticiaServlet.
// La diferencia clave es:
//   - Crear → INSERT INTO (crea una fila nueva)
//   - Actualizar → UPDATE (modifica una fila existente, identificada por su ID)
//
// URL: POST /api/noticias/actualizar
// ============================================================
@WebServlet("/api/noticias/actualizar")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2,
                 maxFileSize = 1024 * 1024 * 10,
                 maxRequestSize = 1024 * 1024 * 50)
public class ActualizarNoticiaServlet extends HttpServlet {

    // Método auxiliar para leer campos de texto de un formulario multipart.
    // (Idéntico al de CrearNoticiaServlet - lee la parte y la convierte a String)
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

        // VERIFICACIÓN DE SEGURIDAD: Solo administradores autenticados pueden actualizar.
        // getSession(false) → obtiene la sesión existente sin crear una nueva.
        // Si no hay sesión activa, devuelve null → el usuario no está autenticado.
        if (request.getSession(false) == null || request.getSession(false).getAttribute("adminLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            out.print("{\"status\":\"error\", \"message\":\"No tienes permisos para realizar esta acción\"}");
            return; // Cortamos la ejecución aquí.
        }

        try {
            // DIFERENCIA CLAVE vs. CrearNoticiaServlet:
            // Aquí leemos también el "id" de la noticia a actualizar.
            // El formulario del admin incluye un campo oculto con el ID de la noticia existente.
            String idStr = leerParte(request, "id");
            // Convertimos el String "1" al número entero 1.
            // Integer.parseInt() lanza NumberFormatException si el valor no es un número válido.
            int id = Integer.parseInt(idStr);

            // Leemos los demás campos igual que en CrearNoticiaServlet.
            String titulo = leerParte(request, "titulo");
            String contenido = leerParte(request, "contenido");
            String autor = leerParte(request, "autor");

            // Leemos la imagen (puede ser null si no se sube una nueva).
            Part filePart = request.getPart("imagen");
            String nombreImagenFinal = null;

            // Si el admin subió una imagen nueva, la procesamos y guardamos.
            if (filePart != null && filePart.getSize() > 0) {
                String nombreOriginal = filePart.getSubmittedFileName();
                String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
                nombreImagenFinal = UUID.randomUUID().toString() + extension;

                // Misma lógica de rutas que en CrearNoticiaServlet:
                // Intentamos guardar en /datos_persistentes (Railway), o en "uploads" como fallback.
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
            // Si nombreImagenFinal es null → el DAO usará el SQL sin actualizar la imagen.

            // Creamos el objeto Noticia con los datos nuevos.
            Noticia noticia = new Noticia(titulo, contenido, nombreImagenFinal, autor);
            // DIFERENCIA CLAVE: asignamos el ID al objeto para que el UPDATE
            // sepa QUÉ fila de la BD debe modificar (WHERE id=?).
            noticia.setId(id);

            // Llamamos al DAO para ejecutar el UPDATE en la BD.
            ActualizarNoticiaDAO dao = new ActualizarNoticiaDAO();
            if (dao.actualizar(noticia)) {
                // Éxito: el UPDATE modificó al menos 1 fila.
                out.print("{\"status\":\"success\", \"message\":\"Noticia actualizada correctamente\"}");
            } else {
                // El UPDATE no modificó ninguna fila (¿ID no existe?).
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\", \"message\":\"Error al actualizar la noticia en MySQL\"}");
            }

        } catch (Exception e) {
            // Capturamos cualquier error inesperado y respondemos con error 500.
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        } finally {
            // Siempre enviamos el buffer de respuesta.
            out.flush();
        }
    }
}