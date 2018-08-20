package com.example.weather.util;

import android.text.TextUtils;
import com.example.weather.db.City;
import com.example.weather.db.County;
import com.example.weather.db.Province;
import com.example.weather.gson.Weather;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Utility {
    /**
     * 解析和处理服务器放回的省级数据
     */
    public static boolean handleProvinceResponse(String response) throws JSONException {
        if(!TextUtils.isEmpty(response)){
            JSONArray allProvince=new JSONArray(response);
            for(int i=0;i<allProvince.length();i++){
                JSONObject jsonObject=allProvince.getJSONObject(i);
                Province province=new Province();
                province.setProvinceName(jsonObject.getString("name"));
                province.setProvinceCode(jsonObject.getInt("id"));
                //相当于插入语句
                province.save();
            }
            return true;
        }
        return false;
    }

    /**
     * 解析市级数据
     */
    public static boolean handleCityResponse(String response,int ProvinceId) throws JSONException {
        if(!TextUtils.isEmpty(response)){
            JSONArray allCounty=new JSONArray(response);
            for(int i=0;i<allCounty.length();i++){
                JSONObject jsonObject=allCounty.getJSONObject(i);
                City city=new City();
                city.setCityName(jsonObject.getString("name"));
                city.setCityCode(jsonObject.getInt("id"));
                city.setProvinceId(ProvinceId);
                city.save();
            }
            return true;
        }
        return false;
    }

    /**
     * 解析县级数据
     */
    public static boolean handleCountyResponse(String response,int cityId) throws JSONException {
        if(!TextUtils.isEmpty(response)){
            JSONArray allCounty=new JSONArray(response);
            for(int i=0;i<allCounty.length();i++){
                JSONObject jsonObject=allCounty.getJSONObject(i);
                County county=new County();
                county.setCountyName(jsonObject.getString("name"));
                county.setWeatherId(jsonObject.getString("weather_id"));
                county.setCityId(cityId);
                county.save();
                }
            return true;
        }
        return false;
    }

    /**
     * 解析返回的json天气信息
     */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String content=jsonArray.getString(0);
            //将json数据和Weather类的变量联系起来
            return new Gson().fromJson(content,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
