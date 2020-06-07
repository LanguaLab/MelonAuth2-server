package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChallengeOverview {

    @SerializedName("challengeID")
    @Expose
    private int challengeID;
    @SerializedName("expireIn")
    @Expose
    private Long expireIn;

    public ChallengeOverview(int challengeID, Long expireIn) {
        this.challengeID = challengeID;
        this.expireIn = expireIn;
    }

    public ChallengeOverview() {
    }

    public int getChallengeID() {
        return challengeID;
    }

    public Long getExpireIn() {
        return expireIn;
    }
}
