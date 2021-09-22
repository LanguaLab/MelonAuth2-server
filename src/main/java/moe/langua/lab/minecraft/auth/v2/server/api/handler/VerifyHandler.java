package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.api.Limiter;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Message;
import moe.langua.lab.minecraft.auth.v2.server.sql.DataSearcher;
import moe.langua.lab.minecraft.auth.v2.server.util.Challenge;
import moe.langua.lab.minecraft.auth.v2.server.util.ChallengeManager;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

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
        boolean uuidMode = false;
        UUID playerUniqueID = null;
        if (httpExchange.getRequestHeaders().containsKey("Authorization")) {
            String[] pass = httpExchange.getRequestHeaders().getFirst("Authorization").split(" ");
            boolean passed = false;
            if (pass.length >= 2) {
                passed = Utils.passManager.verifySecret(pass[1], requestAddress);
            }
            if (!passed) {
                Utils.server.returnNoContent(httpExchange, 403);
                getLimiter().add(requestAddress, 1);
                return;
            }
            try {
                playerUniqueID = UUID.fromString(Utils.getLastChild(httpExchange.getRequestURI()));
                uuidMode = true;
            } catch (IllegalArgumentException ignore) {

            }
        } else {
            getLimiter().add(requestAddress, 1);
        }

        int verificationCode = 0;
        if (!uuidMode) {
            try {
                verificationCode = Integer.parseInt(Utils.getLastChild(httpExchange.getRequestURI()));
            } catch (NumberFormatException ignore) {
                return;
            }
        }


        Challenge challenge;
        if (uuidMode) {
            if (!challengeManager.hasChallenge(playerUniqueID)) {
                return;
            } else if ((challenge = challengeManager.getChallenge(playerUniqueID)).isExpired()) {
                challengeManager.removeChallenge(playerUniqueID);
                return;
            }
        } else {
            if (!challengeManager.hasChallenge(verificationCode)) {
                return;
            } else if ((challenge = challengeManager.getChallenge(verificationCode)).isExpired()) {
                challengeManager.removeChallenge(verificationCode);
                return;
            }
        }

        BufferedImage skin;
        playerUniqueID = challenge.getUniqueID();
        if (!uuidLimiter.getUsability(playerUniqueID)) {
            Utils.server.errorReturn(httpExchange, 429, Utils.server.TOO_MANY_REQUEST_ERROR.clone().setErrorMessage("only the first request will be proceed each minute per unique id").setExtra("" + (uuidLimiter.getNextReset() - System.currentTimeMillis())));
            return;
        }
        uuidLimiter.add(playerUniqueID, 1);
        try {
            skin = Utils.getSkin(playerUniqueID);
        } catch (IOException e) {
            Utils.logger.warn(e.toString());
            Utils.server.errorReturn(httpExchange, 500, SERVER_NETWORK_ERROR);
            return;
        }
        if (challenge.verify(skin)) {
            //success
            try {
                dataSearcher.setPlayerStatus(playerUniqueID, true, requestAddress);
                Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(Message.getFromString("Your account has been verified")));
                Utils.logger.info(challenge.getPlayerName() + " (" + challenge.getUniqueID().toString() + ") has completed challenge.");
            } catch (SQLException e) {
                Utils.logger.error(e.toString());
                Utils.server.errorReturn(httpExchange, 500, INTERNAL_ERROR);
            } finally {
                if (uuidMode)
                    challengeManager.removeChallenge(playerUniqueID);
                else
                    challengeManager.removeChallenge(verificationCode);
            }
        } else {
            //failed
            Utils.server.returnNoContent(httpExchange, 304);
        }
    }
}
