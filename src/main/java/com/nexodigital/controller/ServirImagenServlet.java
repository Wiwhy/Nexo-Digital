// ============================================================
// PAQUETE: Capa CONTROLLER del proyecto.
// ============================================================
package com.nexodigital.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// File representa un archivo o directorio en el sistema de archivos.
import java.io.File;
// FileInputStream abre un archivo para leerlo como stream de bytes.
import java.io.FileInputStream;
import java.io.IOException;
// OutputStream es el canal de salida para escribir bytes en la respuesta HTTP.
// Necesitamos bytes (no texto) para enviar imágenes.
import java.io.OutputStream;

// ============================================================
// SERVLET: ServirImagenServlet
//
// URL: GET /uploads/{nombreArchivo}
//   Ejemplo: GET /uploads/abc123.jpg
//
// PROBLEMA QUE RESUELVE:
// Las imágenes subidas se guardan en /datos_persistentes (en Railway)
// o en una carpeta "uploads" (en local). Estas carpetas NO son
// accesibles directamente por el navegador (están fuera de la
// carpeta pública de la app web).
//
// SOLUCIÓN:
// Este Servlet actúa como "intermediario": el navegador le pide la imagen,
// el Servlet la busca en el disco y se la devuelve como respuesta HTTP.
// Así el navegador puede mostrar la imagen aunque esté en una ruta privada.
//
// La URL /uploads/* significa que este Servlet responde a CUALQUIER URL
// que empiece por /uploads/ (el * es un comodín).
// ============================================================
@WebServlet("/uploads/*")
public class ServirImagenServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // request.getPathInfo() devuelve la parte de la URL después de /uploads.
        // Ejemplo: si la URL es /uploads/abc123.jpg → pathInfo = "/abc123.jpg"
        String pathInfo = request.getPathInfo();

        // Si la URL es exactamente /uploads/ (sin nombre de archivo) → error 404.
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404
            return;
        }

        // Extraemos el nombre del archivo eliminando la barra inicial "/".
        // substring(1) corta el primer carácter (la barra "/").
        // Ejemplo: "/abc123.jpg" → "abc123.jpg"
        String fileName = pathInfo.substring(1);

        // BUSCAMOS EL ARCHIVO EN LAS POSIBLES UBICACIONES:

        // 1. Primero buscamos en /datos_persistentes (el volumen persistente de Railway).
        // new File(directorio, nombreArchivo) construye la ruta completa.
        File file = new File("/datos_persistentes", fileName);

        // 2. Si no existe en /datos_persistentes (ej. en desarrollo local),
        //    buscamos en la carpeta "uploads" dentro de la aplicación web desplegada.
        if (!file.exists()) {
            // getServletContext().getRealPath("") devuelve la ruta real en disco
            // de la raíz de la aplicación web (donde Tomcat la desplegó).
            String localPath = getServletContext().getRealPath("") + File.separator + "uploads";
            file = new File(localPath, fileName);
        }

        // Si el archivo no existe en ninguna ubicación → error 404.
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404
            return;
        }

        // Determinamos el tipo MIME del archivo según su extensión.
        // MIME type es el "tipo de contenido": image/jpeg, image/png, image/gif...
        // getServletContext().getMimeType() usa el tipo de extensión del archivo
        // para determinar el MIME type automáticamente.
        String mimeType = getServletContext().getMimeType(file.getName());
        if (mimeType == null) {
            // Si no reconoce la extensión, usamos el tipo genérico de bytes.
            // El navegador intentará inferir el tipo por su cuenta.
            mimeType = "application/octet-stream";
        }

        // Configuramos la respuesta:
        // Le decimos al navegador qué tipo de archivo le estamos enviando.
        response.setContentType(mimeType);
        // Le decimos cuántos bytes tiene el archivo para que el navegador
        // pueda mostrar el progreso de descarga.
        response.setContentLength((int) file.length());

        // LEEMOS EL ARCHIVO Y LO ENVIAMOS AL NAVEGADOR byte a byte.
        // Try-with-resources: gestiona el FileInputStream y el OutputStream.
        // FileInputStream abre el archivo para leerlo.
        // response.getOutputStream() es el canal de salida para enviar bytes.
        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {

            // Creamos un buffer de 4096 bytes (4 KB) para leer el archivo en trozos.
            // Leer de 4 KB en 4 KB es mucho más eficiente que leer byte a byte.
            byte[] buffer = new byte[4096];
            int bytesRead;

            // Leemos hasta que el archivo se acabe.
            // in.read(buffer) lee hasta 4096 bytes y los guarda en el buffer.
            //   Devuelve cuántos bytes leyó, o -1 si llegó al final del archivo.
            while ((bytesRead = in.read(buffer)) != -1) {
                // Escribimos en la respuesta solo los bytes que realmente se leyeron
                // (los últimos bytes del archivo pueden ser menos de 4096).
                out.write(buffer, 0, bytesRead);
            }
            // Cuando el while termina (bytesRead == -1), el archivo completo fue enviado.
        }
        // FileInputStream e OutputStream se cierran automáticamente aquí (try-with-resources).
    }
}
