// ============================================================
// PAQUETE: Capa CONTROLLER del proyecto.
// ============================================================
package com.nexodigital.controller;

import com.nexodigital.model.CrearNoticiaDAO;
import com.nexodigital.model.Noticia;

import jakarta.servlet.ServletException;
// @MultipartConfig es necesaria para recibir archivos subidos (imágenes).
// Sin esta anotación, el Servlet no puede procesar formularios con archivos.
// Los parámetros de la anotación definen límites de tamaño:
//   - fileSizeThreshold: si el archivo supera 2MB, se guarda en disco temporal (no en RAM)
//   - maxFileSize: tamaño máximo de un archivo individual (10MB)
//   - maxRequestSize: tamaño máximo de TODA la petición (50MB)
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// Part representa UNA parte de un formulario multipart.
// Un formulario multipart es el que permite enviar tanto campos de texto
// como archivos en la misma petición HTTP.
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
// UUID (Universally Unique Identifier) genera identificadores únicos y aleatorios.
// Lo usamos para renombrar las imágenes subidas y evitar colisiones de nombres.
// Ejemplo: "a3f2c8d1-7e45-4b09-..." + ".jpg"
import java.util.UUID;

// ============================================================
// ANOTACIÓN @WebServlet("/api/noticias")
// Responde a POST /api/noticias (la URL sin ruta adicional).
// ============================================================
@WebServlet("/api/noticias")
// Límites de tamaño para la subida de archivos (1024*1024 = 1 MB)
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2,  // 2 MB → umbral para usar disco temporal
                 maxFileSize = 1024 * 1024 * 10,        // 10 MB → máximo por archivo
                 maxRequestSize = 1024 * 1024 * 50)     // 50 MB → máximo por petición
public class CrearNoticiaServlet extends HttpServlet {

    // ----------------------------------------------------------
    // MÉTODO PRIVADO DE AYUDA: leerParte
    //
    // Un formulario multipart envía cada campo (texto o archivo)
    // como una "parte" (Part). Para los campos de TEXTO, necesitamos
    // leer el contenido de esa parte como String.
    //
    // Recibe: el objeto request y el nombre del campo del formulario.
    // Devuelve: el valor del campo como String, o null si no existe.
    // ----------------------------------------------------------
    private String leerParte(HttpServletRequest request, String nombre) throws IOException, ServletException {
        // request.getPart(nombre) busca la parte del formulario con ese nombre.
        Part part = request.getPart(nombre);
        // Si la parte no existe, devolvemos null.
        if (part == null) return null;
        // part.getInputStream() abre un "stream" (flujo de datos) para leer el contenido.
        // readAllBytes() lee todos los bytes del stream de golpe.
        // new String(..., "UTF-8") convierte los bytes en texto usando codificación UTF-8.
        return new String(part.getInputStream().readAllBytes(), "UTF-8");
    }

    // ----------------------------------------------------------
    // MÉTODO: doPost
    //
    // Gestiona la creación de una nueva noticia.
    // El frontend (admin.js) envía los datos del formulario a esta URL.
    // ----------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Establecemos codificación para leer correctamente caracteres especiales.
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // -------------------------------------------------------
        // VERIFICACIÓN DE SEGURIDAD: ¿Está el admin logueado?
        //
        // Solo los administradores autenticados pueden crear noticias.
        // request.getSession(false) → obtiene la sesión existente sin crear una nueva.
        //   Si devuelve null, no hay sesión → el usuario no ha hecho login.
        //   El "false" es importante: getSession() sin argumento crea sesión si no existe,
        //   lo cual abriría un agujero de seguridad.
        //
        // Si la sesión existe, comprobamos que tenga el atributo "adminLogueado".
        // Si no tiene ese atributo, no está autenticado como admin.
        // -------------------------------------------------------
        if (request.getSession(false) == null || request.getSession(false).getAttribute("adminLogueado") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Código 401
            out.print("{\"status\":\"error\", \"message\":\"No tienes permisos para realizar esta acción\"}");
            out.flush();
            return; // "return" aquí corta la ejecución del método: no continúa.
        }

