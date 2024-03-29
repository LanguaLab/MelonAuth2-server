package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.mojang.Profile;
import moe.langua.lab.minecraft.auth.v2.server.json.server.ChallengeOverview;
import moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.MainSettings;
import moe.langua.lab.minecraft.auth.v2.server.util.Challenge;
import moe.langua.lab.minecraft.auth.v2.server.util.ChallengeManager;
import moe.langua.lab.minecraft.auth.v2.server.util.SkinServer;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

public class RequireHandler extends AbstractHandler {

    private final ChallengeManager challengeManager;
    private final SkinServer skinServer;


    public RequireHandler(long limit, long periodInMilliseconds, HttpServer httpServer, String handlePath, ChallengeManager challengeManager, SkinServer skinServer) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
        this.challengeManager = challengeManager;
        this.skinServer = skinServer;
    }

    @Override
    public void process(HttpExchange httpExchange, InetAddress requestAddress) {
        if (!httpExchange.getRequestHeaders().containsKey("Authorization")) {
            httpExchange.getResponseHeaders().set("WWW-Authenticate", "Basic");
            Utils.server.returnNoContent(httpExchange, 401);
            getLimiter().add(requestAddress, 1);
            return;
        } else {
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
        }

        UUID uniqueID;
        try {
            uniqueID = UUID.fromString(Utils.getLastChild(httpExchange.getRequestURI()));
        } catch (IllegalArgumentException e) {
            Utils.server.errorReturn(httpExchange, 404, Utils.server.NOT_FOUND_ERROR);
            return;
        }

        if (!challengeManager.hasChallenge(uniqueID) || challengeManager.getChallenge(uniqueID).getExpireTime() - System.currentTimeMillis() < MainSettings.instance.getChallengeRegen()/*has no existing verification OR exist verification remains less than regen time*/) {
            challengeManager.removeChallenge(uniqueID);
            //create new verification
            BufferedImage playerSkin;
            String playerName;
            Profile profile;
            try {
                profile = Utils.getPlayerProfile(uniqueID);
                if (profile == null) return;
                playerName = profile.name;
                playerSkin = Utils.getSkinFromProfile(profile);
            } catch (IOException e) {
                Utils.server.errorReturn(httpExchange, 500, Utils.server.SERVER_NETWORK_ERROR);
                Utils.logger.warn(e.toString());
                return;
            }
            int[] verificationCode = Utils.generateRandomVerificationCodeArray();
            Utils.paintVerificationCode(playerSkin, verificationCode);
            String urlPath;
            try {
                urlPath = skinServer.putSkin(playerSkin);
                long expire = System.currentTimeMillis() + MainSettings.instance.getChallengeLife();
                String skinType = Utils.getPlayerSkinModel(profile);
                Challenge challenge = new Challenge(uniqueID, playerName, skinType, verificationCode, expire, urlPath);
                int code = challengeManager.newVerification(uniqueID, challenge);
                ChallengeOverview challengeOverview = new ChallengeOverview(code, MainSettings.instance.getChallengeLife());
                Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(challengeOverview));
                Utils.logger.info("New verification code created: " + code + " for " + profile.name + " (" + uniqueID.toString() + ")");
            } catch (IOException e) {
                Utils.logger.warn(e.toString());
                Utils.server.errorReturn(httpExchange, 500, Utils.server.SERVER_NETWORK_ERROR);
            }
        } else {//send exist verification
            Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(new ChallengeOverview(challengeManager.getChallengeID(uniqueID), challengeManager.getChallenge(uniqueID).getExpireTime() - System.currentTimeMillis())));
        }
    }
}
