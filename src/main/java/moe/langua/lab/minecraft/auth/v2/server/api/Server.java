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

        //initialize handlers
        new NotFoundHandler(-1, 0, httpServer, "/");
        new GetUUIDStatusHandler(Config.instance.minecraftServerFailedAttempts.get(0), Config.instance.minecraftServerFailedAttempts.get(1), httpServer, "/get/uuid/", searcher, verificationCodeManager, skinServer);
        new GetVerificationCodeDetailHandler(Config.instance.verificationPublicAPIUsageLimit.getVerificationCodeDetail.get(0), Config.instance.verificationPublicAPIUsageLimit.getVerificationCodeDetail.get(1), httpServer, "/get/code/", verificationCodeManager);
        new LocalSkinServerHandler(Config.instance.skinServerSettings.usageLimit.get(0), Config.instance.skinServerSettings.usageLimit.get(1), httpServer, "/get/skin/", skinServer.getDataRoot());
        new VerificationTryHandler(Config.instance.verificationPublicAPIUsageLimit.sendVerificationRequest.get(0), Config.instance.verificationPublicAPIUsageLimit.sendVerificationRequest.get(1), httpServer, "/try/", verificationCodeManager);
        httpServer.start();
    }
}
