package moe.langua.lab.minecraft.auth.v2.server.sql;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.UUID;

public interface DataSearcher {

    boolean getPlayerStatus(UUID uniqueID) throws SQLException;

    void setPlayerStatus(UUID uniqueID, boolean status, InetAddress commitAddress) throws SQLException;
}
