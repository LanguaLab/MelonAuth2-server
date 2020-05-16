package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

public class DatabaseSettings {

    @SerializedName("tablePrefix")
    @Expose
    public String tablePrefix;
    @SerializedName("useMySQL")
    @Expose
    public Boolean useMySQL;
    @SerializedName("mysqlHost")
    @Expose
    public String mysqlHost;
    @SerializedName("mysqlPort")
    @Expose
    public Integer mysqlPort;
    @SerializedName("mysqlDatabase")
    @Expose
    public String mysqlDatabase;
    @SerializedName("mysqlUsername")
    @Expose
    public String mysqlUsername;
    @SerializedName("mysqlPassword")
    @Expose
    public String mysqlPassword;

    public DatabaseSettings check(){
        if(tablePrefix==null) tablePrefix = "AuthV2_";
        if(useMySQL==null) useMySQL = false;
        if(mysqlHost==null) mysqlHost = "127.0.0.1";
        if(mysqlPort==null) mysqlPort = 3306;
        if(mysqlDatabase==null) mysqlDatabase = "databaseName";
        if(mysqlUsername==null) mysqlUsername = "authv2";
        if(mysqlPassword==null) mysqlPassword = Utils.getRandomString(10);
        return this;
    }

    public static DatabaseSettings getDefault(){
        return new DatabaseSettings().check();
    }

}