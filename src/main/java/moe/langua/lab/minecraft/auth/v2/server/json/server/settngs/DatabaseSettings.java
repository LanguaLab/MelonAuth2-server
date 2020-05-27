package moe.langua.lab.minecraft.auth.v2.server.json.server.settngs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.sql.MySQLSettings;
import moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.sql.SQLiteSettings;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

public class DatabaseSettings {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("sQLiteSettings")
    @Expose
    private SQLiteSettings sqLiteSettings = null;
    @SerializedName("mySQLSettings")
    @Expose
    private MySQLSettings mySQLSettings = null;


    public static DatabaseSettings getDefault() {
        return new DatabaseSettings().check();
    }

    public DatabaseSettings check() {
        if (type == null) type = "SQLite";
        if(sqLiteSettings==null) sqLiteSettings = SQLiteSettings.getDefault();
        if(mySQLSettings==null) mySQLSettings = MySQLSettings.getDefault();
        sqLiteSettings.check();
        mySQLSettings.check();
        return this;
    }

    public String getSQLType() {
        return type;
    }

    public SQLiteSettings getSqLiteSettings() {
        return sqLiteSettings;
    }

    public MySQLSettings getMySQLSettings() {
        return mySQLSettings;
    }
}
