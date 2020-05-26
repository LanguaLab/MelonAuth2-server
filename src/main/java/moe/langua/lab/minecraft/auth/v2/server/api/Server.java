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
        new JoinHandler(MainSettings.instance.getClientAuthenticationFailed().getLimitPerCircle(), MainSettings.instance.getClientAuthenticationFailed().getCircleInMillisecond(), httpServer, "/join/", searcher, verificationCodeManager, skinServer);
        new GetStatusHandler(MainSettings.instance.getAPIUsageSettings().getGetStatus().getLimitPerCircle(), MainSettings.instance.getAPIUsageSettings().getGetStatus().getCircleInMillisecond(), httpServer, "/get/status/", searcher);
        new GetCodeHandler(MainSettings.instance.getAPIUsageSettings().getGetCode().getLimitPerCircle(), MainSettings.instance.getAPIUsageSettings().getGetCode().getCircleInMillisecond(), httpServer, "/get/code/", verificationCodeManager);
        new GetSkinHandler(MainSettings.instance.getAPIUsageSettings().getGetSkin().getLimitPerCircle(), MainSettings.instance.getAPIUsageSettings().getGetSkin().getCircleInMillisecond(), httpServer, "/get/skin/", skinServer.getDataRoot());
        new VerifyHandler(MainSettings.instance.getAPIUsageSettings().getVerify().getLimitPerCircle(), MainSettings.instance.getAPIUsageSettings().getVerify().getCircleInMillisecond(), httpServer, "/verify/", searcher, verificationCodeManager);
        httpServer.start();
    }
}
