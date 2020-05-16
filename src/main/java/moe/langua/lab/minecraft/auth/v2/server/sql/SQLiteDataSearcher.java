package moe.langua.lab.minecraft.auth.v2.server.sql;

import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.io.File;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

public class SQLiteDataSearcher implements DataSearcher {
    private final Connection jDBCConnection;

    public SQLiteDataSearcher(File dataRoot) throws IllegalArgumentException, SQLException {
        Utils.logger.log(LogRecord.Level.INFO, "Initializing SQLite for verification data storage...");
        File dataDirectory = new File(dataRoot.getAbsolutePath()+ "/data");
        if (!dataDirectory.mkdir() && !dataDirectory.isDirectory()) {
            throw new IllegalArgumentException(dataDirectory.getAbsolutePath() + " should be a directory, but found a file.");
        }
        File dataBaseFile = new File(dataDirectory, "verification.db");
        String url = "jdbc:sqlite:" + dataBaseFile.getAbsolutePath();
        jDBCConnection = DriverManager.getConnection(url);

        initialize_database:{

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
    public int getPlayerStatus(UUID uniqueID) {
        return 0;
    }

    @Override
    public boolean setPlayerStatus(UUID uniqueID, int status, InetAddress commitAddress) {
        return false;
    }
}
