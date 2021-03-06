package moe.langua.lab.minecraft.auth.v2.server.util;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.UUID;

public class Challenge {
    static private final Random random = new Random();

    private final UUID uniqueID;
    private final String playerName;
    private final String skinModel;
    private final int[] verificationCode;
    private final long expireTime;
    private final String urlPath;

    public Challenge(UUID uniqueID, String playerName, String skinModel, int[] verificationCode, long expireTime, String urlPath) {
        this.uniqueID = uniqueID;
        this.playerName = playerName;
        this.skinModel = skinModel;
        this.verificationCode = verificationCode;
        this.expireTime = expireTime;
        this.urlPath = urlPath;
    }

    public UUID getUniqueID() {
        return uniqueID;
    }

    public int[] getVerificationCode() {
        return verificationCode;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public String getSkinModel() {
        return skinModel;
    }

    public boolean verify(BufferedImage paintedSkin) {
        int colorIndex;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                colorIndex = verificationCode[(y * 8) + x];
                if (paintedSkin.getRGB(x, y) != Utils.colorTable[colorIndex]) return false;
            }
        }
        return true;
    }

}
