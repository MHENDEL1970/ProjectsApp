package projects.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import projects.exception.DbException;

public class DbConnection {
    private static final String SCHEMA = "projects";
    private static final String USER = "projects";
    private static final String PASSWORD = "projects";
    private static final String HOST = "localhost";
    private static final int PORT = 3306;

    public static Connection getConnection() {
        String uri = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC",
                                   HOST, PORT, SCHEMA);
        try {
            // Load MySQL Driver explicitly
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(uri, USER, PASSWORD);
            System.out.println("Successfully obtained connection!");
            return conn;
            
        } catch (SQLException e) {
            throw new DbException("Unable to get connection at " + uri, e);
            
        } catch (ClassNotFoundException e) {
            throw new DbException("MySQL JDBC Driver not found.", e);
        }
    }
}