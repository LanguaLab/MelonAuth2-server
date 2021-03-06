package moe.langua.lab.minecraft.auth.v2.server.api;

import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.api.handler.*;
import moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.MainSettings;
import moe.langua.lab.minecraft.auth.v2.server.sql.DataSearcher;
import moe.langua.lab.minecraft.auth.v2.server.util.ChallengeManager;
import moe.langua.lab.minecraft.auth.v2.server.util.SkinServer;

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
        ChallengeManager challengeManager = new ChallengeManager();

        //initialize handlers
        new DefaultHandler(-1, 0, httpServer, "/");
        new RequireHandler(MainSettings.instance.getClientAuthenticationFailed().getLimitPerCircle(), MainSettings.instance.getClientAuthenticationFailed().getCircleInMillisecond(), httpServer, "/require/", challengeManager, skinServer);
        new GetStatusHandler(MainSettings.instance.getAPIUsageSettings().getGetStatus().getLimitPerCircle(), MainSettings.instance.getAPIUsageSettings().getGetStatus().getCircleInMillisecond(), httpServer, "/get/status/", searcher);
        new GetChallengeHandler(MainSettings.instance.getAPIUsageSettings().getGetCode().getLimitPerCircle(), MainSettings.instance.getAPIUsageSettings().getGetCode().getCircleInMillisecond(), httpServer, "/get/challenge/", challengeManager);
        new GetSkinHandler(MainSettings.instance.getAPIUsageSettings().getGetSkin().getLimitPerCircle(), MainSettings.instance.getAPIUsageSettings().getGetSkin().getCircleInMillisecond(), httpServer, "/get/skin/", skinServer.getDataRoot());
        new VerifyHandler(MainSettings.instance.getAPIUsageSettings().getVerify().getLimitPerCircle(), MainSettings.instance.getAPIUsageSettings().getVerify().getCircleInMillisecond(), httpServer, "/verify/", searcher, challengeManager);
        httpServer.start();
    }
}
