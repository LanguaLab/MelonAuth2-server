package moe.langua.lab.minecraft.auth.v2.server.json.server.settngs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

public class DatabaseSettings {

    @SerializedName("tablePrefix")
    @Expose
    private String tablePrefix;
    @SerializedName("useMySQL")
    @Expose
    private Boolean useMySQL;
    @SerializedName("mysqlHost")
    @Expose
    private String mysqlHost;
    @SerializedName("mysqlPort")
    @Expose
    private Integer mysqlPort;
    @SerializedName("mysqlDatabase")
    @Expose
    private String mysqlDatabase;
    @SerializedName("mysqlUsername")
    @Expose
    private String mysqlUsername;
    @SerializedName("mysqlPassword")
    @Expose
    private String mysqlPassword;

    public static DatabaseSettings getDefault() {
        return new DatabaseSettings().check();
    }

    public DatabaseSettings check() {
        if (tablePrefix == null) tablePrefix = "AuthV2_";
        if (useMySQL == null) useMySQL = false;
        if (mysqlHost == null) mysqlHost = "127.0.0.1";
        if (mysqlPort == null) mysqlPort = 3306;
        if (mysqlDatabase == null) mysqlDatabase = "databaseName";
        if (mysqlUsername == null) mysqlUsername = "authv2";
        if (mysqlPassword == null) mysqlPassword = Utils.getRandomString(10);
        return this;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public Boolean getUseMySQL() {
        return useMySQL;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public Integer getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }
}
