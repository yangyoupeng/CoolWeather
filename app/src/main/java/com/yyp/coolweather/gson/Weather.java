package com.yyp.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by yangyoupeng on 2017/1/5.
 */

public class Weather {
    /**
     * 返回的天气数据中还会包含一项status数据，所以需要添加一个对应的字段
     */
    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List< Forecast > forecastList;
}
