package moe.langua.lab.minecraft.auth.v2.server.json.server.settngs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class APIUsageSettings {

    @SerializedName("getCode")
    @Expose
    private List<Integer> getCode = null;
    @SerializedName("getStatus")
    @Expose
    private List<Integer> getStatus = null;
    @SerializedName("verify")
    @Expose
    private List<Integer> verify = null;

    public static APIUsageSettings getDefault() {
        return new APIUsageSettings().check();
    }

    public APIUsageSettings check() {
        if (getCode == null || getCode.size() < 2) {
            getCode = new ArrayList<>();
            getCode.add(60);
            getCode.add(60000);
        }
        if (getStatus == null || getStatus.size() < 2) {
            getStatus = new ArrayList<>();
            getStatus.add(60);
            getStatus.add(60000);
        }
        if (verify == null || verify.size() < 2) {
            verify = new ArrayList<>();
            verify.add(1);
            verify.add(60000);
        }
        return this;
    }


    public List<Integer> getGetCode() {
        return new ArrayList<>(getCode);
    }

    public List<Integer> getVerify() {
        return new ArrayList<>(verify);
    }

    public List<Integer> getGetStatus() {
        return new ArrayList<>(getStatus);
    }
}