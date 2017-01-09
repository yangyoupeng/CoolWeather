package com.yyp.coolweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yyp.coolweather.db.City;
import com.yyp.coolweather.db.County;
import com.yyp.coolweather.db.Province;
import com.yyp.coolweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangyoupeng on 2017/1/4.
 */

public class Utility {

    /**
     * 将返回json数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject (response);
            JSONArray jsonArray = jsonObject.getJSONArray ("HeWeather");
            String weatherContent = jsonArray.getJSONObject (0).toString ();
            return new Gson ().fromJson (weatherContent, Weather.class);
        } catch (JSONException e) {
            e.printStackTrace ();
        }
        return null;
    }


    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty (response)) {
            try {
                JSONArray allProvinces = new JSONArray (response);
                for (int i = 0; i < allProvinces.length (); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject (i);
                    Province province = new Province ();
                    province.setProvinceName (provinceObject.getString ("name"));
                    province.setProvinceCode (provinceObject.getInt ("id"));
                    province.save ();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace ();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty (response)) {
            try {
                JSONArray allCityes = new JSONArray (response);
                for (int i = 0; i < allCityes.length (); i++) {
                    JSONObject cityObject = allCityes.getJSONObject (i);
                    City city = new City ();
                    city.setCityName (cityObject.getString ("name"));
                    city.setCityCode (cityObject.getInt ("id"));
                    city.setProvinceId (provinceId);
                    city.save ();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace ();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty (response)) {
            try {
                JSONArray allCounty = new JSONArray (response);
                for (int i = 0; i < allCounty.length (); i++) {
                    JSONObject cityObject = allCounty.getJSONObject (i);
                    County county = new County ();
                    county.setCountyName (cityObject.getString ("name"));
                    county.setWeatherId (cityObject.getString ("weather_id"));
                    county.setCityId (cityId);
                    county.save ();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace ();
            }
        }
        return false;
    }


}
