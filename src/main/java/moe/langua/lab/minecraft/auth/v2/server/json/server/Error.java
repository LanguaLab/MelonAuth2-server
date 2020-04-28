package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Error implements Cloneable {

    @SerializedName("error")
    @Expose
    public String error;
    @SerializedName("errorMessage")
    @Expose
    public String errorMessage;
    @SerializedName("extra")
    @Expose
    public String extra;

    public Error(String error, String errorMessage, String extra) {
        this.error = error;
        this.errorMessage = errorMessage;
        this.extra = extra;
    }

    public Error setError(String error) {
        this.error = error;
        return this;
    }

    public Error setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public Error setExtra(String extra) {
        this.extra = extra;
        return this;
    }

    @Override
    public Error clone() {
        return new Error(error, errorMessage, extra);
    }
}