package moe.langua.lab.minecraft.auth.v2.server.json.server.settngs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainSettings {
    private static final Gson prettyGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static MainSettings instance;
    @SerializedName("secretKeyList")
    @Expose
    private List<String> secretKeyList;
    @SerializedName("queueKeyList")
    @Expose
    private List<String> queueKeyList;
    @SerializedName("proxyKey")
    @Expose
    private String proxyKey;
    @SerializedName("CORSList")
    @Expose
    private List<String> CORSList;
    @SerializedName("databaseSettings")
    @Expose
    private DatabaseSettings databaseSettings;
    @SerializedName("skinBase")
    @Expose
    private String skinBase;
    @SerializedName("lifetimeVerification")
    @Expose
    private Boolean lifetimeVerification;
    @SerializedName("verificationLife")
    @Expose
    private Long verificationLife;
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
    @SerializedName("workerThreads")
    @Expose
    private Integer workerThreads;
    @SerializedName("applicationOwner")
    @Expose
    private String applicationOwner;
    @SerializedName("applicationDescription")
    @Expose
    private String applicationDescription;

    public static MainSettings getDefault() {
        Utils.logger.info("First startup detected. Generating default config file...");
        return new MainSettings().check();
    }

    public static MainSettings readFromFile(File settingsFile) throws IOException {
        MainSettings settings;
        if (settingsFile.createNewFile()) {
            settings = MainSettings.getDefault();
        } else if (settingsFile.isFile()) {
            settings = Utils.gson.fromJson(new InputStreamReader(new FileInputStream(settingsFile), StandardCharsets.UTF_8), MainSettings.class);
            settings.check();
        } else {
            throw new IOException(settingsFile.getAbsolutePath() + " should be a file, but found a directory.");
        }
        FileOutputStream configOutputStream = new FileOutputStream(settingsFile, false);
        configOutputStream.write(prettyGson.toJson(settings).getBytes(StandardCharsets.UTF_8));
        configOutputStream.flush();
        configOutputStream.close();
        return settings;
    }

    public MainSettings check() {
        String randomServerName = "Server_" + Utils.getRandomString(8);
        String EVERYTHING = ".*";
        if (secretKeyList == null) {
            secretKeyList = new ArrayList<>();
            secretKeyList.add(Utils.getRandomString(64) + ":" + randomServerName + ":" + EVERYTHING);
        }
        if (queueKeyList == null) {
            queueKeyList = new ArrayList<>();
            queueKeyList.add(Utils.getRandomString(24) + ":" + randomServerName + ":" + EVERYTHING);
        }
        if (proxyKey == null) proxyKey = Utils.getRandomString(24);
        if (CORSList == null) CORSList = new ArrayList<>();
        for (int index = 0; index < CORSList.size(); index++) {
            CORSList.set(index, Utils.removeSlashAtTheEnd(CORSList.get(index)));
        }
        if (databaseSettings == null) databaseSettings = DatabaseSettings.getDefault();
        if (skinBase == null) skinBase = "./skins";
        if (APIUsageSettings == null)
            APIUsageSettings = moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.APIUsageSettings.getDefault();
        if (lifetimeVerification == null) lifetimeVerification = true;
        if (verificationLife == null) verificationLife = 31622400000L; //366 days
        if (challengeLife == null) challengeLife = 1800000L;
        if (challengeRegen == null) challengeRegen = 900000L;
        if (challengeLife < challengeRegen) challengeRegen = challengeLife;
        if (clientAuthenticationFailed == null) clientAuthenticationFailed = UsageSetting.get(1, 60000);
        if (workerThreads == null) workerThreads = Runtime.getRuntime().availableProcessors() / 2 + 1;
        if (applicationOwner == null) applicationOwner = "LanguaLab";
        if (applicationDescription == null) applicationDescription = "MelonAuth v2 Public API";

        databaseSettings.check();
        APIUsageSettings.check();

        return this;
    }

    public List<String> getSecretKeys() {
        return secretKeyList;
    }

    public String getProxyKey() {
        return proxyKey;
    }

    public List<String> getQueueKeys() {
        return queueKeyList;
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

    public String getApplicationOwner() {
        return applicationOwner;
    }

    public Integer getWorkerThreads() {
        return workerThreads;
    }

    public Boolean isLifetimeVerification() {
        return lifetimeVerification;
    }

    public Long getVerificationLife() {
        return verificationLife;
    }

    public String getApplicationDescription() {
        return applicationDescription;
    }
}