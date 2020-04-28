package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DatabaseSettings {

    @SerializedName("useMySQL")
    @Expose
    public String useMySQL;
    @SerializedName("table-prefix")
    @Expose
    public String tablePrefix;
    @SerializedName("mysql-host")
    @Expose
    public String mysqlHost;
    @SerializedName("mysql-port")
    @Expose
    public Integer mysqlPort;
    @SerializedName("mysql-database")
    @Expose
    public String mysqlDatabase;
    @SerializedName("mysql-username")
    @Expose
    public String mysqlUsername;
    @SerializedName("mysql-password")
    @Expose
    public String mysqlPassword;

}