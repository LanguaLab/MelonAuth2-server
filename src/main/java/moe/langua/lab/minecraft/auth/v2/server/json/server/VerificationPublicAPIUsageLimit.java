package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class VerificationPublicAPIUsageLimit {

    @SerializedName("getVerificationCodeDetail")
    @Expose
    public List<Integer> getVerificationCodeDetail = null;
    @SerializedName("sendVerificationRequest")
    @Expose
    public List<Integer> sendVerificationRequest = null;

    public static VerificationPublicAPIUsageLimit getDefault() {
        VerificationPublicAPIUsageLimit verificationPublicAPIUsageLimit = new VerificationPublicAPIUsageLimit();
        verificationPublicAPIUsageLimit.setToDefault();
        return verificationPublicAPIUsageLimit;
    }

    public void check() {
        if (getVerificationCodeDetail == null || sendVerificationRequest == null) {
            setToDefault();
        }
        if (getVerificationCodeDetail.size() < 2 || sendVerificationRequest.size() < 2) {
            setToDefault();
        }
    }

    public void setToDefault() {
        getVerificationCodeDetail = new ArrayList<>();
        getVerificationCodeDetail.add(60);
        getVerificationCodeDetail.add(60000);
        sendVerificationRequest = new ArrayList<>();
        sendVerificationRequest.add(1);
        sendVerificationRequest.add(60000);
    }

}