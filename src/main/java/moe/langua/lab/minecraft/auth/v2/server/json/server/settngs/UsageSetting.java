package moe.langua.lab.minecraft.auth.v2.server.json.server.settngs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UsageSetting {

    @SerializedName("limitPerCircle")
    @Expose
    private Long limitPerCircle;
    @SerializedName("circleInMillisecond")
    @Expose
    private Long circleInMillisecond;

    public static UsageSetting get(long limitPerCircle, long circleInMillisecond) {
        UsageSetting settingInstance = new UsageSetting();
        settingInstance.limitPerCircle = limitPerCircle;
        settingInstance.circleInMillisecond = circleInMillisecond;
        return settingInstance;
    }

    public UsageSetting check(long limitPerCircleByDefault,long circleInMillisecondByDefault){
        if(limitPerCircle==null) limitPerCircle = limitPerCircleByDefault;
        if(circleInMillisecond==null) circleInMillisecond=circleInMillisecondByDefault;
        return this;
    }

    public Long getLimitPerCircle() {
        return limitPerCircle;
    }

    public Long getCircleInMillisecond() {
        return circleInMillisecond;
    }

}