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
    @SerializedName("skinServerSettings")
    @Expose
    private SkinServerSettings skinServerSettings;
    @SerializedName("verificationExpireTime")
    @Expose
    private Long verificationExpireTime;
    @SerializedName("verificationRegenTime")
    @Expose
    private Long verificationRegenTime;
    @SerializedName("verificationPublicAPIUsageLimit")
    @Expose
    private APIUsageSettings APIUsageSettings;
    @SerializedName("minecraftServerFailedAttempts")
    @Expose
    private List<Integer> minecraftServerFailedAttempts = null;
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
        if (queueKey == null) queueKey = Utils.getRandomString(16);
        if (proxyKey == null) proxyKey = Utils.getRandomString(16);
        if (aPIUrl == null) aPIUrl = "http://127.0.0.1:11014";
        aPIUrl = Utils.removeSlashAtTheEnd(aPIUrl);
        if (CORSList == null) CORSList = new ArrayList<>();
        for (int index = 0; index < CORSList.size(); index++) {
            CORSList.set(index, Utils.removeSlashAtTheEnd(CORSList.get(index)));
        }
        if (databaseSettings == null) databaseSettings = DatabaseSettings.getDefault();
        if (skinServerSettings == null) skinServerSettings = SkinServerSettings.getDefault();
        if (APIUsageSettings == null)
            APIUsageSettings = APIUsageSettings.getDefault();
        if (verificationExpireTime == null) verificationExpireTime = 1800000L;
        if (verificationRegenTime == null) verificationRegenTime = 900000L;
        if (verificationExpireTime < verificationRegenTime) verificationRegenTime = verificationExpireTime;
        if (minecraftServerFailedAttempts == null || minecraftServerFailedAttempts.size() < 2) {
            minecraftServerFailedAttempts = new ArrayList<>();
            minecraftServerFailedAttempts.add(1);
            minecraftServerFailedAttempts.add(60000);
        }
        if (minimumLogRecordLevel == null) minimumLogRecordLevel = "fine";
        if (LogRecord.Level.getFromName(minimumLogRecordLevel) == null) minimumLogRecordLevel = "fine";
        if (applicationOwner == null) applicationOwner = "LanguaLab";
        if (applicationDescription == null) applicationDescription = "MelonAuth 2 public api";

        databaseSettings.check();
        skinServerSettings.check();
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

    public SkinServerSettings getSkinServerSettings() {
        return skinServerSettings;
    }

    public Long getVerificationExpireTime() {
        return verificationExpireTime;
    }

    public Long getVerificationRegenTime() {
        return verificationRegenTime;
    }

    public APIUsageSettings getAPIUsageSettings() {
        return APIUsageSettings;
    }

    public List<Integer> getMinecraftServerFailedAttempts() {
        return new ArrayList<>(minecraftServerFailedAttempts);
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