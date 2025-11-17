package biblioteca.data.db;

import biblioteca.data.db.ConfigBD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Clase de conexión centralizada para la base de datos MySQL.
public class ConexionBD {

    public static Connection conexion;

    //Devuelve una conexión válida a la base de datos. Si no existe o está cerrada, la crea.
    public static Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                Class.forName(ConfigBD.DRIVER);
                conexion = DriverManager.getConnection(
                        ConfigBD.URL,
                        ConfigBD.USUARIO,
                        ConfigBD.CONTRASENA
                );
                System.out.println("Conexión a la base de datos establecida correctamente.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Error: no se encontró el driver JDBC (" + ConfigBD.DRIVER + ")", e);
            } catch (SQLException e) {
                throw new SQLException("Error al conectar a la base de datos: " + e.getMessage(), e);
            }
        }
        return conexion;
    }

    //Cierra la conexión activa si existe.
    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                    System.out.println("Conexión a la base de datos cerrada correctamente.");
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}