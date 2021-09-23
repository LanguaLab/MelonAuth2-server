package moe.langua.lab.minecraft.auth.v2.server.json.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

import java.io.BufferedReader;
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

    public static Overview getDefault() {
        Overview overview = new Overview();
        overview.status = "OK";
        InputStream in = Overview.class.getResourceAsStream("/VERSION");
        try {
            assert in != null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            overview.version = reader.readLine();
        } catch (Exception e) {
            Utils.logger.warn("/VERSION file not fund in classpath, a file named VERSION which contains version information should be placed under the root folder of the jar file.");
            Utils.logger.warn(e.getStackTrace());
            overview.version = "UNKNOWN";
        }
        return overview;
    }
}
