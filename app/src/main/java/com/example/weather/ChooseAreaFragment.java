package com.example.weather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.weather.db.City;
import com.example.weather.db.County;
import com.example.weather.db.Province;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.Utility;
import org.json.JSONException;
import org.litepal.LitePal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    private TextView title;
    private Button back;
    private ListView chooseitem;
    private ProgressDialog progress;

    private ArrayAdapter<String> adapter;
    private List<String> datalist=new ArrayList<>();

    /**
     *省,市，区列表
     */
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    /**
     * 选中的省，市
     */
    private Province selectedProvince;
    private City selectedCity;

    /**
     * 选则的级别
     */
    private int currentLevel;
    private static final int level_province=0;
    private static final int level_city=1;
    private static final int level_county=2;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
         title=view.findViewById(R.id.textView);
         back=view.findViewById(R.id.button);
         chooseitem=view.findViewById(R.id.Choose_item);
         adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,datalist);
         chooseitem.setAdapter(adapter);
        return view;
    }

    /**
     * 设置点击事件
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //直接执行省级列表
        queryProvince();

        chooseitem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //对级别进行判断
                if(currentLevel==level_province){
                    selectedProvince=provinceList.get(position);
                    queryCity();
                }else if(currentLevel==level_city){
                    selectedCity=cityList.get(position);
                    queryCounty();
                }else if(currentLevel==level_county){
                    //通过WeatherId判断获取天气信息的城市
                    String WeatherId=countyList.get(position).getWeatherId();
                    //用instanceof来判断活动在哪个实例里面
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("WeatherId", WeatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity=(WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(WeatherId);
                    }
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==level_county){
                    queryCity();
                }else if(currentLevel==level_city){
                    queryProvince();
                }
            }
        });

    }

    /**
     * 省级列表
     */

    private void queryProvince(){
        title.setText("中国");
        back.setVisibility(View.INVISIBLE);
        //查找该类的数据集从数据库的表里
        provinceList= LitePal.findAll(Province.class);
        //
        if(provinceList.size()>0){
            datalist.clear();
            for(Province province:provinceList){
                datalist.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            chooseitem.setSelection(0);
            currentLevel=level_province;
        }else{
            String url="http://guolin.tech/api/china";
            queryFromServer(url,"province");
        }
    }

    /**
     * 市级列表
     */

    private void queryCity(){
        title.setText(selectedProvince.getProvinceName());
        back.setVisibility(View.VISIBLE);
        //执行了where语句 select * from city where ()
        cityList= LitePal.where("provinceId = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        //
        if(cityList.size()>0){
            datalist.clear();
            for(City city:cityList){
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            chooseitem.setSelection(0);
            currentLevel=level_city;
        }else{
            String url="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
            queryFromServer(url,"city");
        }
    }

    /**
     * 区级列表
     */

    private void queryCounty(){
        title.setText(selectedCity.getCityName());
        back.setVisibility(View.VISIBLE);
        //执行了where语句
        countyList = LitePal.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        //
        if(countyList.size()>0){
            datalist.clear();
            for(County county: countyList){
                datalist.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            chooseitem.setSelection(0);
            currentLevel=level_county;
        }else{
            String url="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode();
            queryFromServer(url,"county");
        }
    }

    /**
     * 从服务器查询
     */
    private void queryFromServer(String url,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getActivity(),"网络加载数据失败",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseText= Objects.requireNonNull(response.body()).string();
                boolean result=false;
                try {
                    if ("province".equals(type)) {
                        result = Utility.handleProvinceResponse(responseText);
                    }else if("city".equals(type)){
                        result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                    }else if("county".equals(type)){
                        result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if("province".equals(type)){
                                queryProvince();
                            }else if("city".equals(type)){
                                queryCity();
                            }else if("county".equals(type)){
                                queryCounty();
                            }
                            closeProgressDialog();
                        }
                    });
                }
            }
        });

    }

    private void showProgressDialog(){
        if(progress==null){
            progress=new ProgressDialog(getActivity());
            progress.setTitle("正在加载...");
            progress.setCanceledOnTouchOutside(false);
        }
        progress.show();
    }

    private void closeProgressDialog(){
        if(progress!=null) progress.dismiss();
    }

}
