// ============================================================
// PAQUETE: Capa MODEL del proyecto.
// ============================================================
package com.nexodigital.model;

// Importamos todas las clases del paquete java.sql que necesitamos
// (Connection, PreparedStatement, SQLException, etc.)
import java.sql.*;

// ============================================================
// CLASE: CrearNoticiaDAO
//
// DAO responsable de INSERTAR (CREATE) una nueva noticia
// en la base de datos. Implementa la "C" del CRUD:
//   C → Create  (Crear)   → esta clase
//   R → Read    (Leer)    → ListarNoticiasDAO / ObtenerNoticiaDAO
//   U → Update  (Editar)  → ActualizarNoticiaDAO
//   D → Delete  (Borrar)  → EliminarNoticiaDAO
// ============================================================
public class CrearNoticiaDAO {

    // ----------------------------------------------------------
    // MÉTODO: crear
    //
    // Recibe un objeto Noticia con los datos de la nueva noticia
    // y los inserta como una nueva fila en la tabla "noticias".
    //
    // Devuelve true si el INSERT tuvo éxito, false si falló.
    // ----------------------------------------------------------
    public boolean crear(Noticia noticia) {

        // SENTENCIA SQL INSERT
        //
        // INSERT INTO tabla (columna1, columna2...) VALUES (?, ?, ...)
        // Inserta una nueva fila en la tabla "noticias".
        //
        // Columnas que insertamos:
        //   - titulo: el título de la noticia
        //   - contenido_noticia: el texto del artículo
        //   - nombre_archivo_imagen: el nombre del archivo de imagen subido
        //   - autor: el nombre del autor
        //   - fecha_publicacion: fecha y hora actual, generada por MySQL con NOW()
        //
        // NOTA: El campo "id" NO lo insertamos porque es AUTO_INCREMENT:
        // MySQL le asigna automáticamente el siguiente número disponible.
        //
        // Los "?" son placeholders para evitar SQL Injection.
        // NOW() es una función de MySQL que devuelve la fecha y hora actuales.
        String sql = "INSERT INTO noticias (titulo, contenido_noticia, nombre_archivo_imagen, autor, fecha_publicacion) VALUES (?, ?, ?, ?, NOW())";

        // Try-with-resources: abre la conexión y el PreparedStatement,
        // y los cierra automáticamente al finalizar (con éxito o con error).
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Asignamos valores a los placeholders "?" en orden de posición:
            pstmt.setString(1, noticia.getTitulo());         // 1er ? → título
            pstmt.setString(2, noticia.getContenido());      // 2do ? → contenido
            pstmt.setString(3, noticia.getNombreImagen());   // 3er ? → nombre archivo imagen
            pstmt.setString(4, noticia.getAutor());          // 4to ? → autor
            // El 5to "?" no existe porque el NOW() lo calcula MySQL directamente.

            // executeUpdate() ejecuta el INSERT.
            // Devuelve el número de filas insertadas (normalmente 1).
            // Si devuelve > 0, el INSERT tuvo éxito → retornamos true.
            // Si devuelve 0, no se insertó nada → retornamos false.
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            // Si ocurre un error (conexión caída, violación de restricción, etc.)
            // imprimimos el error en los logs del servidor para poder diagnosticarlo.
            e.printStackTrace();
            // Indicamos que la creación falló.
            return false;
        }
    }
}
