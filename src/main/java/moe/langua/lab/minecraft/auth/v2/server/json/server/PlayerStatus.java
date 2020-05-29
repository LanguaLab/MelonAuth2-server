package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class PlayerStatus {
    @SerializedName("uniqueID")
    @Expose
    private String uniqueID;
    @SerializedName("verified")
    @Expose
    private Boolean verified;
    @SerializedName("commitTime")
    @Expose
    private Long commitTime;

    public static PlayerStatus get(UUID uniqueID, boolean verified, Long commitTime) {
        PlayerStatus statusInstance = new PlayerStatus();
        statusInstance.uniqueID = uniqueID.toString();
        statusInstance.verified = verified;
        statusInstance.commitTime = commitTime;
        return statusInstance;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public Boolean getVerified() {
        return verified;
    }

    public long getCommitTime(){
        return commitTime;
    }

}