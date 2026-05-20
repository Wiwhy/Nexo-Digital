// ============================================================
// PAQUETE: Capa CONTROLLER del proyecto.
// ============================================================
package com.nexodigital.controller;

import com.nexodigital.model.LoginDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// HttpSession representa la SESIÓN del usuario en el servidor.
// Una sesión es como un "token" que el servidor guarda para recordar
// que un usuario ya se ha autenticado. Sin sesiones, cada petición
// HTTP sería anónima (HTTP es "stateless" = sin estado por defecto).
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

// ============================================================
// ANOTACIÓN @WebServlet
//
// Este Servlet responde a la URL /api/login.
// Cuando el admin envía el formulario de login, el JavaScript
// hace un POST a esta URL con usuario y contraseña.
// ============================================================
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    // ----------------------------------------------------------
    // MÉTODO: doPost
    //
    // El formulario de login usa el método POST (no GET),
    // porque POST envía datos de forma más segura (no van en la URL).
    //
    // Este método recibe el usuario y contraseña, los verifica
    // contra la BD, y si son correctos crea una sesión de admin.
    // ----------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configuramos la respuesta como JSON.
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // LEEMOS LOS PARÁMETROS DEL FORMULARIO
        //
        // request.getParameter("nombre") lee un campo del formulario HTML
        // o un parámetro de la URL.
        // "usuario" y "password" son los nombres de los campos del formulario
        // en admin.html (o del body de la petición fetch desde JavaScript).
        String usuario = request.getParameter("usuario");
        String password = request.getParameter("password");

        // Creamos el DAO para verificar las credenciales contra la BD.
        LoginDAO loginDAO = new LoginDAO();

        // Llamamos al método autenticar() del DAO.
        // Devuelve true si existe un administrador con ese usuario y contraseña.
        if (loginDAO.autenticar(usuario, password)) {

            // CREDENCIALES CORRECTAS → CREAMOS LA SESIÓN
            //
            // request.getSession() obtiene la sesión actual o crea una nueva.
            // La sesión es un objeto que Tomcat guarda en memoria del servidor.
            // Al cliente (navegador) le envía solo una COOKIE con el ID de sesión
            // (normalmente llamada "JSESSIONID"), no los datos de la sesión.
            HttpSession session = request.getSession();

            // Guardamos en la sesión un atributo que indica que el admin está logueado.
            // "adminLogueado" es la clave; "true" (booleano) es el valor.
            // Otros Servlets comprueben si este atributo existe para saber
            // si el usuario tiene permiso para hacer operaciones de admin.
            session.setAttribute("adminLogueado", true);

            // También guardamos el nombre de usuario para mostrarlo en la UI.
            session.setAttribute("usuario", usuario);

            // Respondemos con éxito en formato JSON.
            // El frontend (JavaScript) leerá este JSON y si el status es "success"
            // redirigirá al usuario a la página de admin.
            out.print("{\"status\":\"success\", \"message\":\"Login correcto\"}");

        } else {
            // CREDENCIALES INCORRECTAS
            //
            // Establecemos el código de estado HTTP 401 (Unauthorized = No autorizado).
            // Los códigos HTTP indican el resultado de la petición:
            //   200 → OK (éxito)
            //   401 → Unauthorized (no autenticado)
            //   403 → Forbidden (autenticado pero sin permisos)
            //   404 → Not Found (no encontrado)
            //   500 → Internal Server Error (error del servidor)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            out.print("{\"status\":\"error\", \"message\":\"Usuario o contraseña incorrectos\"}");
        }

        // Enviamos los datos al cliente.
        out.flush();
    }
}