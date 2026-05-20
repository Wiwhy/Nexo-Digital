// ============================================================
// PAQUETE: Capa MODEL del proyecto.
// ============================================================
package com.nexodigital.model;

import java.sql.*;

// ============================================================
// CLASE: ObtenerNoticiaDAO
//
// DAO responsable de LEER (READ) UNA sola noticia específica,
// buscándola por su ID. Implementa la otra parte de la "R" del CRUD.
//
// Diferencia con ListarNoticiasDAO:
//   - ListarNoticiasDAO → devuelve TODAS las noticias (List<Noticia>)
//   - ObtenerNoticiaDAO → devuelve UNA noticia por ID (Noticia)
//
// Se usa cuando el usuario hace clic en una noticia y quiere
// ver el artículo completo (página noticia.html).
// ============================================================
public class ObtenerNoticiaDAO {

    // ----------------------------------------------------------
    // MÉTODO: obtenerPorId
    //
    // Recibe el "id" (número entero) de la noticia a buscar.
    // Devuelve un objeto Noticia si la encontró, o NULL si no existe.
    //
    // IMPORTANTE: "null" en Java significa "ausencia de valor".
    // El código que llama a este método DEBE comprobar si el
    // resultado es null antes de usarlo (para no tener NullPointerException).
    // ----------------------------------------------------------
    public Noticia obtenerPorId(int id) {

        // Empezamos con null: si no encontramos la noticia, devolveremos null.
        Noticia n = null;

        // SENTENCIA SQL SELECT con WHERE
        //
        // SELECT * FROM noticias WHERE id = ?
        // Busca la fila cuyo campo "id" coincida con el valor que pasemos.
        // Solo puede devolver 0 o 1 fila (porque "id" es PRIMARY KEY = único).
        String sql = "SELECT * FROM noticias WHERE id = ?";

        // Try-with-resources: gestiona conexión y PreparedStatement.
        // Nótese que el ResultSet se abre dentro del try, no en la cabecera,
        // porque primero necesitamos asignar el parámetro "id" antes de ejecutar.
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Asignamos el valor del id al primer "?" de la sentencia.
            pstmt.setInt(1, id);

            // Ejecutamos el SELECT y obtenemos el ResultSet.
            // Este ResultSet también se abre en un try-with-resources anidado
            // para asegurar que se cierre correctamente.
            try (ResultSet rs = pstmt.executeQuery()) {

                // rs.next() avanza al primer resultado.
                // Si devuelve true → encontramos la noticia.
                // Si devuelve false → no existe ninguna noticia con ese ID.
                if (rs.next()) {
                    // Creamos el objeto Noticia y lo rellenamos con los datos de la BD.
                    n = new Noticia();
                    n.setId(rs.getInt("id"));
                    n.setTitulo(rs.getString("titulo"));
                    n.setContenido(rs.getString("contenido_noticia"));
                    n.setNombreImagen(rs.getString("nombre_archivo_imagen"));
                    n.setAutor(rs.getString("autor"));
                    n.setFechaPublicacion(rs.getString("fecha_publicacion"));
                    // No llamamos a rs.next() de nuevo porque solo esperamos 1 resultado.
                }
            }
            // El ResultSet "rs" se cierra automáticamente aquí.

        } catch (SQLException e) {
            // Si hay error de BD, imprimimos el error en los logs.
            // "n" sigue siendo null, así que el método devolverá null.
            e.printStackTrace();
        }
        // El PreparedStatement y la Connection se cierran automáticamente aquí.

        // Devolvemos el objeto Noticia (con datos) o null (si no se encontró).
        return n;
    }
}
