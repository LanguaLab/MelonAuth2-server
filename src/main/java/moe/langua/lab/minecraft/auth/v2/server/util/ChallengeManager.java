package moe.langua.lab.minecraft.auth.v2.server.util;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChallengeManager {
    private final Random random = new Random(System.currentTimeMillis() - 111111);
    private final ConcurrentHashMap<Integer, Challenge> challengeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> playerUniqueIDMap = new ConcurrentHashMap<>();

    private void setVerification(UUID uniqueID, Integer id, Challenge challenge) {
        playerUniqueIDMap.put(uniqueID, id);
        challengeMap.put(id, challenge);
    }

    public int newVerification(UUID uniqueID, Challenge challenge) {
        int id = random.nextInt(999999);
        while (hasChallenge(id++)) {
            if (hasChallenge(id) && challengeMap.get(id).isExpired()) {
                challengeMap.remove(id);
                break;
            }
        }
        setVerification(uniqueID, id, challenge);
        return id;
    }

    public Challenge getChallenge(int id) {
        if (!hasChallenge(id)) return null;
        return challengeMap.get(id);
    }

    public Challenge getChallenge(UUID uniqueID) {
        if (!hasChallenge(uniqueID)) return null;
        return challengeMap.get(playerUniqueIDMap.get(uniqueID));
    }

    public void removeChallenge(int id) {
        if (!hasChallenge(id)) return;
        playerUniqueIDMap.remove(challengeMap.get(id).getUniqueID());
        challengeMap.remove(id);
    }

    public void removeChallenge(UUID uniqueID) {
        if (!playerUniqueIDMap.containsKey(uniqueID)) return;
        challengeMap.remove(playerUniqueIDMap.get(uniqueID));
        playerUniqueIDMap.remove(uniqueID);
    }

    public int getChallengeID(UUID uniqueID) {
        return playerUniqueIDMap.get(uniqueID);
    }

    public boolean hasChallenge(int id) {
        return challengeMap.containsKey(id);
    }

    public boolean hasChallenge(UUID uniqueID) {
        return playerUniqueIDMap.containsKey(uniqueID);
    }
}
