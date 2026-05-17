package com.nexodigital.model;

import java.sql.*;

public class ObtenerNoticiaDAO {

    public Noticia obtenerPorId(int id) {
        Noticia n = null;
        String sql = "SELECT * FROM noticias WHERE id = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    n = new Noticia();
                    n.setId(rs.getInt("id"));
                    n.setTitulo(rs.getString("titulo"));
                    n.setContenido(rs.getString("contenido_noticia"));
                    n.setNombreImagen(rs.getString("nombre_archivo_imagen"));
                    n.setAutor(rs.getString("autor"));
                    n.setFechaPublicacion(rs.getString("fecha_publicacion"));
                    n.setFechaRegistro(rs.getString("fecha_registro"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n;
    }
}
