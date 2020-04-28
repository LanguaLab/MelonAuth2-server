package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.server.util.Verification;

public class VerificationCodeDetail {

    @SerializedName("uuid")
    @Expose
    public String uuid;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("expireIn")
    @Expose
    public Long expireIn;
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("skinModel")
    @Expose
    public String skinModel;

    public VerificationCodeDetail() {
    }

    public VerificationCodeDetail(Verification verification) {
        this.uuid = verification.getUniqueID().toString();
        this.name = verification.getPlayerName();
        this.expireIn = verification.getExpireTime() - System.currentTimeMillis();
        this.url = verification.getSkinURL().toString();
        this.skinModel = verification.getSkinModel();
    }

    public VerificationCodeDetail(String uuid, String name, Long expireIn, String url, String skinModel) {
        this.uuid = uuid;
        this.name = name;
        this.expireIn = expireIn;
        this.url = url;
        this.skinModel = skinModel;
    }
}