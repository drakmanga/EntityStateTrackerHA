package webSocketHomeAssistant;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class EntityService extends SSHJDBCConnectionWithKey {

    public void insertEntityActivity(String entityId, String state, String friendlyName, String userId) {
        try {

            Integer entityDbId = getOrCreateEntityId(entityId);
            Integer userDbId = getUserId(userId);


            if (entityDbId == null) {
                throw new Exception("Parametro obbligatorio mancante");
            }
            if (userDbId == null) {
                userDbId = 4;
            }

            String sql = "INSERT INTO EntityActivity (entityId, state, lastUpdate, friendlyName, userId) VALUES (?, ?, ?, ?, ?)";

            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, entityDbId);
            stmt.setString(2, state);
            stmt.setTimestamp(3, timestamp);
            stmt.setString(4, friendlyName);
            stmt.setInt(5, userDbId);
            stmt.execute();

            System.out.println("Inserimento dati eseguito con successo");
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Errore SQL: " + e.getMessage());
        } catch (Exception a) {
            System.err.println("Errore Exception " + a.getMessage());
        }
    }

    // Metodo per recuperare o creare un'entità
    private Integer getOrCreateEntityId(String entityName) throws SQLException {
        Integer entityId = getEntityId(entityName);
        if (entityId == null) {
            String insertSql = "INSERT INTO Entity (name, dateAdded) VALUES (?, NOW())";

            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, entityName);
                insertStmt.executeUpdate();
                insertStmt.close();

                ResultSet rs = insertStmt.getGeneratedKeys();
                if (rs.next()) {
                    entityId = rs.getInt(1);
                }
            }
        }
        return entityId;
    }

    // Metodo per recuperare l'ID di un'entità
    private Integer getEntityId(String entityName) throws SQLException {
        String sql = "SELECT id FROM Entity WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, entityName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("id") : null;
        }
    }

    // Metodo per recuperare l'ID di un utente
    private Integer getUserId(String userId) throws SQLException {
        String sql = "SELECT id FROM Users WHERE userId = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("id") : null;
        }
    }
}
