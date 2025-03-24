package webSocketHomeAssistant;


import java.sql.*;

abstract class MariaDBConnection {

    protected Connection connection;

    public MariaDBConnection() {
        try {
            String url = "jdbc:mariadb://localhost:3306/yourDbName";
            String user = "inser_your_user";
            String password = "inser_your_password";
            Connection conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.err.println("❌ Errore SQL: " + e.getMessage());
        }
        System.out.println("✅ Connessione a MariaDB riuscita!");
    }
}
