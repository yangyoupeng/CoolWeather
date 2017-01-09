package com.yyp.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.yyp.coolweather.gson.Weather;
import com.yyp.coolweather.util.HttpUtil;
import com.yyp.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        //        // TODO: Return the communication channel to the service.
        //        throw new UnsupportedOperationException ("Not yet implemented");

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather ();
        updateBingPic ();
        AlarmManager manager = (AlarmManager) getSystemService (ALARM_SERVICE);
        /*����8��Сʱ���������*/
        int anHout = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime () + anHout;
        Intent i = new Intent (this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService (this, 0, i, 0);
        manager.cancel (pendingIntent);
        manager.set (AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);

        return super.onStartCommand (intent, flags, startId);
    }

    /**
     * ����������Ϣ
     */
    private void updateWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences (this);
        String weatherString = sharedPreferences.getString ("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse (weatherString);
            String weatherId = weather.basic.weatherId;

            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId +
                    "&key=a0da1a13dde44a348d8721f757f8a8a0";

            HttpUtil.sendOkHttpRequest (weatherUrl, new Callback () {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body ().string ();
                    Weather weather = Utility.handleWeatherResponse (responseText);
                    if (weather != null & "ok".equals (weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences (AutoUpdateService.this).edit ();
                        editor.putString ("weather", responseText);
                        editor.apply ();
                    }
                }
            });
        }
    }


    /*���±�Ӧÿ��һͼ*/
    private void updateBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest (requestBingPic, new Callback () {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String stringPic = response.body ().string ();
                SharedPreferences.Editor editorPic = PreferenceManager
                        .getDefaultSharedPreferences (AutoUpdateService.this).edit ();

                editorPic.putString ("bing_pic", stringPic);
                editorPic.apply ();
            }
        });
    }
}
