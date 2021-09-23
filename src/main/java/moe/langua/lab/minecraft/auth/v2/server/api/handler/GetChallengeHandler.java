package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.server.ChallengeDetail;
import moe.langua.lab.minecraft.auth.v2.server.util.Challenge;
import moe.langua.lab.minecraft.auth.v2.server.util.ChallengeManager;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.UUID;

public class GetChallengeHandler extends AbstractHandler {
    private final ChallengeManager challengeManager;

    public GetChallengeHandler(long limit, long periodInMilliseconds, HttpServer httpServer, String handlePath, ChallengeManager challengeManager) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
        this.challengeManager = challengeManager;
    }

    @Override
    public void process(HttpExchange httpExchange, @NotNull InetAddress requestAddress) {
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
                /*uuidMode = false;*/
            }
        } else {
            getLimiter().add(requestAddress, 1);
        }

        int verificationCode = 0;
        if (!uuidMode) {
            try {
                verificationCode = Integer.parseInt(Utils.getLastChild(httpExchange.getRequestURI()));
            } catch (NumberFormatException e) {
                return;
            }
        }

        if (uuidMode ? !challengeManager.hasChallenge(playerUniqueID) : !challengeManager.hasChallenge(verificationCode)) {
            return;
        } else if (uuidMode ? challengeManager.getChallenge(playerUniqueID).isExpired() : challengeManager.getChallenge(verificationCode).isExpired()) {
            if (uuidMode) {
                challengeManager.getChallenge(playerUniqueID);
            } else {
                challengeManager.removeChallenge(verificationCode);
            }
            return;
        }
        Challenge challenge = uuidMode ? challengeManager.getChallenge(playerUniqueID) : challengeManager.getChallenge(verificationCode);
        ChallengeDetail challengeDetail = new ChallengeDetail(challenge, httpExchange.getRequestHeaders().getFirst("X-Forwarded-Proto") + "://" + httpExchange.getRequestHeaders().getFirst("X-Forwarded-Host"));
        Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(challengeDetail));
    }
}
