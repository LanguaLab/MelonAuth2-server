package moe.langua.lab.minecraft.auth.v2.server.sql;

import moe.langua.lab.minecraft.auth.v2.server.json.server.PlayerStatus;
import moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.MainSettings;
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

    public SQLiteDataSearcher(File dataRoot) throws IllegalArgumentException, SQLException {
        Utils.logger.log(LogRecord.Level.INFO, "Initializing SQLite for verification data storage...");
        if (!dataRoot.mkdir() && !dataRoot.isDirectory()) {
            throw new IllegalArgumentException(dataRoot.getAbsolutePath() + " should be a directory, but found a file.");
        }
        File dataBaseFile = new File(dataRoot, "verification.db");
        String url = "jdbc:sqlite:" + dataBaseFile.getAbsolutePath();
        jDBCConnection = DriverManager.getConnection(url);
        String synchronousConfig = "PRAGMA synchronous = " + MainSettings.instance.getDatabaseSettings().getSqLiteSettings().getSynchronous() + ";";
        String journalModeConfig = "PRAGMA journal_mode = " + MainSettings.instance.getDatabaseSettings().getSqLiteSettings().getJournalMode() + ";";
        String initializeTable =
                "CREATE TABLE IF NOT EXISTS Verifications (\n" +
                        " RecordID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        " UniqueIDMost INTEGER,\n" +
                        " UniqueIDLeast INTEGER,\n" +
                        " Status INTEGER,\n" +
                        " CommitIPAddress TEXT,\n" +
                        " CommitTime INTEGER\n" +
                        ");";
        String initializeIndex =
                "CREATE INDEX IF NOT EXISTS VerificationIndex ON Verifications (\n" +
                        "\tUniqueIDMost,\n" +
                        "\tUniqueIDLeast\n" +
                        ");";

        {
            Statement statementInstance = jDBCConnection.createStatement();
            statementInstance.execute(synchronousConfig);
            statementInstance.execute(journalModeConfig);
            statementInstance.execute(initializeTable);
            statementInstance.execute(initializeIndex);
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
                "SELECT Count(Status) AS Count FROM Verifications\n" +
                        "WHERE UniqueIDMost = ? AND UniqueIDLeast = ?;");

        insertStatement = jDBCConnection.prepareStatement(
                "INSERT INTO Verifications(UniqueIDMost,UniqueIDLeast,Status) VALUES(?,?,?);");

        getStatusStatement = jDBCConnection.prepareStatement(
                "SELECT Status,CommitTime FROM Verifications " +
                        "WHERE UniqueIDMost = ? AND UniqueIDLeast = ?;");

        updateStatement = jDBCConnection.prepareStatement(
                "UPDATE Verifications SET Status = ? ,CommitIPAddress = ? ,CommitTime = ? " +
                        "WHERE UniqueIDMost = ? AND UniqueIDLeast = ?;");

        Utils.logger.log(LogRecord.Level.INFO, "SQLite has been loaded.");
    }

    @Override
    public synchronized PlayerStatus getPlayerStatus(UUID uniqueID) throws SQLException {
        checkPlayerExistenceStatement.setLong(1, uniqueID.getMostSignificantBits());
        checkPlayerExistenceStatement.setLong(2, uniqueID.getLeastSignificantBits());
        ResultSet resultSet = checkPlayerExistenceStatement.executeQuery();

        if (resultSet.getLong(1) == 0) {
            //insert new record
            insertStatement.setLong(1, uniqueID.getMostSignificantBits());
            insertStatement.setLong(2, uniqueID.getLeastSignificantBits());
            insertStatement.setBoolean(3, false);
            insertStatement.executeUpdate();
            return PlayerStatus.get(uniqueID, false, null, null);
        } else {
            //lookup status and return
            getStatusStatement.setLong(1, uniqueID.getMostSignificantBits());
            getStatusStatement.setLong(2, uniqueID.getLeastSignificantBits());
            ResultSet statusResultSet = getStatusStatement.executeQuery();
            if (statusResultSet.getBoolean("Status")) {
                if (MainSettings.instance.isLifetimeVerification()) {
                    return PlayerStatus.get(uniqueID, true, statusResultSet.getLong("CommitTime"), null);
                } else {
                    if (System.currentTimeMillis() < (statusResultSet.getLong("CommitTime") + MainSettings.instance.getVerificationLife())) {
                        return PlayerStatus.get(uniqueID, false, null, null);
                    } else {
                        return PlayerStatus.get(uniqueID, true, statusResultSet.getLong("CommitTime"), MainSettings.instance.getVerificationLife());
                    }
                }
            } else {
                return PlayerStatus.get(uniqueID, false, null, null);
            }
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
