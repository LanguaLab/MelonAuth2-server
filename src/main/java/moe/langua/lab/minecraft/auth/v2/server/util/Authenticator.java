package moe.langua.lab.minecraft.auth.v2.server.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Authenticator {
    private final String mostSignificantBits;
    private final String leastSignificantBits;
    private String[] passWords = new String[3];
    private final MessageDigest digest = MessageDigest.getInstance("SHA-256");
    private long lastUpdate = 0;

    public Authenticator(UUID uniqueID) throws NoSuchAlgorithmException {
        mostSignificantBits = Long.toHexString(uniqueID.getMostSignificantBits());
        leastSignificantBits = Long.toHexString(uniqueID.getLeastSignificantBits());
    }

    private long[] getTimeArray() {
        long now = System.currentTimeMillis();
        long[] timeArray = new long[3];
        long basic = now / 100000; /* 100 seconds */
        timeArray[0] = (basic - 1) * 100000;
        timeArray[1] = basic * 100000;
        timeArray[2] = (basic + 1) * 100000;
        return timeArray;
    }

    private String[] getPassWords() {
        long now = System.currentTimeMillis();
        if (now - lastUpdate > 100000) {
            long[] timeArray = getTimeArray();
            String passWord1 = leastSignificantBits + " " + new Date(timeArray[0]).toString() + " " + mostSignificantBits;
            String passWord2 = leastSignificantBits + " " + new Date(timeArray[1]).toString() + " " + mostSignificantBits;
            String passWord3 = leastSignificantBits + " " + new Date(timeArray[2]).toString() + " " + mostSignificantBits;
            passWords = new String[]{digestOfDigest(passWord1), digestOfDigest(passWord2), digestOfDigest(passWord3)};
            lastUpdate = now;
        }
        return passWords;
    }

    private String digestOfDigest(String input) {
        byte[] hash = digest.digest(input.getBytes(UTF_8));
        return "" + Integer.toHexString(Byte.toUnsignedInt(hash[3])) + Integer.toHexString(Byte.toUnsignedInt(hash[7])) + Integer.toHexString(Byte.toUnsignedInt(hash[11])) + Integer.toHexString(Byte.toUnsignedInt(hash[15])) + Integer.toHexString(Byte.toUnsignedInt(hash[19])) + Integer.toHexString(Byte.toUnsignedInt(hash[23])) + Integer.toHexString(Byte.toUnsignedInt(hash[27])) + Integer.toHexString(Byte.toUnsignedInt(hash[31]));
    }

    public boolean verify(String key){
        synchronized (this){
            for (String passWord : getPassWords()) {
                if(passWord.equals(key)) return true;
            }
            return false;
        }
    }
}
