// ============================================================
// PAQUETE: Capa CONTROLLER del proyecto.
// ============================================================
package com.nexodigital.controller;

// Importamos el DAO que necesitamos para hablar con la BD.
import com.nexodigital.model.ListarNoticiasDAO;
// Importamos la clase Noticia (el modelo de datos).
import com.nexodigital.model.Noticia;

// Jakarta Servlet API: el estándar para crear Servlets en Java.
// Un Servlet es una clase Java que recibe peticiones HTTP (como GET o POST)
// y envía una respuesta HTTP de vuelta al navegador.
import jakarta.servlet.ServletException;
// @WebServlet es una anotación (como una etiqueta) que le dice a Tomcat
// que esta clase es un Servlet y en qué URL debe responder.
import jakarta.servlet.annotation.WebServlet;
// HttpServlet es la clase base de la que heredamos.
// Ya tiene implementada toda la lógica de HTTP.
import jakarta.servlet.http.HttpServlet;
// HttpServletRequest representa la petición HTTP recibida.
// Contiene los parámetros, cabeceras, cookies, sesión, etc.
import jakarta.servlet.http.HttpServletRequest;
// HttpServletResponse representa la respuesta HTTP que vamos a enviar.
// Podemos configurar el código de estado, el tipo de contenido, y escribir el cuerpo.
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
// PrintWriter permite escribir texto en la respuesta HTTP (el "cuerpo" de la respuesta).
import java.io.PrintWriter;
import java.util.List;

// ============================================================
// ANOTACIÓN @WebServlet
//
// Le dice a Tomcat: "cuando el navegador haga una petición a
// /api/noticias/listar, ejecuta este Servlet".
//
// Tomcat es el servidor web que aloja nuestra aplicación Java.
// Al arrancar, Tomcat lee todas las anotaciones @WebServlet y
// crea un "mapa de rutas" para saber qué clase usar para cada URL.
// ============================================================
@WebServlet("/api/noticias/listar")
// "extends HttpServlet" → heredamos de HttpServlet, lo que nos da
// la capacidad de procesar peticiones HTTP sin escribir todo desde cero.
public class ListarNoticiasServlet extends HttpServlet {

    // ----------------------------------------------------------
    // MÉTODO: doGet
    //
    // Este método se ejecuta cuando el navegador hace una petición GET
    // a la URL "/api/noticias/listar".
    //
    // GET es el método HTTP para LEER datos (el navegador "pide" información).
    // Se opone a POST (que envía datos al servidor).
    //
    // @Override indica que estamos reescribiendo (sobreescribiendo) el método
    // doGet() que viene de HttpServlet. La clase padre no hace nada útil;
    // nosotros le damos la lógica real.
    //
    // "throws ServletException, IOException" → este método puede lanzar
    // estos dos tipos de errores, y la responsabilidad de gestionarlos
    // la dejamos al contenedor (Tomcat).
    // ----------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configuramos la RESPUESTA:
        // Le decimos al navegador que vamos a enviar datos en formato JSON.
        // "application/json" es el tipo MIME (Media Type) estándar para JSON.
        response.setContentType("application/json");
        // Codificación de caracteres: UTF-8 para soportar tildes, ñ, emojis, etc.
        response.setCharacterEncoding("UTF-8");
        // Obtenemos el escritor para poder escribir la respuesta texto.
        PrintWriter out = response.getWriter();

        // Creamos una instancia del DAO y le pedimos todas las noticias.
        ListarNoticiasDAO dao = new ListarNoticiasDAO();
        // Llamamos al método del DAO → esto ejecuta el SELECT en la BD.
        // "noticias" es una lista de objetos Noticia (puede estar vacía pero nunca null).
        List<Noticia> noticias = dao.listarTodas();

