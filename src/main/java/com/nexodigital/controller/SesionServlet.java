package com.nexodigital.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/sesion")
public class SesionServlet extends HttpServlet {

    // GET → devuelve si hay admin logueado
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        boolean logueado = session != null && session.getAttribute("adminLogueado") != null;
        String usuario = logueado ? (String) session.getAttribute("usuario") : "";

        out.print("{\"logueado\":" + logueado + ", \"usuario\":\"" + usuario + "\"}");
        out.flush();
    }

    // POST con action=logout → cierra sesión
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            out.print("{\"status\":\"success\", \"message\":\"Sesión cerrada\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\", \"message\":\"Acción no reconocida\"}");
        }
        out.flush();
    }
}
