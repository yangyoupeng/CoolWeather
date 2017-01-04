package com.yyp.coolweather.util;

import android.text.TextUtils;

import com.yyp.coolweather.db.City;
import com.yyp.coolweather.db.County;
import com.yyp.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangyoupeng on 2017/1/4.
 */

public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response) throws JSONException {
        if (!TextUtils.isEmpty (response)) {
            JSONArray allProvinces = new JSONArray (response);
            for (int i = 0; i < allProvinces.length (); i++) {
                JSONObject provinceObject = allProvinces.getJSONObject (i);
                Province province = new Province ();
                province.setProvinceName (provinceObject.getString ("name"));
                province.setProvinceCode (provinceObject.getInt ("id"));
                province.save ();
            }
            return true;
        }

        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId) throws JSONException {
        if (!TextUtils.isEmpty (response)) {
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
        }

        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId) throws JSONException {
        if (!TextUtils.isEmpty (response)) {
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
        }

        return false;
    }


}
