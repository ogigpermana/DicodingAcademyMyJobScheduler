package com.ogi.myjobscheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private  int jobId = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnStart = findViewById(R.id.btn_start);

        btnCancel.setOnClickListener(this);
        btnStart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_cancel:
                cancelJob();
                break;
            case R.id.btn_start:
                Log.d(TAG, "btn start");
                startJob();
                break;
        }
    }

    private void cancelJob(){
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancel(jobId);

         Toast.makeText(this,"Job Scheduler has been canceled", Toast.LENGTH_SHORT).show();
//         finish();
    }
    private void startJob(){
        ComponentName mServiceComponent = new ComponentName(this, GetCurrentWeatherJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, mServiceComponent);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setRequiresDeviceIdle(false);
        builder.setRequiresCharging(false);

        // 1000ms = 1 second
        builder.setPeriodic(18000);

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());

         Toast.makeText(this, "Job Scheduler has been started", Toast.LENGTH_SHORT).show();
    }
}
