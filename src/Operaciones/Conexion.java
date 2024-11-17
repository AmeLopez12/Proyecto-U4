package Operaciones;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private Connection con;

    public Connection getConnection() {
        try {
            // Cambia "nombre_base_datos" por el nombre de tu base de datos
            String url = "jdbc:mysql://127.0.0.1:3306/sistemadeventa?useTimezone=true&serverTimezone=America/Mexico_City";
            String user = "root";
            String password = "*AC1264LM*";

            con = DriverManager.getConnection(url, user, password);
            System.out.println("Conexión exitosa a la base de datos.");
            return con;
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
        return null;
    }
}
