// ============================================================
// PAQUETE: Esta clase pertenece a la capa MODEL.
// ============================================================
package com.nexodigital.model;

// ============================================================
// IMPORTS: "import" le dice a Java qué clases externas necesita
// esta clase para funcionar. Es como añadir librerías.
// ============================================================

// "Connection" representa UNA conexión activa con la base de datos.
// Es como un "cable" abierto hacia MySQL.
import java.sql.Connection;

// "DriverManager" es el "gestor de drivers": es el encargado de
// crear la conexión a la base de datos buscando el driver adecuado.
import java.sql.DriverManager;

// "SQLException" es el tipo de error (excepción) que lanza Java
// cuando algo falla con la base de datos (credenciales incorrectas,
// BD no disponible, etc.). El "throws" más abajo obliga a quien
// llame a este método a manejar este posible error.
import java.sql.SQLException;

// ============================================================
// CLASE: ConexionDB
//
// Esta clase tiene UNA sola responsabilidad: crear y devolver
// una conexión a la base de datos MySQL.
//
// Es una clase de UTILIDAD: no tiene atributos de instancia,
// solo métodos estáticos (que se pueden llamar sin crear un objeto).
//
// TODOS los DAO del proyecto (ActualizarNoticiaDAO, CrearNoticiaDAO...)
// llaman a esta clase para obtener la conexión.
// ============================================================
public class ConexionDB {

    // ----------------------------------------------------------
    // MÉTODO PRIVADO DE AYUDA: getEnv
    //
    // "private" → solo esta clase puede usarlo.
    // "static" → no necesita crear un objeto ConexionDB para llamarlo.
    // "String" → devuelve texto.
    //
    // Este método lee una VARIABLE DE ENTORNO del sistema operativo
    // (o del contenedor Docker). Las variables de entorno son
    // configuraciones externas que no van en el código, sino que
    // se pasan desde fuera (en docker-compose.yml, por ejemplo).
    //
    // Si la variable existe y no está vacía → la devuelve.
    // Si no existe o está vacía → devuelve el "defaultValue" (valor por defecto).
    //
    // Esto es muy útil para que el mismo código funcione tanto
    // en tu ordenador local como en un servidor en la nube (Railway).
    // ----------------------------------------------------------
    private static String getEnv(String name, String defaultValue) {
        // System.getenv(name) lee la variable de entorno del sistema.
        // Devuelve null si la variable no existe.
        String value = System.getenv(name);

        // Operador ternario: "condición ? valorSiTrue : valorSiFalse"
        // Si value no es null Y no está en blanco → devuelve value
        // Si value es null O está en blanco → devuelve defaultValue
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    // ----------------------------------------------------------
    // MÉTODO PRINCIPAL: obtenerConexion
    //
    // "public" → cualquiera puede llamarlo.
    // "static" → se llama como ConexionDB.obtenerConexion() sin crear objeto.
    // "Connection" → devuelve un objeto de tipo Connection (la conexión a MySQL).
    // "throws SQLException" → advierte que puede lanzar un error de BD,
    //   y quien llame a este método deberá manejarlo con try/catch.
    //
    // Este es el método que usan todos los DAOs. Cada vez que
    // un DAO necesita hablar con la BD, llama a este método.
    // ----------------------------------------------------------
    public static Connection obtenerConexion() throws SQLException {

        // Leemos el HOST (dirección) del servidor MySQL.
        // Soporta DB_HOST (custom), MYSQL_HOST (Railway actual) y MYSQLHOST (Railway Legacy).
        String host = getEnv("DB_HOST", getEnv("MYSQL_HOST", getEnv("MYSQLHOST", "mysql_bd_noticias")));

        // Leemos el PUERTO de MySQL.
        String port = getEnv("DB_PORT", getEnv("MYSQL_PORT", getEnv("MYSQLPORT", "3306")));

        // Leemos el NOMBRE de la base de datos.
        // En Railway la base de datos suele llamarse "railway" por defecto. En local "nexo_digital".
        String database = getEnv("DB_NAME", getEnv("MYSQL_DATABASE", getEnv("MYSQLDATABASE", "railway")));

        // Leemos el USUARIO de MySQL.
        String user = getEnv("DB_USER", getEnv("MYSQL_USER", getEnv("MYSQLUSER", "root")));

        // Leemos la CONTRASEÑA de MySQL.
        String password = getEnv("DB_PASSWORD", getEnv("MYSQL_PASSWORD", getEnv("MYSQLPASSWORD", "root")));

        // Construimos la URL de conexión JDBC (Java DataBase Connectivity).
        // El formato es: jdbc:mysql://HOST:PUERTO/BASE_DE_DATOS?opciones
        // String.format es como un printf: %s se sustituye por las variables en orden.
        // Parámetros extra en la URL:
        //   - useSSL=false: no usamos cifrado SSL (simplifica la conexión en desarrollo)
        //   - allowPublicKeyRetrieval=true: necesario para algunas versiones de MySQL 8
        //   - serverTimezone=UTC: establece la zona horaria del servidor para evitar errores
        String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port, database);

        // Antes de conectar, debemos "registrar" el driver de MySQL.
        // Un driver es el software intermediario que sabe cómo hablar con MySQL.
        // Class.forName carga la clase del driver en memoria.
        // Si la clase no se encuentra (no está en el pom.xml), lanza ClassNotFoundException.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Si el driver no está disponible, convertimos el error en SQLException
            // para que quien llame a este método lo gestione uniformemente.
            throw new SQLException("Error al cargar el driver de MySQL", e);
        }

        // DriverManager.getConnection crea y devuelve la conexión real a MySQL.
        // Recibe la URL, el usuario y la contraseña.
        // Si hay algún problema (credenciales incorrectas, MySQL no responde...) lanza SQLException.
        return DriverManager.getConnection(url, user, password);
    }
}