package com.example.weather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    //储存是否获取成功的状态，成功则返回ok，失败会有其他字段
    public String status;

    public Aqi aqi;
    public Basic basic;
    //有多个数据组用列表来声明
    @SerializedName("daily_forecast")
    public List<Forecast> forecast;
    public Now now;
    public Suggestion suggestion;
}
