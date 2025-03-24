package webSocketHomeAssistant;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class SSHJDBCConnectionWithKey {
    // Configurazione SSH e database
    private static final String SSH_HOST = "yourSSH_Host";
    private static final int SSH_PORT = 22;
    private static final String SSH_USER = "insert_user";
    private static final String PRIVATE_KEY_PATH = "insert_path_key";
    private static final int LOCAL_PORT = 3307;
    private static final int DB_PORT = 3306;
    private static final String DB_NAME = "insert_db_name";
    private static final String DB_USER = "insert_db_user";
    private static final String DB_PASSWORD = "insert_db_password";

    protected Session sshSession;
    protected Connection connection;

    private static Session createSSHTunnel() throws Exception {
        JSch jsch = new JSch();
        jsch.addIdentity(PRIVATE_KEY_PATH);
        Session session = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        System.out.println("✅ Connessione SSH riuscita!");
        session.setPortForwardingL(LOCAL_PORT, "localhost", DB_PORT);
        System.out.println("✅ Tunnel SSH creato su porta locale: " + LOCAL_PORT);
        return session;
    }

    private static Connection createDBConnection() throws SQLException {
        String jdbcUrl = "jdbc:mariadb://127.0.0.1:" + LOCAL_PORT + "/" + DB_NAME;
        return DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
    }

    public void startConnection() {
        try {
            connection = createDBConnection();
            if (connection != null) {
                System.out.println("✅ Connessione al database riuscita!");
            }
        } catch (Exception e) {
            System.err.println("Errore " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (Exception e) {
            System.err.println("Errore " + e.getMessage());
        }
    }

    public void startSshConnection() {
        try {
            sshSession = createSSHTunnel();
        } catch (Exception e) {
            System.err.println("Errore " + e.getMessage());
        }
    }

    public void disconnectSshConnection() {
        try {
            sshSession.disconnect();
        } catch (Exception e) {
            System.err.println("Errore " + e.getMessage());
        }
    }
}

