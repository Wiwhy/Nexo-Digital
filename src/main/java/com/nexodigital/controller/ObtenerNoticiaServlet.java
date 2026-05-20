// ============================================================
// PAQUETE: Capa CONTROLLER del proyecto.
// ============================================================
package com.nexodigital.controller;

// Gson es una librería de Google que convierte objetos Java a JSON y viceversa.
// En lugar de construir el JSON a mano (como en ListarNoticiasServlet),
// aquí usamos Gson para hacerlo automáticamente.
// Gson lee todos los campos del objeto Noticia y los convierte a JSON.
import com.google.gson.Gson;
import com.nexodigital.model.Noticia;
import com.nexodigital.model.ObtenerNoticiaDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

// ============================================================
// SERVLET: ObtenerNoticiaServlet
//
// URL: GET /api/noticias/obtener?id=5
//
// Devuelve los datos COMPLETOS de UNA sola noticia, identificada
// por su ID que viene como parámetro en la URL (?id=...).
//
// Se usa cuando el usuario hace clic en una noticia en la página
// principal y se abre la página noticia.html con el artículo completo.
// ============================================================
@WebServlet("/api/noticias/obtener")
public class ObtenerNoticiaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configuramos UTF-8 para entrada y salida para soportar caracteres especiales.
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        // Instanciamos Gson: esta clase se encargará de convertir el objeto Noticia a JSON.
        Gson gson = new Gson();

        // Leemos el parámetro "id" de la URL.
        // Ejemplo de URL: /api/noticias/obtener?id=3
        // request.getParameter("id") devuelve el String "3".
        String idParam = request.getParameter("id");

        // VALIDACIÓN: Si no se proporcionó el parámetro "id", es una petición incorrecta.
        // idParam.trim() elimina espacios al inicio y al final.
        // isEmpty() devuelve true si el String está vacío "".
        if (idParam == null || idParam.trim().isEmpty()) {
            // 400 Bad Request: el cliente envió una petición incorrecta (falta el ID).
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"ID es requerido\"}");
            out.flush();
            return; // Cortamos la ejecución aquí.
        }

        try {
            // Convertimos el String "3" al entero 3.
            // Si no es un número (ej. "abc"), lanza NumberFormatException → lo capturamos abajo.
            int id = Integer.parseInt(idParam);

            // Llamamos al DAO para buscar la noticia con ese ID en la BD.
            ObtenerNoticiaDAO dao = new ObtenerNoticiaDAO();
            Noticia noticia = dao.obtenerPorId(id);

            if (noticia != null) {
                // Encontramos la noticia → respondemos con 200 OK.
                response.setStatus(HttpServletResponse.SC_OK); // 200
                // gson.toJson(noticia) convierte automáticamente el objeto Java a JSON.
                // Usa los nombres de los ATRIBUTOS de la clase Noticia como claves JSON:
                // { "id": 3, "titulo": "...", "contenido": "...", "nombreImagen": "...", ... }
                String jsonResponse = gson.toJson(noticia);
                out.print(jsonResponse);
            } else {
                // No encontramos noticia con ese ID → respondemos con 404 Not Found.
                response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
                out.print("{\"error\": \"Noticia no encontrada\"}");
            }

        } catch (NumberFormatException e) {
            // El parámetro "id" no era un número válido → petición incorrecta.
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            out.print("{\"error\": \"ID inválido\"}");
        } finally {
            // Siempre enviamos el buffer de respuesta.
            out.flush();
        }
    }
}