        // -------------------------------------------------------
        // BLOQUE TRY-CATCH PRINCIPAL
        //
        // Todo el proceso de creación está envuelto en un try-catch.
        // Si cualquier operación falla (leer campos, guardar archivo, BD...),
        // el catch captura el error y devuelve una respuesta de error al cliente.
        // -------------------------------------------------------
        try {
            // Leemos los campos de texto del formulario multipart.
            String titulo = leerParte(request, "titulo");
            String contenido = leerParte(request, "contenido");
            String autor = leerParte(request, "autor");

            // Leemos la parte de tipo ARCHIVO (la imagen).
            // A diferencia de los campos de texto, la imagen se lee con getPart() directamente
            // y se procesa con métodos específicos para archivos.
            Part filePart = request.getPart("imagen");
            String nombreImagenFinal = null; // Nombre definitivo del archivo en el servidor.

            // Comprobamos si el admin realmente subió una imagen
            // (que no sea null y que tenga tamaño > 0).
            if (filePart != null && filePart.getSize() > 0) {

                // Obtenemos el nombre original del archivo (ej: "foto-vacaciones.jpg").
                String nombreOriginal = filePart.getSubmittedFileName();
                // Extraemos la extensión (ej: ".jpg", ".png").
                // lastIndexOf(".") busca la última posición del punto en el nombre.
                // substring() corta el String desde esa posición hasta el final.
                String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));

                // Generamos un nombre único usando UUID para evitar que dos archivos
                // con el mismo nombre original se sobreescriban.
                // UUID.randomUUID() genera algo como: "a3f2c8d1-7e45-4b09-9f2a-..."
                nombreImagenFinal = UUID.randomUUID().toString() + extension;
                // Resultado ejemplo: "a3f2c8d1-7e45-4b09-9f2a-abc123.jpg"

                // -------------------------------------------------------
                // LÓGICA DE RUTA DE SUBIDA
                //
                // En Railway (producción), se monta un volumen persistente en /datos_persistentes.
                // Si guardamos archivos dentro del contenedor Docker sin un volumen,
                // se pierden al reiniciar el contenedor.
                //
                // En local, /datos_persistentes probablemente no exista o no tengamos permisos,
                // así que usamos la carpeta "uploads" dentro de la aplicación web como fallback.
                // -------------------------------------------------------
                String uploadPath = "/datos_persistentes"; // Ruta prioritaria (Railway)
                File uploadDir = new File(uploadPath);

                // Intentamos crear el directorio si no existe.
                if (!uploadDir.exists()) {
                    try {
                        uploadDir.mkdirs(); // mkdirs() crea también los directorios intermedios.
                    } catch (SecurityException ignored) {
                        // Si no tenemos permisos (ej. en local), ignoramos el error y probamos la ruta alternativa.
                    }
                }

                // Si /datos_persistentes no existe o no se puede escribir → usamos la ruta alternativa.
                if (!uploadDir.exists() || !uploadDir.canWrite()) {
                    // getServletContext().getRealPath("") devuelve la ruta absoluta en disco
                    // de la carpeta raíz de la aplicación web desplegada en Tomcat.
                    uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
                    uploadDir = new File(uploadPath);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs(); // Creamos la carpeta "uploads" si no existe.
                    }
                }

                // Guardamos el archivo en el directorio determinado.
                // filePart.write() escribe los bytes del archivo subido en la ruta indicada.
                // File.separator es "/" en Linux/Mac y "\" en Windows (multiplataforma).
                filePart.write(uploadPath + File.separator + nombreImagenFinal);
            }

            // Creamos el objeto Noticia con los datos leídos.
            // "nombreImagenFinal" puede ser null si no se subió imagen → el DAO lo gestiona.
            Noticia nuevaNoticia = new Noticia(titulo, contenido, nombreImagenFinal, autor);

            // Llamamos al DAO para insertar la noticia en la BD.
            CrearNoticiaDAO dao = new CrearNoticiaDAO();
            boolean guardadoExitoso = dao.crear(nuevaNoticia);

            // Respondemos según el resultado del DAO.
            if (guardadoExitoso) {
                out.print("{\"status\":\"success\", \"message\":\"Noticia publicada correctamente\"}");
            } else {
                // Error 500: el servidor falló al procesar la petición.
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\", \"message\":\"Error al guardar en la base de datos.\"}");
            }

        } catch (Exception e) {
            // Capturamos cualquier excepción no prevista.
            e.printStackTrace(); // Logueamos el error en el servidor.
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // e.getMessage() devuelve el mensaje de la excepción.
            out.print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        } finally {
            // "finally" se ejecuta SIEMPRE, haya error o no.
            // Aseguramos que el buffer de respuesta se envía al cliente.
            out.flush();
        }
    }
}