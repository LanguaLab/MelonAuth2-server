package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Config {
    public static Config instance;

    @SerializedName("secretKey")
    @Expose
    public String secretKey;
    @SerializedName("APIUrl")
    @Expose
    public String aPIUrl;
    @SerializedName("CORSList")
    @Expose
    public List<String> CORSList;
    @SerializedName("databaseSettings")
    @Expose
    public DatabaseSettings databaseSettings;
    @SerializedName("skinServerSettings")
    @Expose
    public SkinServerSettings skinServerSettings;
    @SerializedName("verificationExpireTime")
    @Expose
    public Long verificationExpireTime;
    @SerializedName("verificationRegenTime")
    @Expose
    public Long verificationRegenTime;
    @SerializedName("verificationPublicAPIUsageLimit")
    @Expose
    public VerificationPublicAPIUsageLimit verificationPublicAPIUsageLimit;
    @SerializedName("minecraftServerFailedAttempts")
    @Expose
    public List<Integer> minecraftServerFailedAttempts = null;
    @SerializedName("minimumLogRecordLevel")
    @Expose
    public String minimumLogRecordLevel;
    @SerializedName("sourceCode")
    @Expose
    public String sourceCode;

    public static Config getDefault() {
        Utils.logger.log(LogRecord.Level.INFO, "First startup detected. Generating default config file...");
        Config config = new Config();
        config.secretKey = UUID.randomUUID().toString();
        config.aPIUrl = "http://127.0.0.1:11014";
        config.CORSList = new ArrayList<>();
        config.skinServerSettings = SkinServerSettings.getDefault();
        config.verificationExpireTime = 1800000L;
        config.verificationRegenTime = 900000L;
        config.verificationPublicAPIUsageLimit = VerificationPublicAPIUsageLimit.getDefault();
        config.minecraftServerFailedAttempts = new ArrayList<>();
        config.minecraftServerFailedAttempts.add(1);
        config.minecraftServerFailedAttempts.add(60000);
        config.minimumLogRecordLevel = "fine";
        config.sourceCode = "";
        return config;
    }

    public void check() {
        if (secretKey == null) secretKey = UUID.randomUUID().toString();
        if (aPIUrl == null) aPIUrl = "http://127.0.0.1:11014";
        aPIUrl = Utils.removeSlashAtTheEnd(aPIUrl);
        if (CORSList == null) CORSList = new ArrayList<>();
        for (int index = 0; index < CORSList.size(); index++) {
            CORSList.set(index, Utils.removeSlashAtTheEnd(CORSList.get(index)));
        }
        if (skinServerSettings == null) skinServerSettings = SkinServerSettings.getDefault();
        if (verificationPublicAPIUsageLimit == null)
            verificationPublicAPIUsageLimit = VerificationPublicAPIUsageLimit.getDefault();
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
        if (sourceCode == null) sourceCode = "";
        skinServerSettings.check();
        verificationPublicAPIUsageLimit.check();
    }

}