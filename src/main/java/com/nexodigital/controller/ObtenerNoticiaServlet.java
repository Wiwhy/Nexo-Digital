package com.nexodigital.controller;

import com.google.gson.Gson;
import com.nexodigital.model.Noticia;
import com.nexodigital.model.NoticiaDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/noticias/obtener")
public class ObtenerNoticiaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Buena práctica: Asegurar que todo lo que entra y sale está en UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        String idParam = request.getParameter("id");

        if (idParam == null || idParam.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"ID es requerido\"}");
            out.flush();
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            NoticiaDAO dao = new NoticiaDAO();
            Noticia noticia = dao.obtenerPorId(id);

            if (noticia != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                String jsonResponse = gson.toJson(noticia);
                out.print(jsonResponse);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Noticia no encontrada\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"ID inválido\"}");
        } finally {
            out.flush();
        }
    }
}