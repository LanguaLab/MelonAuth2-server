package moe.langua.lab.minecraft.auth.v2.server.sql;

import java.net.InetAddress;
import java.util.UUID;

public interface DataSearcher {

    int getPlayerStatus(UUID uniqueID);

    boolean setPlayerStatus(UUID uniqueID, int status, InetAddress commitAddress);
}
