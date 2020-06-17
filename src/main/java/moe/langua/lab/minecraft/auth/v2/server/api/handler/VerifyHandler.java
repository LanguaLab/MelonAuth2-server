package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.api.Limiter;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Message;
import moe.langua.lab.minecraft.auth.v2.server.sql.DataSearcher;
import moe.langua.lab.minecraft.auth.v2.server.util.ChallengeManager;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.UUID;

import static moe.langua.lab.minecraft.auth.v2.server.util.Utils.server.INTERNAL_ERROR;
import static moe.langua.lab.minecraft.auth.v2.server.util.Utils.server.SERVER_NETWORK_ERROR;

public class VerifyHandler extends AbstractHandler {
    private final ChallengeManager challengeManager;
    private final DataSearcher dataSearcher;
    private final Limiter<UUID> uuidLimiter = new Limiter<>(1, 60000, "/verify/");

    public VerifyHandler(long limit, long resetPeriod, HttpServer httpServer, String handlePath, DataSearcher dataSearcher, ChallengeManager challengeManager) {
        super(limit, resetPeriod, httpServer, handlePath);
        this.dataSearcher = dataSearcher;
        this.challengeManager = challengeManager;
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
        if (!challengeManager.hasChallenge(verificationCode)) {
            return;
        } else if (challengeManager.getChallenge(verificationCode).isExpired()) {
            challengeManager.removeChallenge(verificationCode);
            return;
        }

        BufferedImage skin;
        UUID playerUniqueID = challengeManager.getChallenge(verificationCode).getUniqueID();
        if (!uuidLimiter.getUsability(playerUniqueID)) {
            Utils.server.errorReturn(httpExchange, 429, Utils.server.TOO_MANY_REQUEST_ERROR.clone().setErrorMessage("only the first request will be proceed each minute per unique id").setExtra("" + (uuidLimiter.getNextReset() - System.currentTimeMillis())));
            return;
        }
        uuidLimiter.add(playerUniqueID, 1);
        try {
            skin = Utils.getSkin(playerUniqueID);
        } catch (IOException e) {
            Utils.logger.log(LogRecord.Level.WARN, e.toString());
            Utils.server.errorReturn(httpExchange, 500, SERVER_NETWORK_ERROR);
            return;
        }
        if (challengeManager.getChallenge(verificationCode).verify(skin)) {
            //success
            try {
                dataSearcher.setPlayerStatus(playerUniqueID, true, requestAddress);
                Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(Message.getFromString("Your account has been verified")));
                Utils.logger.log(LogRecord.Level.INFO, challengeManager.getChallenge(verificationCode).getPlayerName() + " (" + challengeManager.getChallenge(verificationCode).getUniqueID().toString() + ") has completed challenge.");
            } catch (SQLException e) {
                Utils.logger.log(LogRecord.Level.ERROR, e.toString());
                Utils.server.errorReturn(httpExchange, 500, INTERNAL_ERROR);
            } finally {
                challengeManager.removeChallenge(verificationCode);
            }
        } else {
            //failed
            Utils.server.returnNoContent(httpExchange, 304);
        }
    }
}
