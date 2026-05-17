package com.nexodigital.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListarNoticiasDAO {

    public List<Noticia> listarTodas() {
        List<Noticia> lista = new ArrayList<>();
        String sql = "SELECT * FROM noticias ORDER BY fecha_publicacion DESC";
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Noticia n = new Noticia();
                n.setId(rs.getInt("id"));
                n.setTitulo(rs.getString("titulo"));
                n.setContenido(rs.getString("contenido_noticia"));
                n.setNombreImagen(rs.getString("nombre_archivo_imagen"));
                n.setAutor(rs.getString("autor"));
                n.setFechaPublicacion(rs.getString("fecha_publicacion"));
                n.setFechaRegistro(rs.getString("fecha_registro"));
                lista.add(n);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
