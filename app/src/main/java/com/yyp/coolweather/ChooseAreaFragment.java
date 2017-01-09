package com.yyp.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yyp.coolweather.db.City;
import com.yyp.coolweather.db.County;
import com.yyp.coolweather.db.Province;
import com.yyp.coolweather.util.HttpUtil;
import com.yyp.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by yangyoupeng on 2017/1/4.
 */
public class ChooseAreaFragment extends Fragment {

    private static final String TAG = "ChooseAreaFragment";

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 省列表
     */
    private List< Province > provinceList;
    /**
     * 市列表
     */
    private List< City > cityList;
    /**
     * 县列表
     */
    private List< County > countyList;

    private ListView listView;
    private TextView tv;
    private Button button;

    private List< String > dataList = new ArrayList<> ();
    private ArrayAdapter< String > adapter;

    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        View view = inflater.inflate (R.layout.choose_area, container, false);
        button = (Button) view.findViewById (R.id.bt_back);
        tv = (TextView) view.findViewById (R.id.title_text);
        listView = (ListView) view.findViewById (R.id.lv_view);

        adapter = new ArrayAdapter< String > (getContext (), android.R.layout.simple_list_item_1,
                dataList);
        listView.setAdapter (adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated (savedInstanceState);

        listView.setOnItemClickListener (new AdapterView.OnItemClickListener () {
            @Override
            public void onItemClick(AdapterView< ? > adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get (i);
                    queryCityes ();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get (i);
                    queryCounties ();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get (i).getWeatherId ();
                    if (getActivity () instanceof MainActivity) {
                        Intent intent = new Intent (getActivity (), WeatherActivity.class);
                        intent.putExtra ("weather_id", weatherId);
                        startActivity (intent);
                        getActivity ().finish ();
                    } else if (getActivity () instanceof WeatherActivity) {
                        WeatherActivity weatherActivity = (WeatherActivity) getActivity ();
                        weatherActivity.drawerLayout.closeDrawers ();
                        weatherActivity.swipeRefreshLayout.setRefreshing (true);
                        weatherActivity.requestWeather (weatherId);

                    }
                }
            }
        });

        button.setOnClickListener (view -> {
            if (currentLevel == LEVEL_COUNTY) {
                queryCityes ();
            } else if (currentLevel == LEVEL_CITY) {
                queryProvinces ();
            }
        });
        queryProvinces ();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        tv.setText ("中国");
        button.setVisibility (View.GONE);
        provinceList = DataSupport.findAll (Province.class);
        if (provinceList.size () > 0) {
            dataList.clear ();
            for (Province pv : provinceList) {
                dataList.add (pv.getProvinceName ());
            }
            adapter.notifyDataSetChanged ();
            listView.setSelection (0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer (address, "province");
        }
    }

    /**
     * 查询全国所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCityes() {
        tv.setText (selectedProvince.getProvinceName ());
        button.setVisibility (View.VISIBLE);
        cityList = DataSupport.where ("provinceid = ?", String.valueOf (selectedProvince.getId ()
        )).find (City.class);

        if (cityList.size () > 0) {
            dataList.clear ();
            for (City city : cityList) {
                dataList.add (city.getCityName ());
            }
            adapter.notifyDataSetChanged ();
            listView.setSelection (0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode ();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer (address, "city");
        }
    }

    /**
     * 查询全国所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties() {
        tv.setText (selectedCity.getCityName ());
        button.setVisibility (View.VISIBLE);
        countyList = DataSupport.where ("cityid =?", String.valueOf (selectedCity.getId ())).find
                (County.class);
        if (countyList.size () > 0) {
            dataList.clear ();
            for (County county : countyList) {
                dataList.add (county.getCountyName ());
            }
            adapter.notifyDataSetChanged ();
            listView.setSelection (0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode ();
            int cityCode = selectedCity.getCityCode ();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer (address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog ();
        HttpUtil.sendOkHttpRequest (address, new Callback () {

            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity ().runOnUiThread (() -> {
                    closeProgressDialog ();
                    Toast.makeText (getContext (), "加载失败", Toast.LENGTH_SHORT).show ();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body ().string ();

                boolean result = false;

                if ("province".equals (type)) {
                    result = Utility.handleProvinceResponse (responseText);

                } else if ("city".equals (type)) {
                    result = Utility.handleCityResponse (responseText, selectedProvince.getId ());

                } else if ("county".equals (type)) {
                    result = Utility.handleCountyResponse (responseText, selectedCity.getId ());
                }

                if (result) {
                    getActivity ().runOnUiThread (() -> {
                        closeProgressDialog ();
                        if ("province".equals (type)) {
                            queryProvinces ();
                        } else if ("city".equals (type)) {
                            queryCityes ();
                        } else if ("county".equals (type)) {
                            queryCounties ();
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog (getActivity ());
            progressDialog.setMessage ("正在加载.......");
            progressDialog.setCancelable (false);
        }
        progressDialog.show ();
    }

    /**
     * 关闭对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss ();
        }
    }
}
