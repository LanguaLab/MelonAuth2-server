package moe.langua.lab.minecraft.auth.v2.server.json.mojang;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Metadata {

    @SerializedName("model")
    @Expose
    public String model;

}
