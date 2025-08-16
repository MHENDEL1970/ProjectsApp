package projects.util;

public class TestDbConnection {
    public static void main(String[] args) {
        try {
            DbConnection.getConnection();
            System.out.println("Database connection test successful!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}