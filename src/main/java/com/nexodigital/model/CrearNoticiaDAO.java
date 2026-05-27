package com.nexodigital.model;

import java.sql.*;

public class CrearNoticiaDAO {

    public boolean crear(Noticia noticia) {

        String sql = "INSERT INTO noticias (titulo, contenido_noticia, nombre_archivo_imagen, autor, fecha_publicacion) VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, noticia.getTitulo());
            pstmt.setString(2, noticia.getContenido());
            pstmt.setString(3, noticia.getNombreImagen());
            pstmt.setString(4, noticia.getAutor());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
