package com.nexodigital.controller;

import com.nexodigital.model.EliminarNoticiaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/noticias/eliminar")
public class EliminarNoticiaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (request.getSession(false) == null || request.getSession(false).getAttribute("adminLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"status\":\"error\", \"message\":\"No tienes permisos para realizar esta acción\"}");
            return;
        }

        try {
            int id = Integer.parseInt(request.getParameter("id"));

            EliminarNoticiaDAO dao = new EliminarNoticiaDAO();

            if (dao.eliminar(id)) {
                out.print("{\"status\":\"success\", \"message\":\"Noticia eliminada correctamente\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\", \"message\":\"No se pudo encontrar la noticia en la base de datos\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\", \"message\":\"ID de noticia inválido\"}");
        }

        out.flush();
    }
}