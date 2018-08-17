package com.example.weather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {
    public String date;

    @SerializedName("cond")
    public cloud cloud;

    public class cloud{
        @SerializedName("txt_d")
        public String information;
    }

    @SerializedName("tmp")
    public temperature temperature;

    public class temperature{
        public String max;
        public String min;
    }
}
