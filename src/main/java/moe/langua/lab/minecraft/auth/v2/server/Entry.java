package moe.langua.lab.minecraft.auth.v2.server;

import moe.langua.lab.minecraft.auth.v2.server.api.Server;
import moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.MainSettings;
import moe.langua.lab.minecraft.auth.v2.server.sql.SQLiteDataSearcher;
import moe.langua.lab.minecraft.auth.v2.server.util.PassManager;
import moe.langua.lab.minecraft.auth.v2.server.util.SkinServer;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class Entry {


    public static void main(String... args) throws SQLException, IOException {
        long start = System.currentTimeMillis();
        System.out.println("Loading runtime...");

        File dataRoot = new File(new File("").getAbsolutePath());
        File settingsFile = new File(dataRoot.getAbsolutePath() + "/config.json");
        MainSettings.instance = MainSettings.readFromFile(settingsFile);

        File logFolder = new File(dataRoot.getAbsolutePath() + "/logs");
        if (!logFolder.mkdir() && logFolder.isFile()) {
            throw new IOException("LogFolder " + logFolder.getAbsolutePath() + " should be a folder, but found a file.");
        }

        Utils.logger.info("Loading server SecretKey...");
        Utils.passManager = new PassManager(MainSettings.instance.getSecretKeys(), MainSettings.instance.getQueueKeys());

        Utils.logger.info("Initializing SkinServer...");
        File skinServerRoot = new File(dataRoot.getAbsolutePath() + "/" + MainSettings.instance.getSkinBase());

        SkinServer skinServer = new SkinServer(skinServerRoot, MainSettings.instance.getChallengeLife());
        Runtime.getRuntime().addShutdownHook(new Thread(skinServer::purgeAll));

        Utils.logger.info("API Starting...");
        Utils.logger.info("Worker threads: " + MainSettings.instance.getWorkerThreads());
        new Server(11014, new SQLiteDataSearcher(new File(dataRoot.getAbsolutePath() + "/" + "data")), skinServer);
        Utils.logger.info("Done(" + (System.currentTimeMillis() - start) / 1000.0 + "s)! All modules have started.");
    }
}
