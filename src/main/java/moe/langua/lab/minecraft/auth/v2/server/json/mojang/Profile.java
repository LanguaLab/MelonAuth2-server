package moe.langua.lab.minecraft.auth.v2.server.json.mojang;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Profile {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("properties")
    @Expose
    public List<Property> properties = null;

}