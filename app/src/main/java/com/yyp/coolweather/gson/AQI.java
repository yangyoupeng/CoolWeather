package com.yyp.coolweather.gson;

/**
 * Created by yangyoupeng on 2017/1/5.
 */

public class AQI {

    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }

}
