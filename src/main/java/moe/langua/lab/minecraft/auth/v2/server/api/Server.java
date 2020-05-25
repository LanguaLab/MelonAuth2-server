package moe.langua.lab.minecraft.auth.v2.server.api;

import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.api.handler.*;
import moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.MainSettings;
import moe.langua.lab.minecraft.auth.v2.server.sql.DataSearcher;
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
        new DefaultHandler(-1, 0, httpServer, "/");
        new JoinHandler(MainSettings.instance.getMinecraftServerFailedAttempts().get(0), MainSettings.instance.getMinecraftServerFailedAttempts().get(1), httpServer, "/join/", searcher, verificationCodeManager, skinServer);
        new StatusHandler(MainSettings.instance.getAPIUsageSettings().getGetStatus().get(0), MainSettings.instance.getAPIUsageSettings().getGetStatus().get(1), httpServer, "/get/status/", searcher);
        new GetCodeHandler(MainSettings.instance.getAPIUsageSettings().getGetCode().get(0), MainSettings.instance.getAPIUsageSettings().getGetCode().get(1), httpServer, "/get/code/", verificationCodeManager);
        new GetSkinHandler(MainSettings.instance.getSkinServerSettings().getUsageLimit().get(0), MainSettings.instance.getSkinServerSettings().getUsageLimit().get(1), httpServer, "/get/skin/", skinServer.getDataRoot());
        new TryHandler(MainSettings.instance.getAPIUsageSettings().getVerify().get(0), MainSettings.instance.getAPIUsageSettings().getVerify().get(1), httpServer, "/verify/", searcher, verificationCodeManager);
        httpServer.start();
    }
}
