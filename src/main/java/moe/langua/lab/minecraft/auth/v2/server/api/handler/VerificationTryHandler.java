package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Message;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.minecraft.auth.v2.server.util.VerificationCodeManager;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import static moe.langua.lab.minecraft.auth.v2.server.util.Utils.server.*;

public class VerificationTryHandler extends AbstractHandler {
    private final VerificationCodeManager verificationCodeManager;

    public VerificationTryHandler(int limit, long resetPeriod, HttpServer httpServer, String handlePath, VerificationCodeManager verificationCodeManager) {
        super(limit, resetPeriod, httpServer, handlePath);
        this.verificationCodeManager = verificationCodeManager;
    }

    @Override
    public void process(HttpExchange httpExchange, InetAddress requestAddress) {
        getLimiter().add(requestAddress,1);
        int verificationCode;
        try {
            verificationCode = Integer.parseInt(Utils.getLastChild(httpExchange.getRequestURI()));
        } catch (NumberFormatException e) {
            return;
        }
        if (!verificationCodeManager.hasVerification(verificationCode)) {
            return;
        } else if (verificationCodeManager.getVerification(verificationCode).isExpired()) {
            verificationCodeManager.removeVerification(verificationCode);
            return;
        }

        BufferedImage skin;
        UUID playerUniqueID = verificationCodeManager.getVerification(verificationCode).getUniqueID();
        try {
            skin = Utils.getSkin(playerUniqueID);
        } catch (IOException e) {
            Utils.logger.log(LogRecord.Level.WARN, e.toString());
            Utils.server.errorReturn(httpExchange, 500, SERVER_NETWORK_ERROR);
            return;
        }
        if (verificationCodeManager.getVerification(verificationCode).verify(skin)) {
            //success
            verificationCodeManager.removeVerification(verificationCode);
            Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(Message.getFromString("Your account has been verified successfully")));
            Utils.logger.log(LogRecord.Level.INFO, verificationCodeManager.getVerification(verificationCode).getPlayerName() + " (" + verificationCodeManager.getVerification(verificationCode).getUniqueID().toString() + ") has completed verification challenge.");
        } else {
            //failed
            Utils.server.returnNoContent(httpExchange, 304);
        }
    }
}
