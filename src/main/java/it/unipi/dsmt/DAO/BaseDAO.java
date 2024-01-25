package it.unipi.dsmt.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BaseDAO {
    // Cambiare url quando sarà online
    private static final String URL = "jdbc:mysql://localhost:3306/intesa_vincente";
    // mettere proprie credenziali
    private static final String USERNAME = "root";

    private static final String PASSWORD = "studenti";
    private static Connection connection = null;

    public BaseDAO(){
        connection = getConnection();
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection(){
        try{
            if (connection == null || connection.isClosed()){
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        }catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error during the connection to the db", e);
        }
        return connection;
    }
}
