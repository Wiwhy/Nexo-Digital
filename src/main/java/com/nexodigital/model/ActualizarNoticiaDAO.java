package com.nexodigital.model;

import java.sql.*;

public class ActualizarNoticiaDAO {

    public boolean actualizar(Noticia noticia) {
        String sql;
        if (noticia.getNombreImagen() != null) {
            sql = "UPDATE noticias SET titulo=?, contenido_noticia=?, nombre_archivo_imagen=?, autor=? WHERE id=?";
        } else {
            sql = "UPDATE noticias SET titulo=?, contenido_noticia=?, autor=? WHERE id=?";
        }

        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, noticia.getTitulo());
            pstmt.setString(2, noticia.getContenido());

            if (noticia.getNombreImagen() != null) {
                pstmt.setString(3, noticia.getNombreImagen());
                pstmt.setString(4, noticia.getAutor());
                pstmt.setInt(5, noticia.getId());
            } else {
                pstmt.setString(3, noticia.getAutor());
                pstmt.setInt(4, noticia.getId());
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
