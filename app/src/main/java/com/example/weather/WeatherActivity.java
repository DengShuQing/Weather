package com.example.weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.weather.gson.Forecast;
import com.example.weather.gson.Weather;
import com.example.weather.service.AutoUpdateService;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.Utility;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ImageView imageView;
    private ScrollView weatherLayout;
    private TextView cityName;
    private TextView updateTime;
    private TextView temperature;
    private TextView weatherInfo;
    private LinearLayout forecstLayout;
    private TextView aqi;
    private TextView pm25;
    private TextView Comforatable;
    private TextView WashCar;
    private TextView Sport;
    private Button homeChoose;
    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;

    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*
        //无法完成
        //将状态栏透明与app融合
        Window window=getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
*/

        setContentView(R.layout.activity_weather);
        findView();
/*
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //加载背景图片
        String imagepic = preferences.getString("image_pic", null);
        if (imagepic != null) {
            //有图片缓存，直接加载
            Glide.with(this).load(imagepic).into(imageView);
        } else {
            //无图片缓存，联网下载图片
            loadPicture();
        }

        //SharedPreferences 读取数据
        String WeatherString = preferences.getString("weather", null);
        if (WeatherString != null) {
            //有缓存，直接解析数据
            Weather weather = Utility.handleWeatherResponse(WeatherString);
            //方便下拉刷新时使用的天气id
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);

        } else {
            //无缓存时，先从服务器获取天气数据
            //获取从ChooseAreaFragment传过来的天气ID
            //下拉刷新的天气id
            mWeatherId = getIntent().getStringExtra("WeatherId");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeater(mWeatherId);
        }
*/

        SharedPreferences preferences = getSharedPreferences("data",MODE_PRIVATE);
        //加载背景图片
        String imagepic = preferences.getString("image_pic", null);
        String WeatherString = preferences.getString("weather", null);
        loadPicture();
        Glide.with(this).load(imagepic).into(imageView);

        weatherLayout.setVisibility(View.INVISIBLE);
        if(WeatherString==null) {
            mWeatherId = getIntent().getStringExtra("WeatherId");
            requestWeather(mWeatherId);

        }else {
            Weather weather=Utility.handleWeatherResponse(WeatherString);
            assert weather != null;
            requestWeather(weather.basic.weatherId);

        }

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        serListener();

    }

    /**
     * 初始化各个控件
     */
    private void findView() {
        imageView = findViewById(R.id.imageView);
        weatherLayout = findViewById(R.id.weather_layout);
        cityName = findViewById(R.id.cityName);
        updateTime = findViewById(R.id.updateTime);
        temperature = findViewById(R.id.Temperature);
        weatherInfo = findViewById(R.id.Cloud_info);
        forecstLayout = findViewById(R.id.forecast_layout);
        aqi = findViewById(R.id.AQI);
        pm25 = findViewById(R.id.PM25);
        Comforatable = findViewById(R.id.Comfortable);
        WashCar = findViewById(R.id.WashCar);
        Sport = findViewById(R.id.Sport);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        drawerLayout = findViewById(R.id.drawer_layout);
        homeChoose = findViewById(R.id.homechoose);
    }

    /**
     * 设置监听器
     */
    private void serListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        homeChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开滑动界面
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 获取背景图片
     */
    private void loadPicture() {
        final String requestUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(WeatherActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String picture = Objects.requireNonNull(response.body()).string();
                SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                editor.putString("image_pic", picture);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(picture).into(imageView);
                    }
                });
            }
        });
    }

    /**
     * 根据天气ID从服务器获取天气信息
     */
    public void requestWeather(final String weatherId) {
        String url = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=4f73fed96d344670b206f042842d3aa1";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseString = Objects.requireNonNull(response.body()).string();
                final Weather weather = Utility.handleWeatherResponse(responseString);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            //SharedPreferences储存数据
                            SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                            editor.putString("weather", responseString);
                            editor.apply();
                            showWeatherInfo(weather);
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
                mWeatherId=weatherId;
            }
        });
    }

    /**
     *显示信息在ui上
     */
    private void showWeatherInfo(Weather weather) {
        String name = weather.basic.cityName;
        //String update=weather.basic.update.updateTime;
        String update = weather.basic.update.updateTime.split(" ")[1];
        String tmp = weather.now.temperature;
        String info = weather.now.cloud;

        cityName.setText(name);
        updateTime.setText(update);
        temperature.setText(tmp);
        weatherInfo.setText(info);
        forecstLayout.removeAllViews();
        //遍历循环
        for (Forecast forecast : weather.forecast) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecstLayout, false);

            TextView date = view.findViewById(R.id.date);
            TextView information = view.findViewById(R.id.info);
            TextView max = view.findViewById(R.id.max);
            TextView min = view.findViewById(R.id.min);

            date.setText(forecast.date);
            information.setText(forecast.cloud.information);
            max.setText(forecast.temperature.max);
            min.setText(forecast.temperature.min);
            //给未来几天天气预测添加控件
            forecstLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqi.setText(weather.aqi.city.aqi);
            pm25.setText(weather.aqi.city.pm25);
        }
        String comfortable = "舒适度 ：" + weather.suggestion.comfortable.information;
        String washcar = "洗车指数 ：" + weather.suggestion.washCar.information;
        String sport = "运动指数 ：" + weather.suggestion.sport.information;

        Comforatable.setText(comfortable);
        WashCar.setText(washcar);
        Sport.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
