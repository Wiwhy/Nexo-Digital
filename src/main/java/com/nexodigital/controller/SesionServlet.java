// ============================================================
// PAQUETE: Capa CONTROLLER del proyecto.
// ============================================================
package com.nexodigital.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

// ============================================================
// SERVLET: SesionServlet
//
// URL: /api/sesion
//
// Este Servlet gestiona DOS operaciones relacionadas con la sesión,
// dependiendo del MÉTODO HTTP usado:
//
//   GET  /api/sesion → Consulta si hay un admin logueado.
//                      Devuelve: {"logueado": true/false, "usuario": "admin"}
//
//   POST /api/sesion (con action=logout) → Cierra la sesión del admin.
//                      Devuelve: {"status":"success", "message":"Sesión cerrada"}
//
// PARA QUÉ SIRVE:
// El frontend (app.js, admin.js, noticia.js) llama a GET /api/sesion
// al cargar cada página para saber si mostrar controles de admin o no.
// Esto es necesario porque HTTP es "stateless": el navegador no sabe
// automáticamente si el usuario está logueado; debe preguntárselo al servidor.
// ============================================================
@WebServlet("/api/sesion")
public class SesionServlet extends HttpServlet {

    // ----------------------------------------------------------
    // MÉTODO: doGet → Comprueba si hay sesión activa
    // ----------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // request.getSession(false) obtiene la sesión existente SIN crear una nueva.
        // Si no hay sesión activa → devuelve null.
        // El "false" es crucial aquí: getSession() sin argumento siempre crea sesión.
        HttpSession session = request.getSession(false);

        // Comprobamos si hay sesión Y si tiene el atributo "adminLogueado".
        // El operador "&&" (AND) evalúa ambas condiciones:
        //   - session != null → hay sesión activa
        //   - session.getAttribute("adminLogueado") != null → hay atributo de admin
        boolean logueado = session != null && session.getAttribute("adminLogueado") != null;

        // Si está logueado, obtenemos el nombre de usuario de la sesión.
        // Si no está logueado, usamos cadena vacía "".
        // Operador ternario: condicion ? valorSiTrue : valorSiFalse
        // (String) hace un cast: getAttribute() devuelve Object, necesitamos String.
        String usuario = logueado ? (String) session.getAttribute("usuario") : "";

        // Construimos y enviamos el JSON de respuesta.
        // Ejemplo de respuesta: {"logueado":true, "usuario":"admin"}
        // o: {"logueado":false, "usuario":""}
        out.print("{\"logueado\":" + logueado + ", \"usuario\":\"" + usuario + "\"}");
        out.flush();
    }

    // ----------------------------------------------------------
    // MÉTODO: doPost → Cierra la sesión (logout)
    //
    // El frontend envía: POST /api/sesion con body "action=logout"
    // ----------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Leemos el parámetro "action" del body de la petición.
        String action = request.getParameter("action");

        // Comprobamos que la acción solicitada sea "logout".
        // Usamos "logout".equals(action) en lugar de action.equals("logout")
        // para evitar NullPointerException si "action" fuera null.
        if ("logout".equals(action)) {
            // Obtenemos la sesión existente (sin crear una nueva).
            HttpSession session = request.getSession(false);
            if (session != null) {
                // session.invalidate() destruye completamente la sesión en el servidor.
                // El cliente ya no tendrá un token de sesión válido.
                // Esto equivale a "cerrar sesión".
                session.invalidate();
            }
            out.print("{\"status\":\"success\", \"message\":\"Sesión cerrada\"}");
        } else {
            // Si "action" no es "logout", es una petición no reconocida.
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            out.print("{\"status\":\"error\", \"message\":\"Acción no reconocida\"}");
        }
        out.flush();
    }
}
