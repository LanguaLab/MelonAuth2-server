package moe.langua.lab.minecraft.auth.v2.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import moe.langua.lab.minecraft.auth.v2.server.api.Server;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Config;
import moe.langua.lab.minecraft.auth.v2.server.util.DataSearcher;
import moe.langua.lab.minecraft.auth.v2.server.util.Database;
import moe.langua.lab.minecraft.auth.v2.server.util.SkinServer;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.handler.ConsoleLogHandler;
import moe.langua.lab.utils.logger.handler.DailyRollingFileLogHandler;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class Bootstrap {

    public static void main(String... args) {
        long start = System.currentTimeMillis();
        System.out.println("Loading runtime...");
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        File dataRoot = new File("");
        File configFile = new File(dataRoot.getAbsolutePath() + "/config.json");
        Config config = null;
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
                config = Config.getDefault();
            } else {
                config = Utils.gson.fromJson(new FileReader(configFile), Config.class);
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

        if (config.sourceCode.length() == 0) {
            System.out.println("MelonAuth2-server  Copyright (C) 2020  LanguaLab");
            System.out.println("MelonAuth2-server is distributed under the terms of the GNU Affero General Public License version 3 (AGPLv3), which means you should make sure that users can get the source code of this copy.");
            System.out.println("Before using MelonAuth2-server, you need to fill in the 'sourceCode' object in 'config.json'. The text you fill in should be a url, which include the source code of this MelonAuth2-server copy.");
            System.out.println("For terms and conditions of AGPLv3, see https://www.gnu.org/licenses/agpl-3.0.html");
            System.exit(-2);
            return;
        }

        Config.instance = config;
        System.out.println("Initializing logger...");
        Utils.logger.addHandler(new ConsoleLogHandler(LogRecord.Level.getFromName(config.minimumLogRecordLevel)));

        File logFolder = new File(dataRoot.getAbsolutePath() + "/logs");
        if (!logFolder.exists()) logFolder.mkdir();
        if (logFolder.isFile()) {
            Utils.logger.log(LogRecord.Level.ERROR, "LogFolder \"" + logFolder.getAbsolutePath() + "\" should be a folder, but found a file.");
            System.exit(-1);
            return;
        }
        try {
            Utils.logger.addHandler(new DailyRollingFileLogHandler(LogRecord.Level.getFromName(config.minimumLogRecordLevel), logFolder));
        } catch (IOException e) {
            Utils.logger.log(LogRecord.Level.FATAL, e.toString());
        }

        Utils.logger.log(LogRecord.Level.INFO, "Loading server SecretKey...");
        try {
            UUID key = UUID.fromString(config.secretKey);
        } catch (IllegalArgumentException e) {
            Utils.logger.log(LogRecord.Level.FATAL, e.toString());
            Utils.logger.log(LogRecord.Level.FATAL, "Secret key must be a full uuid");
            System.exit(-1);
            return;
        }

        Utils.logger.log(LogRecord.Level.INFO, "Initializing SkinServer...");
        File skinServerRoot = new File(new File(dataRoot.getAbsolutePath()) + config.skinServerSettings.dataRoot);
        SkinServer skinServer = new SkinServer(skinServerRoot, config.aPIUrl, config.verificationExpireTime);
        Runtime.getRuntime().addShutdownHook(new Thread(skinServer::purgeAll));

        Utils.logger.log(LogRecord.Level.INFO, "API Starting...");
        new Server(11014, new DataSearcher(new Database()), skinServer);
        Utils.logger.log(LogRecord.Level.INFO, "Done(" + (System.currentTimeMillis() - start) / 1000.0 + "s)! All modules have started.");
    }
}
