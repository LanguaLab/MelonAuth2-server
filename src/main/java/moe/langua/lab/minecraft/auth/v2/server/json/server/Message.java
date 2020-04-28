package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Message {

    @SerializedName("message")
    @Expose
    public String message;

    public static Message getFromString(String message) {
        Message result = new Message();
        result.message = message;
        return result;
    }

}