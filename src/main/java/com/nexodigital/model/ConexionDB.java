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

        String host     = getEnv("DB_HOST", getEnv("MYSQL_HOST", getEnv("MYSQLHOST", "mysql_bd_noticias")));
        String port     = getEnv("DB_PORT", getEnv("MYSQL_PORT", getEnv("MYSQLPORT", "3306")));
        String database = getEnv("DB_NAME", getEnv("MYSQL_DATABASE", getEnv("MYSQLDATABASE", "railway")));
        String user     = getEnv("DB_USER", getEnv("MYSQL_USER", getEnv("MYSQLUSER", "root")));
        String password = getEnv("DB_PASSWORD", getEnv("MYSQL_PASSWORD", getEnv("MYSQLPASSWORD", "root")));

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