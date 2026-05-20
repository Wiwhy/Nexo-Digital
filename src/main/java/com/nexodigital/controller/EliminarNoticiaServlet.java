// ============================================================
// PAQUETE: Capa CONTROLLER del proyecto.
// ============================================================
package com.nexodigital.controller;

import com.nexodigital.model.EliminarNoticiaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

// ============================================================
// SERVLET: EliminarNoticiaServlet
//
// URL: POST /api/noticias/eliminar
//
// Gestiona la ELIMINACIÓN de una noticia.
// Es el más sencillo de los Servlets de operaciones CRUD:
// solo necesita el ID de la noticia para borrarla.
//
// NOTA: Se usa POST y no DELETE (que sería más "correcto" en REST puro)
// porque los formularios HTML estándar solo soportan GET y POST.
// Para simplificar, usamos POST para todas las operaciones de escritura.
// ============================================================
@WebServlet("/api/noticias/eliminar")
public class EliminarNoticiaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // VERIFICACIÓN DE SEGURIDAD: Solo admins logueados pueden eliminar.
        // Si el atributo "adminLogueado" no está en la sesión, bloqueamos la acción.
        if (request.getSession().getAttribute("adminLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            out.print("{\"status\":\"error\", \"message\":\"No tienes permisos para realizar esta acción\"}");
            return; // Cortamos aquí, no continuamos.
        }

        try {
            // Leemos el parámetro "id" de la petición.
            // DIFERENCIA con los Servlets con @MultipartConfig:
            // Aquí NO usamos leerParte() porque el formulario de eliminar
            // NO es multipart (no incluye archivos). Se usa un formulario simple
            // con application/x-www-form-urlencoded, así que request.getParameter() funciona directamente.
            //
            // Integer.parseInt() convierte el String "5" al entero 5.
            // Si "id" no es un número válido (ej. alguien envió "hola"), lanza NumberFormatException.
            int id = Integer.parseInt(request.getParameter("id"));

            // Creamos el DAO y ejecutamos el DELETE en la BD.
            EliminarNoticiaDAO dao = new EliminarNoticiaDAO();

            if (dao.eliminar(id)) {
                // El DELETE afectó al menos 1 fila → éxito.
                out.print("{\"status\":\"success\", \"message\":\"Noticia eliminada correctamente\"}");
            } else {
                // El DELETE no encontró ninguna fila con ese ID.
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
                out.print("{\"status\":\"error\", \"message\":\"No se pudo encontrar la noticia en la base de datos\"}");
            }
        } catch (Exception e) {
            // Si el ID no es un número válido u otro error inesperado:
            // Respondemos con 400 (Bad Request = petición mal formada).
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            out.print("{\"status\":\"error\", \"message\":\"ID de noticia inválido\"}");
        }

        // Enviamos la respuesta al cliente.
        out.flush();
    }
}