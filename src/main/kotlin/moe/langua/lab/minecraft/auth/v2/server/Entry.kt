package moe.langua.lab.minecraft.auth.v2.server

import moe.langua.lab.minecraft.auth.v2.server.api.Server
import moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.MainSettings
import moe.langua.lab.minecraft.auth.v2.server.sql.SQLiteDataSearcher
import moe.langua.lab.minecraft.auth.v2.server.util.PassManager
import moe.langua.lab.minecraft.auth.v2.server.util.SkinServer
import moe.langua.lab.minecraft.auth.v2.server.util.Utils
import java.io.File
import java.io.IOException

object Entry {
    @JvmStatic
    fun main(args: Array<String>) {
        Thread({
            val start = System.currentTimeMillis()
            println("Loading runtime...")
            val dataRoot = File("")
            Utils.logger.info("Root directory: ${dataRoot.absolutePath}")
            val settingsFile = File(dataRoot.absolutePath + "/config.json")
            MainSettings.instance = MainSettings.readFromFile(settingsFile)
            val logFolder = File(dataRoot.absolutePath + "/logs")
            if (!logFolder.mkdir() && logFolder.isFile) {
                throw IOException("LogFolder ${logFolder.absolutePath} should be a folder, but found a file.")
            }
            Utils.logger.info("Loading server Secret Key...")
            Utils.passManager = PassManager(MainSettings.instance.secretKeys, MainSettings.instance.queueKeys)
            Utils.logger.info("Initializing Skin Server...")
            val skinServerRoot = File(dataRoot.absolutePath + "/" + MainSettings.instance.skinBase)
            val skinServer = SkinServer(skinServerRoot, MainSettings.instance.challengeLife)
            Runtime.getRuntime().addShutdownHook(Thread { skinServer.purgeAll() })
            Utils.logger.info("API Starting...")
            Utils.logger.info("Worker threads pool size: " + MainSettings.instance.workerThreads)
            Server(11014, SQLiteDataSearcher(File(dataRoot.absolutePath + "/" + "data")), skinServer)
            Utils.logger.info("Done(" + (System.currentTimeMillis() - start) / 1000.0 + "s)! All modules have started.")
        },"Server Loader").start()
    }
}