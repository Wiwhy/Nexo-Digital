package com.nexodigital.controller;

import com.nexodigital.model.ListarNoticiasDAO;
import com.nexodigital.model.Noticia;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/noticias/listar")
public class ListarNoticiasServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        ListarNoticiasDAO dao = new ListarNoticiasDAO();
        List<Noticia> noticias = dao.listarTodas();

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < noticias.size(); i++) {
            Noticia n = noticias.get(i);
            json.append("{");
            json.append("\"id\":").append(n.getId()).append(",");
            json.append("\"titulo\":\"").append(escaparJson(n.getTitulo())).append("\",");
            json.append("\"contenido\":\"").append(escaparJson(n.getContenido())).append("\",");
            json.append("\"autor\":\"").append(escaparJson(n.getAutor())).append("\",");
            json.append("\"fechaPublicacion\":\"").append(escaparJson(n.getFechaPublicacion())).append("\",");
            if (n.getNombreImagen() != null) {
                json.append("\"nombreImagen\":\"").append(escaparJson(n.getNombreImagen())).append("\"");
            } else {
                json.append("\"nombreImagen\":null");
            }
            json.append("}");

            if (i < noticias.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        out.print(json.toString());
        out.flush();
    }

    private String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}