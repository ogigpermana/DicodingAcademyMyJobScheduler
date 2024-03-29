package com.ogi.myjobscheduler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;


public class GetCurrentWeatherJobService extends JobService {
    public static final String TAG = GetCurrentWeatherJobService.class.getSimpleName();

    // Isi dengan token API dari openweathermap
    final String APP_ID = "6dbd83fa98306862dd45a4e1b633c95a";
    // Isi dengan nama kota sebagai contoh kota jakarta
    final String CITY = "Jakarta,id";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "OnStartJob() Executed");
        getCurrentWeather(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "OnStopJob Executed");
        return true;
    }

    private void getCurrentWeather(final JobParameters job){
        Log.d(TAG, "Running");
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://api.openweathermap.org/data/2.5/weather?q="+CITY+"&appid="+APP_ID;
        Log.e(TAG, "getCurrentWeather: "+url);
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG, result);
                try {
                    JSONObject responseObject = new JSONObject(result);

                    String currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = responseObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin = responseObject.getJSONObject("main").getDouble("temp");

                    // konversi kelvin ke celsius
                    double tempInCelsius = tempInKelvin - 273.15;
                    String temperature = new DecimalFormat("##.##").format(tempInCelsius);

                    String title = "Current Weather";
                    String message = currentWeather + ", "+description+" with "+temperature+" celsius";
                    int notifId = 100;

                    showNotification(getApplicationContext(), title, message, notifId);

                    jobFinished(job, false);
                }catch (Exception e){
                    jobFinished(job, true);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Ketika proses gagal, maka jobFinished di set dengan parameter true yang menandakan bahwa job perlu di jadwal ulang atau reschedule
                jobFinished(job, true);
            }
        });
    }

    private void showNotification(Context context, String title, String message, int notifId){
        String CHANNEL_ID = "Channel_1";
        String CHANNEL_NAME = "Job Scheduler Channel";

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_refresh_black_24dp)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.transparent))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(alarmSound)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000});

            builder.setChannelId(CHANNEL_ID);

            if (notificationManagerCompat != null) {
                notificationManagerCompat.createNotificationChannel(channel);
            }
        }

        Notification notification = builder.build();

        if (notificationManagerCompat != null){
            notificationManagerCompat.notify(notifId, notification);
        }
    }
}
