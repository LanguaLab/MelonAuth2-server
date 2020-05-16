package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class SkinServerSettings {

    @SerializedName("dataRoot")
    @Expose
    public String dataRoot;
    @SerializedName("usageLimit")
    @Expose
    public List<Integer> usageLimit = null;


    public static SkinServerSettings getDefault() {
        return new SkinServerSettings().check();
    }

    public SkinServerSettings check() {
        if (dataRoot == null) dataRoot = "./skins";
        dataRoot = Utils.removeSlashAtTheEnd(dataRoot);
        if (usageLimit == null || usageLimit.size() < 2) {
            usageLimit = new ArrayList<>();
            usageLimit.add(20);
            usageLimit.add(60000);
        }
        return this;
    }
}