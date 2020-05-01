package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Message;
import moe.langua.lab.minecraft.auth.v2.server.util.AbstractHandler;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.minecraft.auth.v2.server.util.VerificationCodeManager;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

import static moe.langua.lab.minecraft.auth.v2.server.util.Utils.server.*;

public class VerificationTryHandler extends AbstractHandler {
    private final VerificationCodeManager verificationCodeManager;

    public VerificationTryHandler(int limit, long resetPeriod, HttpServer httpServer, String handlePath, VerificationCodeManager verificationCodeManager) {
        super(limit, resetPeriod, httpServer, handlePath);
        this.verificationCodeManager = verificationCodeManager;
    }

    @Override
    public void process(HttpExchange httpExchange) {
        super.process(httpExchange);
        if (httpExchange.getResponseCode() != -1) return;

        int verificationCode;
        try {
            verificationCode = Integer.parseInt(Utils.getLastChild(httpExchange.getRequestURI()));
        } catch (NumberFormatException e) {
            errorReturn(httpExchange, 404, NOT_FOUND_ERROR);
            return;
        }
        if (!verificationCodeManager.hasVerification(verificationCode)) {
            errorReturn(httpExchange, 404, NOT_FOUND_ERROR);
            return;
        } else if (verificationCodeManager.getVerification(verificationCode).isExpired()) {
            verificationCodeManager.removeVerification(verificationCode);
            errorReturn(httpExchange, 404, NOT_FOUND_ERROR);
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
            Utils.logger.log(LogRecord.Level.FINE, verificationCodeManager.getVerification(verificationCode).getPlayerName() + " (" + verificationCodeManager.getVerification(verificationCode).getUniqueID().toString() + ") has completed auth challenge.");
        } else {
            //failed
            Utils.server.returnNoContent(httpExchange, 304);
            Utils.logger.log(LogRecord.Level.DEBUG, verificationCodeManager.getVerification(verificationCode).getPlayerName() + " (" + verificationCodeManager.getVerification(verificationCode).getUniqueID().toString() + ") Failed to auth.");
        }
    }
}
