package moe.langua.lab.minecraft.auth.v2.server.api;

import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.api.handler.*;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Config;
import moe.langua.lab.minecraft.auth.v2.server.util.DataSearcher;
import moe.langua.lab.minecraft.auth.v2.server.util.SkinServer;
import moe.langua.lab.minecraft.auth.v2.server.util.VerificationCodeManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public Server(int port, DataSearcher searcher, SkinServer skinServer) {
        HttpServer httpServer;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-2);
            return;
        }
        VerificationCodeManager verificationCodeManager = new VerificationCodeManager();

        httpServer.createContext("/", new NotFoundHandler());
        httpServer.createContext("/get/uuid/", new GetUUIDStatusHandler(Config.instance.minecraftServerFailedAttempts.get(0), Config.instance.minecraftServerFailedAttempts.get(1), searcher, verificationCodeManager, skinServer));
        httpServer.createContext("/get/code/", new GetVerificationCodeDetailHandler(Config.instance.verificationPublicAPIUsageLimit.getVerificationCodeDetail.get(0), Config.instance.verificationPublicAPIUsageLimit.getVerificationCodeDetail.get(1), verificationCodeManager));
        httpServer.createContext("/get/skin/", new LocalSkinServerHandler(Config.instance.skinServerSettings.usageLimit.get(0), Config.instance.skinServerSettings.usageLimit.get(1), skinServer.getDataRoot()));
        httpServer.createContext("/try/", new VerificationTryHandler(Config.instance.verificationPublicAPIUsageLimit.sendVerificationRequest.get(0), Config.instance.verificationPublicAPIUsageLimit.sendVerificationRequest.get(1), verificationCodeManager));
        httpServer.start();
    }
}
