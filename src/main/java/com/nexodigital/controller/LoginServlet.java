package com.nexodigital.controller;

import com.nexodigital.model.LoginDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String usuario = request.getParameter("usuario");
        String password = request.getParameter("password");

        LoginDAO loginDAO = new LoginDAO();

        if (loginDAO.autenticar(usuario, password)) {
            // Si el login es correcto, creamos una sesión
            HttpSession session = request.getSession();
            session.setAttribute("adminLogueado", true);
            session.setAttribute("usuario", usuario);

            out.print("{\"status\":\"success\", \"message\":\"Login correcto\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"status\":\"error\", \"message\":\"Usuario o contraseña incorrectos\"}");
        }
        out.flush();
    }
}