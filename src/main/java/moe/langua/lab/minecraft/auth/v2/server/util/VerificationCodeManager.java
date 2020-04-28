package moe.langua.lab.minecraft.auth.v2.server.util;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VerificationCodeManager {
    private final Random random = new Random(System.currentTimeMillis() - 111111);
    private final ConcurrentHashMap<Integer, Verification> verificationCodeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> playerUniqueIDMap = new ConcurrentHashMap<>();

    private void setVerification(UUID uniqueID, Integer id, Verification verification) {
        playerUniqueIDMap.put(uniqueID, id);
        verificationCodeMap.put(id, verification);
    }

    public int newVerification(UUID uniqueID, Verification verification) {
        int id = random.nextInt(899999) + 100000;
        while (hasVerification(id++)) {
            if (hasVerification(id) && verificationCodeMap.get(id).isExpired()) {
                verificationCodeMap.remove(id);
                break;
            }
        }
        setVerification(uniqueID, id, verification);
        return id;
    }

    public Verification getVerification(int id) {
        if (!hasVerification(id)) return null;
        return verificationCodeMap.get(id);
    }

    public Verification getVerification(UUID uniqueID) {
        if (!hasVerification(uniqueID)) return null;
        return verificationCodeMap.get(playerUniqueIDMap.get(uniqueID));
    }

    public void removeVerification(int id) {
        if (!hasVerification(id)) return;
        playerUniqueIDMap.remove(verificationCodeMap.get(id).getUniqueID());
        verificationCodeMap.remove(id);
    }

    public void removeVerification(UUID uniqueID) {
        if (!playerUniqueIDMap.containsKey(uniqueID)) return;
        verificationCodeMap.remove(playerUniqueIDMap.get(uniqueID));
        playerUniqueIDMap.remove(uniqueID);
    }

    public int getVerificationCode(UUID uniqueID) {
        return playerUniqueIDMap.get(uniqueID);
    }

    public boolean hasVerification(int id) {
        return verificationCodeMap.containsKey(id);
    }

    public boolean hasVerification(UUID uniqueID) {
        return playerUniqueIDMap.containsKey(uniqueID);
    }
}
