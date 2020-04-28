package moe.langua.lab.minecraft.auth.v2.server.json.mojang;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlayerProfile {

    @SerializedName("timestamp")
    @Expose
    public Long timestamp;
    @SerializedName("profileId")
    @Expose
    public String profileId;
    @SerializedName("profileName")
    @Expose
    public String profileName;
    @SerializedName("textures")
    @Expose
    public Textures textures;

}