package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.mojang.Profile;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Config;
import moe.langua.lab.minecraft.auth.v2.server.json.server.VerificationNotice;
import moe.langua.lab.minecraft.auth.v2.server.util.*;
import moe.langua.lab.security.otp.MelonTOTP;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.UUID;

public class GetUUIDStatusHandler extends AbstractHandler {
    private final DataSearcher dataSearcher;
    private final VerificationCodeManager verificationCodeManager;
    private final SkinServer skinServer;
    private final MelonTOTP totpServer;

    public GetUUIDStatusHandler(int limit, long periodInMilliseconds, HttpServer httpServer, String handlePath, DataSearcher dataSearcher, VerificationCodeManager verificationCodeManager, SkinServer skinServer) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
        this.dataSearcher = dataSearcher;
        this.verificationCodeManager = verificationCodeManager;
        this.skinServer = skinServer;
        this.totpServer = new MelonTOTP(Config.instance.secretKey.getBytes(),0x100000000L,30000);
    }

    @Override
    public void process(HttpExchange httpExchange, InetAddress requestAddress) {
        if(!httpExchange.getRequestHeaders().containsKey("Authorization")){
            httpExchange.getResponseHeaders().set("WWW-Authenticate","MelonTOTP Pass required");
            Utils.server.returnNoContent(httpExchange,401);
            getLimiter().add(requestAddress,1);
            return;
        }else{
            long pass;
            try {
                pass = Long.parseLong(httpExchange.getRequestHeaders().getFirst("Authorization"), 16);
            }catch (NumberFormatException e){
                Utils.server.returnNoContent(httpExchange,403);
                getLimiter().add(requestAddress,1);
                return;
            }
            if(!totpServer.verify(pass)){
                Utils.server.returnNoContent(httpExchange,403);
                getLimiter().add(requestAddress,1);
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
        if (dataSearcher.getPlayerStatus(uniqueID) == 0) {// block
            if (!verificationCodeManager.hasVerification(uniqueID) || verificationCodeManager.getVerification(uniqueID).getExpireTime() - System.currentTimeMillis() < Config.instance.verificationRegenTime/*has no existing verification OR exist verification remains less than regen time*/) {
                verificationCodeManager.removeVerification(uniqueID);
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
                    Utils.logger.log(LogRecord.Level.WARN, e.toString());
                    return;
                }
                int[] verificationCode = Utils.generateRandomVerificationCodeArray();
                Utils.paintVerificationCode(playerSkin, verificationCode);
                String url;
                try {
                    url = skinServer.putSkin(playerSkin);
                    long expire = System.currentTimeMillis() + Config.instance.verificationExpireTime;
                    String skinType = Utils.getPlayerSkinModel(profile);
                    Verification verification = new Verification(uniqueID, playerName, skinType, verificationCode, expire, new URL(url));
                    int code = verificationCodeManager.newVerification(uniqueID, verification);
                    VerificationNotice verificationNotice = new VerificationNotice(code, Config.instance.verificationExpireTime);
                    Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(verificationNotice));
                    Utils.logger.log(LogRecord.Level.INFO, "New verification code created: " + code + " for " + profile.name + " (" + uniqueID.toString() + ")");
                } catch (IOException e) {
                    Utils.logger.log(LogRecord.Level.WARN, e.toString());
                    Utils.server.errorReturn(httpExchange, 500, Utils.server.SERVER_NETWORK_ERROR);
                }
            } else {//send exist verification
                Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(new VerificationNotice(verificationCodeManager.getVerificationCode(uniqueID), verificationCodeManager.getVerification(uniqueID).getExpireTime() - System.currentTimeMillis())));
                Utils.logger.log(LogRecord.Level.FINE, "Verification code request: " + verificationCodeManager.getVerificationCode(uniqueID) + "(" + uniqueID.toString() + ")");
            }
        } else {//pass
            Utils.server.returnNoContent(httpExchange, 204);
        }
    }
}
