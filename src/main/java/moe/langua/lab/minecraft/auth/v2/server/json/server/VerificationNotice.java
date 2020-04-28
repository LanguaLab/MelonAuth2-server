package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VerificationNotice {

    @SerializedName("verificationCode")
    @Expose
    public int verificationCode;
    @SerializedName("expireIn")
    @Expose
    public Long expireIn;

    public VerificationNotice(int verificationCode, Long expireIn) {
        this.verificationCode = verificationCode;
        this.expireIn = expireIn;
    }

    public VerificationNotice() {
    }
}
