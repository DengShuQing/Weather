package com.example.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.weather.gson.Weather;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences= getSharedPreferences("data",MODE_PRIVATE);
        if(preferences.getString("weather",null)!=null){
            Intent intent=new Intent(MainActivity.this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }else{
            Log.i("---", "启动城市选择列表");
        }
    }
}
