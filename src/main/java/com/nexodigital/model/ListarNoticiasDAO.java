// ============================================================
// PAQUETE: Capa MODEL del proyecto.
// ============================================================
package com.nexodigital.model;

import java.sql.*;
// ArrayList es una lista dinámica (como un array pero que crece sola).
// Aquí la usamos para almacenar todas las noticias que devuelva la BD.
import java.util.ArrayList;
// List es la interfaz (tipo genérico) de la que ArrayList es una implementación.
// Usar "List" como tipo de retorno es mejor práctica que usar "ArrayList" directamente,
// porque hace el código más flexible (podrías cambiar a otra implementación sin romper nada).
import java.util.List;

// ============================================================
// CLASE: ListarNoticiasDAO
//
// DAO responsable de LEER (READ) TODAS las noticias de la BD.
// Implementa parte de la "R" del CRUD (la parte de "listar todas").
//
// A diferencia de ObtenerNoticiaDAO (que busca una sola noticia por ID),
// este DAO devuelve una LISTA con TODAS las noticias existentes.
// ============================================================
public class ListarNoticiasDAO {

    // ----------------------------------------------------------
    // MÉTODO: listarTodas
    //
    // No recibe parámetros (no necesita saber nada para listar todo).
    // Devuelve "List<Noticia>": una lista de objetos Noticia.
    //   El "<Noticia>" se llama "genérico" (generic): indica que
    //   esta lista solo puede contener objetos de tipo Noticia.
    // ----------------------------------------------------------
    public List<Noticia> listarTodas() {

        // Creamos la lista vacía donde iremos guardando cada noticia.
        // ArrayList<>() → el "<>" vacío infiere el tipo Noticia automáticamente.
        List<Noticia> lista = new ArrayList<>();

        // SENTENCIA SQL SELECT
        //
        // SELECT * FROM tabla ORDER BY campo DESC
        //
        // SELECT * → selecciona TODAS las columnas de la tabla.
        // FROM noticias → de la tabla "noticias".
        // ORDER BY fecha_publicacion DESC → ordena los resultados por fecha
        //   de más reciente a más antigua (DESC = descendente).
        //   Así la noticia más nueva aparece primero.
        String sql = "SELECT * FROM noticias ORDER BY fecha_publicacion DESC";

        // Try-with-resources con TRES recursos:
        //   1. conn → la conexión a la base de datos
        //   2. pstmt → la sentencia SQL preparada
        //   3. rs → el ResultSet (el "cursor" que recorre los resultados)
        // Los tres se cierran automáticamente al salir del try.
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                // executeQuery() ejecuta el SELECT y devuelve un ResultSet.
                // Un ResultSet es como un "cursor" que apunta a las filas devueltas.
                // Inicialmente apunta ANTES de la primera fila.
                ResultSet rs = pstmt.executeQuery()) {

            // rs.next() avanza el cursor a la siguiente fila.
            // Devuelve true si hay una fila más, false si ya no hay más.
            // El while() itera sobre TODAS las filas del resultado.
            while (rs.next()) {

                // Para cada fila, creamos un nuevo objeto Noticia (usando el constructor vacío).
                Noticia n = new Noticia();

                // Leemos cada columna de la fila actual y la guardamos en el objeto Noticia.
                // rs.getInt("nombre_columna") → lee un entero de la columna indicada.
                // rs.getString("nombre_columna") → lee texto de la columna indicada.
                // El nombre debe coincidir EXACTAMENTE con el nombre de la columna en MySQL.
                n.setId(rs.getInt("id"));
                n.setTitulo(rs.getString("titulo"));
                n.setContenido(rs.getString("contenido_noticia")); // Columna se llama "contenido_noticia" en la BD
                n.setNombreImagen(rs.getString("nombre_archivo_imagen")); // Columna "nombre_archivo_imagen"
                n.setAutor(rs.getString("autor"));
                n.setFechaPublicacion(rs.getString("fecha_publicacion"));

                // Añadimos el objeto Noticia a la lista.
                lista.add(n);
            }
            // Cuando rs.next() devuelva false, el while termina y "lista" tiene todas las noticias.

        } catch (SQLException e) {
            // Si algo falla (conexión perdida, error SQL...),
            // imprimimos el error y devolvemos la lista vacía (no null).
            // Devolver una lista vacía es mejor que devolver null
            // porque el código que llama a este método no necesita
            // comprobar si el resultado es null antes de usarlo.
            e.printStackTrace();
        }

        // Devolvemos la lista (puede tener 0 o más noticias).
        return lista;
    }
}
