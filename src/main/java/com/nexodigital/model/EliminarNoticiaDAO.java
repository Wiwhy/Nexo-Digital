// ============================================================
// PAQUETE: Capa MODEL del proyecto.
// ============================================================
package com.nexodigital.model;

import java.sql.*;

// ============================================================
// CLASE: EliminarNoticiaDAO
//
// DAO responsable de ELIMINAR (DELETE) una noticia de la base
// de datos. Implementa la "D" del CRUD.
//
// La operación es sencilla: recibe el ID (número único) de la
// noticia y ejecuta un DELETE en la base de datos.
// ============================================================
public class EliminarNoticiaDAO {

    // ----------------------------------------------------------
    // MÉTODO: eliminar
    //
    // Recibe "id" (el número identificador de la noticia a borrar).
    // Devuelve true si se borró correctamente, false si hubo error.
    // ----------------------------------------------------------
    public boolean eliminar(int id) {

        // SENTENCIA SQL DELETE
        //
        // DELETE FROM tabla WHERE condicion
        // Borra la fila de "noticias" cuyo campo "id" coincida con el valor recibido.
        // El "?" es el placeholder para el id → evita SQL Injection.
        //
        // IMPORTANTE: Sin el WHERE, borraríamos TODAS las noticias de golpe.
        // El WHERE id=? asegura que solo borramos la noticia específica que el admin quiso.
        String sql = "DELETE FROM noticias WHERE id = ?";

        // Try-with-resources: gestiona la conexión y el PreparedStatement
        // y los cierra automáticamente al salir del bloque.
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Asignamos el valor del id al primer "?" de la sentencia SQL.
            // setInt porque "id" es un número entero.
            pstmt.setInt(1, id);

            // executeUpdate() ejecuta el DELETE.
            // Devuelve el número de filas eliminadas.
            // Si eliminó 1 o más filas → devolvemos true (éxito).
            // Si eliminó 0 filas (el ID no existía) → devolvemos false.
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            // Imprimimos el error en los logs del servidor.
            e.printStackTrace();
            // Indicamos que la eliminación falló.
            return false;
        }
    }
}
