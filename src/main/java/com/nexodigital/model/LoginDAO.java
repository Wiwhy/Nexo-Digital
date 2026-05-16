package com.nexodigital.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDAO {

    public boolean autenticar(String usuario, String password) {
        String sql = "SELECT * FROM administradores WHERE usuario = ? AND password_hash = ?";

        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);
            pstmt.setString(2, password); // En un proyecto real, las contraseñas estarían encriptadas

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Devuelve true si encuentra una fila que coincida
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}