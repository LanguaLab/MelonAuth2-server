package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Message;
import moe.langua.lab.minecraft.auth.v2.server.sql.DataSearcher;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.minecraft.auth.v2.server.util.VerificationCodeManager;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.UUID;

import static moe.langua.lab.minecraft.auth.v2.server.util.Utils.server.INTERNAL_ERROR;
import static moe.langua.lab.minecraft.auth.v2.server.util.Utils.server.SERVER_NETWORK_ERROR;

public class TryHandler extends AbstractHandler {
    private final VerificationCodeManager verificationCodeManager;
    private final DataSearcher dataSearcher;

    public TryHandler(long limit, long resetPeriod, HttpServer httpServer, String handlePath, DataSearcher dataSearcher, VerificationCodeManager verificationCodeManager) {
        super(limit, resetPeriod, httpServer, handlePath);
        this.dataSearcher = dataSearcher;
        this.verificationCodeManager = verificationCodeManager;
    }

    @Override
    public void process(HttpExchange httpExchange, InetAddress requestAddress) {
        getLimiter().add(requestAddress, 1);
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
            try {
                dataSearcher.setPlayerStatus(playerUniqueID, true, requestAddress);
                Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(Message.getFromString("Your account has been verified successfully")));
                Utils.logger.log(LogRecord.Level.INFO, verificationCodeManager.getVerification(verificationCode).getPlayerName() + " (" + verificationCodeManager.getVerification(verificationCode).getUniqueID().toString() + ") has completed verification challenge.");
            } catch (SQLException e) {
                Utils.logger.log(LogRecord.Level.ERROR, e.toString());
                Utils.server.errorReturn(httpExchange, 500, INTERNAL_ERROR);
            } finally {
                verificationCodeManager.removeVerification(verificationCode);
            }
        } else {
            //failed
            Utils.server.returnNoContent(httpExchange, 304);
        }
    }
}
