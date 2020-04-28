package moe.langua.lab.minecraft.auth.v2.server.json.mojang;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Property {

    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("value")
    @Expose
    public String value;

}
