package moe.langua.lab.minecraft.auth.v2.server.sql;

import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.io.File;
import java.net.InetAddress;
import java.sql.*;
import java.util.UUID;

public class SQLiteDataSearcher implements DataSearcher {
    private final Connection jDBCConnection;
    private final PreparedStatement checkPlayerExistenceStatement;
    private final PreparedStatement insertStatement;
    private final PreparedStatement getStatusStatement;
    private final PreparedStatement updateStatement;

    public SQLiteDataSearcher(File dataRoot, String tablePrefix) throws IllegalArgumentException, SQLException {
        Utils.logger.log(LogRecord.Level.INFO, "Initializing SQLite for verification data storage...");
        File dataDirectory = new File(dataRoot.getAbsolutePath() + "/data");
        if (!dataDirectory.mkdir() && !dataDirectory.isDirectory()) {
            throw new IllegalArgumentException(dataDirectory.getAbsolutePath() + " should be a directory, but found a file.");
        }
        File dataBaseFile = new File(dataDirectory, "verification.db");
        String url = "jdbc:sqlite:" + dataBaseFile.getAbsolutePath();
        jDBCConnection = DriverManager.getConnection(url);
        String initializeTable =
                "CREATE TABLE IF NOT EXISTS " + tablePrefix + "Verifications (\n" +
                        " RecordID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        " UniqueIDMost INTEGER,\n" +
                        " UniqueIDLeast INTEGER,\n" +
                        " Status BOOLEAN,\n" +
                        " CommitIPAddress TEXT,\n" +
                        " CommitTime INTEGER\n" +
                        ");";

        {
            Statement statementInstance = jDBCConnection.createStatement();
            statementInstance.execute(initializeTable);
            statementInstance.close();
        }


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                jDBCConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));//close connection when shutdown the server

        checkPlayerExistenceStatement = jDBCConnection.prepareStatement(
                "SELECT Count(RecordID) AS Count FROM " + tablePrefix + "Verifications\n" +
                        "WHERE UniqueIDMost = ? AND UniqueIDLeast = ?;");

        insertStatement = jDBCConnection.prepareStatement(
                "INSERT INTO " + tablePrefix + "Verifications(UniqueIDMost,UniqueIDLeast,Status) VALUES(?,?,?);");

        getStatusStatement = jDBCConnection.prepareStatement(
                "SELECT Status FROM " + tablePrefix + "Verifications " +
                        "WHERE UniqueIDMost = ? AND UniqueIDLeast = ?;");

        updateStatement = jDBCConnection.prepareStatement(
                "UPDATE " + tablePrefix + "Verifications SET Status = ? ,CommitIPAddress = ? ,CommitTime = ? " +
                        "WHERE UniqueIDMost = ? AND UniqueIDLeast = ?;");

        Utils.logger.log(LogRecord.Level.INFO, "SQLite has been loaded.");
    }

    @Override
    public synchronized boolean getPlayerStatus(UUID uniqueID) throws SQLException {
        checkPlayerExistenceStatement.setLong(1, uniqueID.getMostSignificantBits());
        checkPlayerExistenceStatement.setLong(2, uniqueID.getLeastSignificantBits());
        ResultSet resultSet = checkPlayerExistenceStatement.executeQuery();

        if (resultSet.getLong(1) == 0) {
            //insert new record
            insertStatement.setLong(1, uniqueID.getMostSignificantBits());
            insertStatement.setLong(2, uniqueID.getLeastSignificantBits());
            insertStatement.setBoolean(3, false);
            insertStatement.executeUpdate();
            return false;
        } else {
            //lookup status and return
            getStatusStatement.setLong(1, uniqueID.getMostSignificantBits());
            getStatusStatement.setLong(2, uniqueID.getLeastSignificantBits());
            ResultSet statusResultSet = getStatusStatement.executeQuery();
            return statusResultSet.getBoolean("Status");
        }
    }

    @Override
    public synchronized void setPlayerStatus(UUID uniqueID, boolean status, InetAddress commitAddress) throws SQLException {
        updateStatement.setBoolean(1, status);
        updateStatement.setString(2, commitAddress.toString());
        updateStatement.setLong(3, System.currentTimeMillis());
        updateStatement.setLong(4, uniqueID.getMostSignificantBits());
        updateStatement.setLong(5, uniqueID.getLeastSignificantBits());
        updateStatement.executeUpdate();
    }
}
