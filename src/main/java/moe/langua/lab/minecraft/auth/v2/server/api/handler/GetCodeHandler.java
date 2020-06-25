package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.server.ChallengeDetail;
import moe.langua.lab.minecraft.auth.v2.server.util.Challenge;
import moe.langua.lab.minecraft.auth.v2.server.util.ChallengeManager;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

import java.net.InetAddress;

public class GetCodeHandler extends AbstractHandler {
    private final ChallengeManager challengeManager;

    public GetCodeHandler(long limit, long periodInMilliseconds, HttpServer httpServer, String handlePath, ChallengeManager challengeManager) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
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
        Challenge challenge = challengeManager.getChallenge(verificationCode);
        ChallengeDetail challengeDetail = new ChallengeDetail(challenge, httpExchange.getRequestHeaders().getFirst("X-Forwarded-Proto") + "://" + httpExchange.getRequestHeaders().getFirst("X-Forwarded-Host"));
        Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(challengeDetail));
    }
}
