package moe.langua.lab.minecraft.auth.v2.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import moe.langua.lab.minecraft.auth.v2.server.api.Server;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Config;
import moe.langua.lab.minecraft.auth.v2.server.sql.SQLiteDataSearcher;
import moe.langua.lab.minecraft.auth.v2.server.util.SkinServer;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.handler.ConsoleLogHandler;
import moe.langua.lab.utils.logger.handler.DailyRollingFileLogHandler;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

public class Bootstrap {

    public static void main(String... args) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        long start = System.currentTimeMillis();
        System.out.println("Loading runtime...");
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        File dataRoot = new File("");
        File configFile = new File(dataRoot.getAbsolutePath() + "/config.json");
        Config config;
        try {
            if (configFile.createNewFile()) {
                config = Config.getDefault();
            } else if (configFile.isFile()) {
                config = Utils.gson.fromJson(new FileReader(configFile), Config.class);
            } else {
                throw new IllegalArgumentException(configFile.getAbsolutePath() + " should be a file, but found a directory.");
            }
            config.check();
            FileWriter writer = new FileWriter(configFile, false);
            writer.write(prettyGson.toJson(config));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            return;
        }

        Config.instance = config;
        Utils.logger.addHandler(new ConsoleLogHandler(LogRecord.Level.getFromName(config.getMinimumLogRecordLevel())));

        File logFolder = new File(dataRoot.getAbsolutePath() + "/logs");
        if (!logFolder.mkdir() && logFolder.isFile()) {
            throw new IllegalArgumentException("LogFolder " + logFolder.getAbsolutePath() + " should be a folder, but found a file.");
        }
        try {
            Utils.logger.addHandler(new DailyRollingFileLogHandler(LogRecord.Level.getFromName(config.getMinimumLogRecordLevel()), logFolder));
        } catch (IOException e) {
            Utils.logger.log(LogRecord.Level.FATAL, e.toString());
        }

        Utils.logger.log(LogRecord.Level.INFO, "Loading server SecretKey...");
        if (config.getClientKey().length() < 64) {
            Utils.logger.log(LogRecord.Level.WARN, "Short secret key detected. Remove the secret key object completely from 'config.json' and restart the server to generate a new key to avoid this warning.");
        }

        Utils.logger.log(LogRecord.Level.INFO, "Initializing SkinServer...");
        File skinServerRoot = new File(new File(dataRoot.getAbsolutePath()) + config.getSkinServerSettings().getDataRoot());
        SkinServer skinServer = new SkinServer(skinServerRoot, config.getAPIUrl(), config.getVerificationExpireTime());
        Runtime.getRuntime().addShutdownHook(new Thread(skinServer::purgeAll));

        Utils.logger.log(LogRecord.Level.INFO, "API Starting...");
        new Server(11014, new SQLiteDataSearcher(dataRoot, Config.instance.getDatabaseSettings().getTablePrefix()), skinServer);
        Utils.logger.log(LogRecord.Level.INFO, "Done(" + (System.currentTimeMillis() - start) / 1000.0 + "s)! All modules have started.");
    }
}
