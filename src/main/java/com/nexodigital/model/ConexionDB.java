package com.nexodigital.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    public static Connection obtenerConexion() throws SQLException {
        // Soporte para los nombres de variables que inyecta Railway
        String host = getEnv("DB_HOST", getEnv("MYSQLHOST", "mysql_bd_noticias"));
        String port = getEnv("DB_PORT", getEnv("MYSQLPORT", "3306"));
        String database = getEnv("DB_NAME", getEnv("MYSQLDATABASE", "nexo_digital")); // ✅ CAMBIO: "railway" →
                                                                                      // "nexo_digital"
        String user = getEnv("DB_USER", getEnv("MYSQLUSER", "root"));
        String password = getEnv("DB_PASSWORD", getEnv("MYSQLPASSWORD", "root"));
        String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port, database);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Error al cargar el driver de MySQL", e);
        }
        return DriverManager.getConnection(url, user, password);
    }
}