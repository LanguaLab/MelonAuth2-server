package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Overview {
    @SerializedName("status")
    @Expose
    public String status;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("version")
    @Expose
    public String version;
    @SerializedName("applicationOwner")
    @Expose
    public String applicationOwner;

    public static Overview getDefault(){
        Overview overview = new Overview();
        overview.status = "OK";
        InputStream in = Overview.class.getResourceAsStream("/VERSION");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try{
            overview.version = reader.readLine();
        }catch (IOException e){
            overview.version = "UNKNOWN";
        }
        return overview;
    }
}
