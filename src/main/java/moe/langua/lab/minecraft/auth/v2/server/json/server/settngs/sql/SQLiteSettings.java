package moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.sql;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SQLiteSettings {

    @SerializedName("synchronous")
    @Expose
    private Boolean isSynchronous = null;
    @SerializedName("journalMode")
    @Expose
    private String journalMode = null;

    public static SQLiteSettings getDefault() {
        return new SQLiteSettings().check();
    }

    public SQLiteSettings check() {
        if (isSynchronous == null) isSynchronous = true;
        if (journalMode == null) journalMode = "TRUNCATE";
        if (!(journalMode.equalsIgnoreCase("DELETE")
                || journalMode.equalsIgnoreCase("TRUNCATE")
                || journalMode.equalsIgnoreCase("PERSIST")
                || journalMode.equalsIgnoreCase("MEMORY")
                || journalMode.equalsIgnoreCase("WAL")
                || journalMode.equalsIgnoreCase("OFF")))
            journalMode = "TRUNCATE";
        return this;
    }

    public Boolean getSynchronous() {
        return isSynchronous;
    }

    public String getJournalMode() {
        return journalMode;
    }
}
