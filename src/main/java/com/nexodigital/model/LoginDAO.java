// ============================================================
// PAQUETE: Capa MODEL del proyecto.
// ============================================================
package com.nexodigital.model;

// Importamos solo las clases específicas que necesitamos (en lugar del comodín *).
// Esta es una buena práctica: importar solo lo necesario deja claro qué se usa.
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// ============================================================
// CLASE: LoginDAO
//
// DAO responsable de VERIFICAR las credenciales del administrador.
// NO forma parte del CRUD de noticias; es una operación de autenticación.
//
// Recibe usuario y contraseña, y comprueba si existe algún registro
// en la tabla "administradores" que coincida.
//
// IMPORTANTE PARA PRODUCCIÓN REAL: En un sistema real, las
// contraseñas NUNCA se guardan en texto plano. Se guardan como
// HASH (una transformación irreversible, ej. con bcrypt o SHA-256).
// Al comprobar, se hashea la contraseña ingresada y se compara
// el hash, no el texto original. En este proyecto es texto plano
// por simplicidad académica.
// ============================================================
public class LoginDAO {

    // ----------------------------------------------------------
    // MÉTODO: autenticar
    //
    // Recibe el "usuario" y "password" que escribió en el formulario.
    // Devuelve true si las credenciales son correctas, false si no.
    // ----------------------------------------------------------
    public boolean autenticar(String usuario, String password) {

        // SENTENCIA SQL SELECT con doble condición
        //
        // Busca en la tabla "administradores" una fila donde
        // el campo "usuario" coincida Y el campo "password_hash" también coincida.
        // Si existe esa fila → el usuario y contraseña son correctos.
        // Si no existe → credenciales incorrectas.
        //
        // "AND" en SQL requiere que AMBAS condiciones sean verdaderas.
        // Los "?" son placeholders para evitar SQL Injection.
        String sql = "SELECT * FROM administradores WHERE usuario = ? AND password_hash = ?";

        // Try-with-resources: gestiona conexión y PreparedStatement.
        try (Connection conn = ConexionDB.obtenerConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Asignamos el usuario al primer "?" y la contraseña al segundo "?".
            pstmt.setString(1, usuario);
            pstmt.setString(2, password); // En un proyecto real, aquí iría el hash de la contraseña.

            // Ejecutamos el SELECT y obtenemos el ResultSet (resultados).
            // El ResultSet también se gestiona con try-with-resources para cerrarlo al final.
            try (ResultSet rs = pstmt.executeQuery()) {

                // rs.next() devuelve true si hay AL MENOS una fila en el resultado.
                // Si hay una fila → las credenciales coincidieron → autenticación correcta.
                // Si no hay fila → ningún administrador tiene esas credenciales → false.
                //
                // Usamos directamente el valor de retorno de rs.next() como resultado del método.
                // Es equivalente a: if(rs.next()) { return true; } else { return false; }
                return rs.next();
            }

        } catch (SQLException e) {
            // Si hay error de BD, lo mostramos en los logs.
            e.printStackTrace();
            // Devolvemos false por seguridad: en caso de duda, no autenticamos.
            return false;
        }
    }
}