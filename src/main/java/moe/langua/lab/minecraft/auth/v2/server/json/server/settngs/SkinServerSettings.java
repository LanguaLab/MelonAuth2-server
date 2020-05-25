package moe.langua.lab.minecraft.auth.v2.server.json.server.settngs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class SkinServerSettings {

    @SerializedName("dataRoot")
    @Expose
    private String dataRoot;
    @SerializedName("usageLimit")
    @Expose
    private List<Integer> usageLimit = null;


    public static SkinServerSettings getDefault() {
        return new SkinServerSettings().check();
    }

    public SkinServerSettings check() {
        if (dataRoot == null) dataRoot = "./skins";
        dataRoot = Utils.removeSlashAtTheEnd(dataRoot);
        if (usageLimit == null || usageLimit.size() < 2) {
            usageLimit = new ArrayList<>();
            usageLimit.add(100);
            usageLimit.add(10000);
        }
        return this;
    }

    public String getDataRoot() {
        return dataRoot;
    }

    public List<Integer> getUsageLimit() {
        return new ArrayList<>(usageLimit);
    }
}