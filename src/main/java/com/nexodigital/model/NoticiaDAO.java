package com.nexodigital.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticiaDAO {

    public boolean crearNoticia(Noticia noticia) {
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

    public List<Noticia> obtenerTodas() {
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

    public boolean eliminarNoticia(int id) {
        String sql = "DELETE FROM noticias WHERE id = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarNoticia(Noticia noticia) {
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