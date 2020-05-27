package moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.sql;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

public class MySQLSettings {
    @SerializedName("host")
    @Expose
    private String host;
    @SerializedName("port")
    @Expose
    private Integer port;
    @SerializedName("database")
    @Expose
    private String database;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("tablePrefix")
    @Expose
    private String tablePrefix;

    public String getTablePrefix() {
        return tablePrefix;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public MySQLSettings check(){
        if (host == null) host = "127.0.0.1";
        if (port == null) port = 3306;
        if (database == null) database = "databaseName";
        if (username == null) username = "authv2";
        if (password == null) password = Utils.getRandomString(12);
        if (tablePrefix == null) tablePrefix = "AuthV2_";
        return this;
    }

    public static MySQLSettings getDefault(){
        return new MySQLSettings().check();
    }
}
