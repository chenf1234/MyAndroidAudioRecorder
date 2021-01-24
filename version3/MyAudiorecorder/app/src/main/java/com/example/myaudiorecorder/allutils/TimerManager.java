package com.example.myaudiorecorder.allutils;

import android.os.SystemClock;
import android.widget.Chronometer;

//计时器工具类，使用Chronometer计时
public class TimerManager {
    private Chronometer timer;
    private static  long elaspedTime = 0;//暂停时间
    public void set(Chronometer t){
        this.timer=t;
    }

    public void start(){
        timer.setBase(SystemClock.elapsedRealtime());//计时器清零
        int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 3600);

        timer.setFormat("0"+String.valueOf(hour)+":%s");
        //Log.d("start:",String.valueOf(SystemClock.elapsedRealtime() - timer.getBase()));
        timer.start();
    }

    public void pause(){
        timer.stop();

        elaspedTime = SystemClock.elapsedRealtime()-timer.getBase();
    }

    public void restart(){

        timer.setBase(SystemClock.elapsedRealtime()-elaspedTime);
        int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 3600);
        timer.setFormat("0"+String.valueOf(hour)+":%s");
        timer.start();
    }

    public void stop(){
        elaspedTime=0;
        timer.setBase(SystemClock.elapsedRealtime());//计时器清零
        timer.setFormat("00:%s");
        timer.stop();
    }
}
