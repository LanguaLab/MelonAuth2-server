package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VerificationNotice {

    @SerializedName("verificationCode")
    @Expose
    private int verificationCode;
    @SerializedName("expireIn")
    @Expose
    private Long expireIn;

    public VerificationNotice(int verificationCode, Long expireIn) {
        this.verificationCode = verificationCode;
        this.expireIn = expireIn;
    }

    public VerificationNotice() {
    }

    public int getVerificationCode() {
        return verificationCode;
    }

    public Long getExpireIn() {
        return expireIn;
    }
}
