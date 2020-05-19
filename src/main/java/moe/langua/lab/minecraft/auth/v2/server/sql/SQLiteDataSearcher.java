package moe.langua.lab.minecraft.auth.v2.server.sql;

import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.io.File;
import java.net.InetAddress;
import java.sql.*;
import java.util.UUID;

public class SQLiteDataSearcher implements DataSearcher {
    private final Connection jDBCConnection;
    private final String TABLE_PREFIX;

    public SQLiteDataSearcher(File dataRoot, String tablePrefix) throws IllegalArgumentException, SQLException {
        Utils.logger.log(LogRecord.Level.INFO, "Initializing SQLite for verification data storage...");
        TABLE_PREFIX = tablePrefix;
        File dataDirectory = new File(dataRoot.getAbsolutePath() + "/data");
        if (!dataDirectory.mkdir() && !dataDirectory.isDirectory()) {
            throw new IllegalArgumentException(dataDirectory.getAbsolutePath() + " should be a directory, but found a file.");
        }
        File dataBaseFile = new File(dataDirectory, "verification.db");
        String url = "jdbc:sqlite:" + dataBaseFile.getAbsolutePath();
        jDBCConnection = DriverManager.getConnection(url);
        String initializeTable =
                "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + "Verifications (\n" +
                        " RecordID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        " UUIDMost INTEGER,\n" +
                        " UUIDLeast INTEGER,\n" +
                        " Status INTEGER,\n" +
                        " CommitIPAddress TEXT,\n" +
                        " CommitTime INTEGER\n" +
                        ");";

        try (Statement statementInstance = jDBCConnection.createStatement()) {
            statementInstance.execute(initializeTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                jDBCConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));//close connection when shutdown the server

        Utils.logger.log(LogRecord.Level.INFO, "SQLite has been loaded successfully.");
    }

    @Override
    public int getPlayerStatus(UUID uniqueID) throws SQLException {
        String checkIfAPlayerExistInDatabase = "" +
                "SELECT Count(Status) AS Count FROM " + TABLE_PREFIX + "_Verifications\n" +
                "WHERE UUIDMost=" + uniqueID.getMostSignificantBits() + " AND UUIDLeast=" + uniqueID.getLeastSignificantBits() + ";";
        String insertVerificationStatement = "INSERT INTO " + TABLE_PREFIX + "Verifications(UniqueIDMost,UniqueIDLeast,Status) VALUES(?,?,?)";
        String getVerificationStatusStatement = "SELECT Status FROM " + TABLE_PREFIX + "Verifications " +
                "WHERE UniqueIDMost = " + uniqueID.getMostSignificantBits() + " AND UniqueIDLeast = " + uniqueID.getLeastSignificantBits() + ";";

        try (Statement statementInstance = jDBCConnection.createStatement()) {
            ResultSet resultSet = statementInstance.executeQuery(checkIfAPlayerExistInDatabase);
            resultSet.first();
            if (resultSet.getInt(0) == 0) {
                //insert new record
                PreparedStatement insertStatement = jDBCConnection.prepareStatement(insertVerificationStatement);
                insertStatement.setLong(0, uniqueID.getMostSignificantBits());
                insertStatement.setLong(1, uniqueID.getLeastSignificantBits());
                insertStatement.setInt(2, 1);
                insertStatement.executeUpdate();
                return 1;
            } else {
                //lookup status and return
                ResultSet statusResultSet = jDBCConnection.createStatement().executeQuery(getVerificationStatusStatement);
                return statusResultSet.getInt(0);
            }
        }
    }

    @Override
    public void setPlayerStatus(UUID uniqueID, int status, InetAddress commitAddress) throws SQLException {
        String updateStatement = "UPDATE " + TABLE_PREFIX + "Verifications SET Status = ? ,CommitIPAddress = ? ,CommitTime = ? " +
                "WHERE UniqueIDMost = " + uniqueID.getMostSignificantBits() + " AND UniqueIDLeast = " + uniqueID.getLeastSignificantBits() + ";";
        PreparedStatement preparedStatement = jDBCConnection.prepareStatement(updateStatement);
        preparedStatement.setInt(0, status);
        preparedStatement.setString(1, commitAddress.toString());
        preparedStatement.setLong(2, System.currentTimeMillis());
        preparedStatement.executeUpdate();
    }
}
