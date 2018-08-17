package com.example.weather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    //当字符串的名称和json数据的键名不一致时，用@SerializedName把json和java字段进行映射
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
