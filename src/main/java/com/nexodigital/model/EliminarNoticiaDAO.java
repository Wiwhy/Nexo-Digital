package com.nexodigital.model;

import java.sql.*;

public class EliminarNoticiaDAO {

    public boolean eliminar(int id) {
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
}
