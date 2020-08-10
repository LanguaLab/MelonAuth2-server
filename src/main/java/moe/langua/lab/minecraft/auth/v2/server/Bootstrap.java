package moe.langua.lab.minecraft.auth.v2.server;

import moe.langua.lab.minecraft.auth.v2.server.api.Server;
import moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.MainSettings;
import moe.langua.lab.minecraft.auth.v2.server.sql.SQLiteDataSearcher;
import moe.langua.lab.minecraft.auth.v2.server.util.PassManager;
import moe.langua.lab.minecraft.auth.v2.server.util.SkinServer;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.handler.ConsoleLogHandler;
import moe.langua.lab.utils.logger.handler.DailyRollingFileLogHandler;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class Bootstrap {


    public static void main(String... args) throws SQLException, IOException {
        long start = System.currentTimeMillis();
        System.out.println("Loading runtime...");

        File dataRoot = new File(new File("").getAbsolutePath());
        File settingsFile = new File(dataRoot.getAbsolutePath() + "/config.json");
        MainSettings.instance = MainSettings.readFromFile(settingsFile);
        Utils.logger.addHandler(new ConsoleLogHandler(LogRecord.Level.getFromName(MainSettings.instance.getMinimumLogRecordLevel())));

        File logFolder = new File(dataRoot.getAbsolutePath() + "/logs");
        if (!logFolder.mkdir() && logFolder.isFile()) {
            throw new IOException("LogFolder " + logFolder.getAbsolutePath() + " should be a folder, but found a file.");
        }

        try {
            Utils.logger.addHandler(new DailyRollingFileLogHandler(LogRecord.Level.getFromName(MainSettings.instance.getMinimumLogRecordLevel()), logFolder));
        } catch (IOException e) {
            Utils.logger.log(LogRecord.Level.FATAL, e.toString());
        }

        Utils.logger.log(LogRecord.Level.INFO, "Loading server SecretKey...");
        Utils.passManager = new PassManager(MainSettings.instance.getSecretKeys(), MainSettings.instance.getQueueKeys());

        Utils.logger.log(LogRecord.Level.INFO, "Initializing SkinServer...");
        File skinServerRoot = new File(dataRoot.getAbsolutePath() + "/" + MainSettings.instance.getSkinBase());

        SkinServer skinServer = new SkinServer(skinServerRoot, MainSettings.instance.getChallengeLife());
        Runtime.getRuntime().addShutdownHook(new Thread(skinServer::purgeAll));

        Utils.logger.log(LogRecord.Level.INFO, "API Starting...");
        Utils.logger.log(LogRecord.Level.INFO, "Worker threads: " + MainSettings.instance.getWorkerThreads());
        new Server(11014, new SQLiteDataSearcher(new File(dataRoot.getAbsolutePath() + "/" + "data")), skinServer);
        Utils.logger.log(LogRecord.Level.INFO, "Done(" + (System.currentTimeMillis() - start) / 1000.0 + "s)! All modules have started.");
    }
}
