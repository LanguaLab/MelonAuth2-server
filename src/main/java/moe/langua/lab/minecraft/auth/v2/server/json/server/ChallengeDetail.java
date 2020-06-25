package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.server.util.Challenge;

public class ChallengeDetail {

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

    public ChallengeDetail() {
    }

    public ChallengeDetail(Challenge challenge, String requestURL) {
        this.uuid = challenge.getUniqueID().toString();
        this.name = challenge.getPlayerName();
        this.expireIn = challenge.getExpireTime() - System.currentTimeMillis();
        this.url = requestURL + challenge.getUrlPath();
        this.skinModel = challenge.getSkinModel();
    }

    public ChallengeDetail(String uuid, String name, Long expireIn, String url, String skinModel) {
        this.uuid = uuid;
        this.name = name;
        this.expireIn = expireIn;
        this.url = url;
        this.skinModel = skinModel;
    }
}