// ============================================================
// PAQUETE: Capa MODEL del proyecto.
// ============================================================
package com.nexodigital.model;

// Importamos todo el paquete java.sql (Connection, PreparedStatement, SQLException, etc.)
// El asterisco "*" es un comodín que importa todas las clases de ese paquete de una vez.
import java.sql.*;

// ============================================================
// CLASE: ActualizarNoticiaDAO
//
// DAO = Data Access Object (Objeto de Acceso a Datos).
// Es una clase cuya ÚNICA responsabilidad es hablar con la
// base de datos para una operación concreta. En este caso:
// ACTUALIZAR (UPDATE) una noticia existente.
//
// Siguiendo el patrón MVC:
//   - El CONTROLLER recibe la petición HTTP del navegador
//   - El CONTROLLER crea un objeto Noticia con los nuevos datos
//   - El CONTROLLER crea este DAO y llama a su método actualizar()
//   - Este DAO ejecuta el SQL contra la base de datos
//   - Este DAO devuelve true (éxito) o false (fracaso)
//   - El CONTROLLER le dice al navegador qué pasó (JSON)
// ============================================================
public class ActualizarNoticiaDAO {

    // ----------------------------------------------------------
    // MÉTODO: actualizar
    //
    // Recibe un objeto "Noticia" con todos los datos nuevos
    // que hay que guardar en la base de datos.
    //
    // Devuelve "boolean": true si la actualización tuvo éxito,
    // false si algo falló.
    // ----------------------------------------------------------
    public boolean actualizar(Noticia noticia) {

        // Declaramos la variable "sql" que contendrá la sentencia SQL.
        // La declaramos aquí (fuera del if/else) para poder usarla después
        // en el try/catch que viene a continuación.
        String sql;

        // LÓGICA CONDICIONAL: ¿Trae imagen nueva o no?
        //
        // Cuando el admin edita una noticia, puede o no subir una imagen nueva.
        // Si sube imagen → queremos actualizar TAMBIÉN el campo imagen en la BD.
        // Si NO sube imagen → NO queremos cambiar la imagen existente en la BD.
        //
        // Por eso tenemos DOS sentencias SQL diferentes:
        if (noticia.getNombreImagen() != null) {
            // El admin SÍ subió una imagen nueva.
            // Actualizamos los 4 campos: título, contenido, imagen y autor.
            // Los signos "?" son PLACEHOLDERS (marcadores de posición).
            // Los valores reales se asignan después con pstmt.setString().
            // Esto evita inyección SQL (SQL Injection): un tipo de ataque
            // donde alguien mete código SQL malicioso en un campo de texto.
            sql = "UPDATE noticias SET titulo=?, contenido_noticia=?, nombre_archivo_imagen=?, autor=? WHERE id=?";
        } else {
            // El admin NO subió imagen nueva.
            // Actualizamos solo 3 campos: título, contenido y autor.
            // El campo imagen de la BD quedará igual que antes.
            sql = "UPDATE noticias SET titulo=?, contenido_noticia=?, autor=? WHERE id=?";
        }

        // TRY-WITH-RESOURCES
        //
        // Esta es una sintaxis especial de Java que garantiza que los
        // recursos (la conexión a la BD y el PreparedStatement) se cierran
        // automáticamente cuando el bloque try termina, haya error o no.
        // Esto evita "fugas de conexión" (dejar conexiones abiertas que consumen memoria).
        //
        // ConexionDB.obtenerConexion() → abre el "cable" hacia MySQL.
        // conn.prepareStatement(sql)  → prepara la sentencia SQL para ejecutarla.
        //   Un PreparedStatement es una sentencia precompilada que es más
        //   eficiente y segura que construir el SQL concatenando Strings.
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // ASIGNAMOS VALORES A LOS PLACEHOLDERS "?"
            //
            // Cada "?" tiene un número de posición (1, 2, 3...).
            // pstmt.setString(1, valor) reemplaza el primer "?" con ese valor.
            // pstmt.setInt(1, valor) hace lo mismo pero para números enteros.

            // Placeholder 1 → titulo
            pstmt.setString(1, noticia.getTitulo());
            // Placeholder 2 → contenido_noticia
            pstmt.setString(2, noticia.getContenido());

            // La posición del siguiente placeholder depende de si hay imagen o no
            if (noticia.getNombreImagen() != null) {
                // Con imagen: placeholder 3 → imagen, 4 → autor, 5 → id del WHERE
                pstmt.setString(3, noticia.getNombreImagen());
                pstmt.setString(4, noticia.getAutor());
                pstmt.setInt(5, noticia.getId()); // El WHERE id=? necesita el ID para saber qué fila actualizar
            } else {
                // Sin imagen: placeholder 3 → autor, 4 → id del WHERE
                pstmt.setString(3, noticia.getAutor());
                pstmt.setInt(4, noticia.getId());
            }

            // EJECUTAMOS LA SENTENCIA SQL UPDATE.
            // executeUpdate() devuelve un entero: el número de filas afectadas.
            // Si actualizó al menos 1 fila (> 0), devolvemos true (éxito).
            // Si actualizó 0 filas (el ID no existía en la BD), devolvemos false.
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            // Si ocurre cualquier error con la base de datos:
            // e.printStackTrace() imprime el error completo en los logs del servidor
            // para que podamos ver qué salió mal.
            e.printStackTrace();
            // Devolvemos false para indicar que la actualización NO tuvo éxito.
            return false;
        }
        // El try-with-resources cierra automáticamente conn y pstmt aquí.
    }
}
