package moe.langua.lab.minecraft.auth.v2.server.json.mojang;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SKIN {

    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("metadata")
    @Expose
    public Metadata metadata;

}