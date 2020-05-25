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

    public static PlayerStatus get(UUID uniqueID, boolean verified) {
        PlayerStatus statusInstance = new PlayerStatus();
        statusInstance.uniqueID = uniqueID.toString();
        statusInstance.verified = verified;
        return statusInstance;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public Boolean getVerified() {
        return verified;
    }

}