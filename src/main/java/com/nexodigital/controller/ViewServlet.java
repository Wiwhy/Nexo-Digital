package com.nexodigital.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@WebServlet("/view/*")
public class ViewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            pathInfo = "/index.html";
        }
        
        String filePath = getServletContext().getRealPath("/view" + pathInfo);
        
        try {
            byte[] content = Files.readAllBytes(Paths.get(filePath));
            
            // Set content type based on file extension
            if (pathInfo.endsWith(".html")) {
                response.setContentType("text/html; charset=UTF-8");
            } else if (pathInfo.endsWith(".css")) {
                response.setContentType("text/css");
            } else if (pathInfo.endsWith(".js")) {
                response.setContentType("application/javascript");
            } else if (pathInfo.endsWith(".png")) {
                response.setContentType("image/png");
            } else if (pathInfo.endsWith(".jpg") || pathInfo.endsWith(".jpeg")) {
                response.setContentType("image/jpeg");
            }
            
            response.getOutputStream().write(content);
            response.getOutputStream().flush();
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
