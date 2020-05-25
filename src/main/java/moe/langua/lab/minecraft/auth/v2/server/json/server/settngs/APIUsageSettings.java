package moe.langua.lab.minecraft.auth.v2.server.json.server.settngs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class APIUsageSettings {

    @SerializedName("getCode")
    @Expose
    private UsageSetting getCode = null;
    @SerializedName("getStatus")
    @Expose
    private UsageSetting getStatus = null;
    @SerializedName("getSkin")
    @Expose
    private UsageSetting getSkin = null;
    @SerializedName("verify")
    @Expose
    private UsageSetting verify = null;

    public static APIUsageSettings getDefault() {
        return new APIUsageSettings().check();
    }

    public APIUsageSettings check() {
        if (getCode == null) getCode = UsageSetting.get(60, 60000);
        if (getStatus == null) getStatus = UsageSetting.get(60, 60000);
        if (getSkin == null) getSkin = UsageSetting.get(200, 60000);
        if (verify == null) verify = UsageSetting.get(1, 60000);
        return this;
    }

    public UsageSetting getGetCode() {
        return getCode;
    }

    public UsageSetting getGetStatus() {
        return getStatus;
    }

    public UsageSetting getGetSkin() {
        return getSkin;
    }

    public UsageSetting getVerify() {
        return verify;
    }
}