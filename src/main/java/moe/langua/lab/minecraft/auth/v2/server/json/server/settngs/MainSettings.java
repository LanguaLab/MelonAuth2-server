package moe.langua.lab.minecraft.auth.v2.server.json.server.settngs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.util.ArrayList;
import java.util.List;

public class MainSettings {
    public static MainSettings instance;

    @SerializedName("clientKey")
    @Expose
    private String clientKey;
    @SerializedName("queueKey")
    @Expose
    private String queueKey;
    @SerializedName("proxyKey")
    @Expose
    private String proxyKey;
    @SerializedName("APIUrl")
    @Expose
    private String aPIUrl;
    @SerializedName("CORSList")
    @Expose
    private List<String> CORSList;
    @SerializedName("databaseSettings")
    @Expose
    private DatabaseSettings databaseSettings;
    @SerializedName("skinBase")
    @Expose
    private String skinBase;
    @SerializedName("challengeLife")
    @Expose
    private Long challengeLife;
    @SerializedName("challengeRegen")
    @Expose
    private Long challengeRegen;
    @SerializedName("APIUsageSettings")
    @Expose
    private APIUsageSettings APIUsageSettings;
    @SerializedName("clientAuthenticationFailed")
    @Expose
    private UsageSetting clientAuthenticationFailed = null;
    @SerializedName("minimumLogRecordLevel")
    @Expose
    private String minimumLogRecordLevel;
    @SerializedName("applicationOwner")
    @Expose
    private String applicationOwner;
    @SerializedName("applicationDescription")
    @Expose
    private String applicationDescription;

    public static MainSettings getDefault() {
        Utils.logger.log(LogRecord.Level.INFO, "First startup detected. Generating default config file...");
        return new MainSettings().check();
    }

    public MainSettings check() {
        if (clientKey == null) clientKey = Utils.getRandomString(64);
        if (queueKey == null) queueKey = Utils.getRandomString(24);
        if (proxyKey == null) proxyKey = Utils.getRandomString(24);
        if (aPIUrl == null) aPIUrl = "http://127.0.0.1:11014";
        aPIUrl = Utils.removeSlashAtTheEnd(aPIUrl);
        if (CORSList == null) CORSList = new ArrayList<>();
        for (int index = 0; index < CORSList.size(); index++) {
            CORSList.set(index, Utils.removeSlashAtTheEnd(CORSList.get(index)));
        }
        if (databaseSettings == null) databaseSettings = DatabaseSettings.getDefault();
        if (skinBase == null) skinBase = "./skins";
        if (APIUsageSettings == null)
            APIUsageSettings = moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.APIUsageSettings.getDefault();
        if (challengeLife == null) challengeLife = 1800000L;
        if (challengeRegen == null) challengeRegen = 900000L;
        if (challengeLife < challengeRegen) challengeRegen = challengeLife;
        if (clientAuthenticationFailed == null) clientAuthenticationFailed = UsageSetting.get(1, 60000);
        if (minimumLogRecordLevel == null || LogRecord.Level.getFromName(minimumLogRecordLevel) == null)
            minimumLogRecordLevel = "fine";
        if (applicationOwner == null) applicationOwner = "LanguaLab";
        if (applicationDescription == null) applicationDescription = "MelonAuth v2 Public API";

        databaseSettings.check();
        APIUsageSettings.check();

        return this;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getProxyKey() {
        return proxyKey;
    }

    public String getQueueKey() {
        return queueKey;
    }

    public String getAPIUrl() {
        return aPIUrl;
    }

    public List<String> getCORSList() {
        return new ArrayList<>(CORSList);
    }

    public DatabaseSettings getDatabaseSettings() {
        return databaseSettings;
    }

    public String getSkinBase() {
        return skinBase;
    }

    public Long getChallengeLife() {
        return challengeLife;
    }

    public Long getChallengeRegen() {
        return challengeRegen;
    }

    public APIUsageSettings getAPIUsageSettings() {
        return APIUsageSettings;
    }

    public UsageSetting getClientAuthenticationFailed() {
        return clientAuthenticationFailed;
    }

    public String getMinimumLogRecordLevel() {
        return minimumLogRecordLevel;
    }

    public String getApplicationOwner() {
        return applicationOwner;
    }

    public String getApplicationDescription() {
        return applicationDescription;
    }
}