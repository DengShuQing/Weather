package com.example.weather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {
    @SerializedName("comf")
    public comfortable comfortable;

    public sport sport;

    @SerializedName("cw")
    public washCar washCar;

    public class comfortable{
        @SerializedName("txt")
        public String information;
    }

    public class sport{
        @SerializedName("txt")
        public String information;
    }

    public class washCar{
        @SerializedName("txt")
        public String information;
    }
}