        // -------------------------------------------------------
        // CONSTRUIMOS EL JSON MANUALMENTE
        //
        // JSON (JavaScript Object Notation) es el formato de datos
        // que usamos para comunicar el backend con el frontend.
        // Ejemplo de JSON que generamos:
        // [
        //   {"id":1, "titulo":"Noticia 1", "contenido":"...", ...},
        //   {"id":2, "titulo":"Noticia 2", "contenido":"...", ...}
        // ]
        //
        // Un array JSON empieza con "[" y termina con "]".
        // Cada objeto JSON está entre "{" y "}".
        // Los pares clave-valor van: "clave":"valor" o "clave":numero
        //
        // StringBuilder es más eficiente que concatenar Strings con "+"
        // porque internamente no crea objetos nuevos en cada concatenación.
        // -------------------------------------------------------
        StringBuilder json = new StringBuilder("["); // Empezamos el array JSON

        // Recorremos la lista con un bucle for clásico (con índice "i").
        // Necesitamos el índice para saber si estamos en el último elemento
        // y así NO poner la coma final (que haría el JSON inválido).
        for (int i = 0; i < noticias.size(); i++) {
            Noticia n = noticias.get(i); // Obtenemos la noticia en la posición i

            // Empezamos el objeto JSON para esta noticia
            json.append("{");
            // "id" es un número → no lleva comillas alrededor del valor
            json.append("\"id\":").append(n.getId()).append(",");
            // Los demás campos son texto → van entre comillas escapadas con \"
            // Llamamos a escaparJson() para que los caracteres especiales no rompan el JSON.
            json.append("\"titulo\":\"").append(escaparJson(n.getTitulo())).append("\",");
            json.append("\"contenido\":\"").append(escaparJson(n.getContenido())).append("\",");
            json.append("\"autor\":\"").append(escaparJson(n.getAutor())).append("\",");
            json.append("\"fechaPublicacion\":\"").append(escaparJson(n.getFechaPublicacion())).append("\",");

            // La imagen puede ser null (si la noticia no tiene imagen).
            // En JSON, null (sin comillas) es el valor nulo estándar.
            if (n.getNombreImagen() != null) {
                json.append("\"nombreImagen\":\"").append(escaparJson(n.getNombreImagen())).append("\"");
            } else {
                json.append("\"nombreImagen\":null");
            }
            json.append("}"); // Cerramos el objeto JSON de esta noticia

            // Si NO es el último elemento, añadimos una coma separadora.
            // El último elemento NO lleva coma (el formato JSON lo requiere así).
            if (i < noticias.size() - 1) {
                json.append(",");
            }
        }
        json.append("]"); // Cerramos el array JSON

        // Escribimos el JSON completo en la respuesta HTTP.
        out.print(json.toString());
        // flush() "vacía el buffer": fuerza que los datos se envíen al cliente ahora.
        out.flush();
    }

    // ----------------------------------------------------------
    // MÉTODO PRIVADO: escaparJson
    //
    // El JSON tiene caracteres reservados que necesitan "escaparse"
    // (ponerse con una barra invertida delante) para no romper
    // la estructura del JSON.
    //
    // Por ejemplo: si el título contiene comillas (") o saltos de línea,
    // sin escapar romperían el JSON. Así que los convertimos en:
    //   "  →  \"  (comilla escapada)
    //   \  →  \\  (barra invertida escapada)
    //   \n →  \\n (salto de línea escapado)
    //   \r →  \\r (retorno de carro escapado)
    //   \t →  \\t (tabulador escapado)
    // ----------------------------------------------------------
    private String escaparJson(String s) {
        // Si el String es null, devolvemos cadena vacía en lugar de null
        // para evitar NullPointerException al llamar a .replace().
        if (s == null) return "";
        // Encadenamos varios replace() uno tras otro.
        // El orden importa: la barra invertida debe escaparse PRIMERO
        // para no re-escapar las barras que añadamos después.
        return s.replace("\\", "\\\\")  // \ → \\
                .replace("\"", "\\\"")  // " → \"
                .replace("\n", "\\n")   // salto de línea → \n
                .replace("\r", "\\r")   // retorno de carro → \r
                .replace("\t", "\\t");  // tabulador → \t
    }
}