package moe.langua.lab.minecraft.auth.v2.server.util;


import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class PassManager {
    private final ConcurrentHashMap<String, String> secretKeyMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ArrayList<Pattern>> secretIPListMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> queueKeyMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ArrayList<Pattern>> queueIPListMap = new ConcurrentHashMap<>();

    public PassManager(List<String> secretKeyList, List<String> queueKeyList) {
        for (String x : secretKeyList) {
            String[] split = x.split(":");
            if (split.length < 3) throw new IllegalArgumentException(x + " is not a legal secretKey setting.");
            secretKeyMap.put(split[1], split[0]);
            ArrayList<Pattern> patterns = new ArrayList<>();
            for (int index = 0; index < split.length - 2; index++) {
                patterns.add(Pattern.compile(split[2 + index]));
            }
            secretIPListMap.put(split[1], patterns);
        }
        for (String x : queueKeyList) {
            String[] split = x.split(":");
            if (split.length < 3) throw new IllegalArgumentException(x + " is not a legal queueKey setting.");
            queueKeyMap.put(split[1], split[0]);
            ArrayList<Pattern> patterns = new ArrayList<>();
            for (int index = 0; index < split.length - 2; index++) {
                patterns.add(Pattern.compile(split[2 + index]));
            }
            queueIPListMap.put(split[1], patterns);
        }
    }

    private boolean verifyQueue(String serverName, String secret, InetAddress address) {
        if (!queueKeyMap.containsKey(serverName)) return false;
        if (!queueKeyMap.get(serverName).equals(secret)) return false;
        for (Pattern x : queueIPListMap.get(serverName)) {
            if (x.matcher(address.getHostAddress()).matches()) return true;
        }
        return false;
    }

    private boolean verifySecret(String serverName, String secret, InetAddress address) {
        if (!secretKeyMap.containsKey(serverName)) return false;
        if (!secretKeyMap.get(serverName).equals(secret)) return false;
        for (Pattern x : secretIPListMap.get(serverName)) {
            if (x.matcher(address.getHostAddress()).matches()) return true;
        }
        return false;
    }

    public boolean verifyQueue(String base64String, InetAddress address) {
        byte[] decoded = Base64.getDecoder().decode(base64String);
        String[] spited = new String(decoded, StandardCharsets.UTF_8).split(":");
        if (spited.length < 2) return false;
        return verifyQueue(spited[0], spited[1], address);
    }

    public boolean verifySecret(String base64String, InetAddress address) {
        byte[] decoded = Base64.getDecoder().decode(base64String);
        String[] spited = new String(decoded, StandardCharsets.UTF_8).split(":");
        if (spited.length < 2) return false;
        return verifySecret(spited[0], spited[1], address);
    }

    public String generateQueueAuthorizationHeader(String serverName) {
        if (!queueKeyMap.containsKey(serverName)) return null;
        String authorizationString = serverName + ":" + queueKeyMap.get(serverName);
        byte[] encodedAuthorization = Base64.getEncoder().encode(authorizationString.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuthorization, StandardCharsets.UTF_8);
    }

    public String generateSecretAuthorizationHeader(String serverName) {
        if (!secretKeyMap.containsKey(serverName)) return null;
        String authorizationString = serverName + ":" + secretKeyMap.get(serverName);
        byte[] encodedAuthorization = Base64.getEncoder().encode(authorizationString.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuthorization, StandardCharsets.UTF_8);
    }
}
