package moe.langua.lab.minecraft.auth.v2.server.util;

public class Database {
    private String address;
    private int port;
    private SQL_TYPE type;

    public Database(String address, int port, SQL_TYPE type) {
        this.address = address;
        this.port = port;
        this.type = type;
    }

    public Database() {
    }

    private enum SQL_TYPE {
        SQLite, MYSQL
    }
}
