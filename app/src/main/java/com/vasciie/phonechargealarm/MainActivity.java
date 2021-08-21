package com.vasciie.phonechargealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView imageAlarm, imageCharge, imageBattery;
    private Button alarmBtn;

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;

    private int prevVolume;


    //@SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageAlarm = findViewById(R.id.imageView_alarm);
        imageBattery = findViewById(R.id.imageView_battery);
        imageCharge = findViewById(R.id.imageView_charge);

        alarmBtn = findViewById(R.id.alarmBtn);
        alarmBtn.setOnClickListener(view -> {
            imageAlarm.setVisibility(View.INVISIBLE);
            alarmBtn.setVisibility(View.INVISIBLE);

            mediaPlayer.pause();
        });


        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        this.registerReceiver(new MainActivity.PowerConnectionReceiver(), filter);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mediaPlayer = MediaPlayer.create(this, R.raw.battery_alarm);
        mediaPlayer.setLooping(true);
        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        //mediaPlayer.setVolume(15, 15);

        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        prevVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        for(int i = prevVolume; i < 15; i++)
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);

    }

    @Override
    protected void onDestroy() {
        for(int i = prevVolume; i < 15; i++)
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);

        mediaPlayer.release();
        mediaPlayer = null;

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    public class PowerConnectionReceiver extends BroadcastReceiver {

        private boolean prevCharging; //Whether the battery was charging so far!

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL ||
                    status == BatteryManager.BATTERY_STATUS_NOT_CHARGING;

            if(isCharging) {
                imageCharge.setVisibility(View.VISIBLE);
                prevCharging = true;
            }else{
                imageCharge.setVisibility(View.INVISIBLE);
                if(prevCharging)
                    goOff();

                prevCharging = false;
            }

            changePercentage(intent);
        }

        private void goOff(){
            imageAlarm.setVisibility(View.VISIBLE);
            alarmBtn.setVisibility(View.VISIBLE);
            try {
                mediaPlayer.start();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        private void changePercentage(Intent batteryStatus){
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if(level == -1){
                imageBattery.setImageResource(R.drawable.no_battery);
                return;
            }

            float batteryPct = level * 100 / (float)scale;

            if(batteryPct < 30)
                imageBattery.setImageResource(R.drawable.battery_low);
            else if(batteryPct < 70)
                imageBattery.setImageResource(R.drawable.battery_med);
            else
                imageBattery.setImageResource(R.drawable.battery_hi);
        }
    }
}