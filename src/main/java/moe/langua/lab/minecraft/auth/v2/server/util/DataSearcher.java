package moe.langua.lab.minecraft.auth.v2.server.util;

import java.util.UUID;

public class DataSearcher {
    Database database;

    public DataSearcher(Database database) {
        this.database = database;
    }

    public int getPlayerStatus(UUID uniqueID) {
        return 0;
    }

    public boolean setPlayerStatus(UUID uniqueID, int status) {
        return true;
    }
}
