package com.example.myaudiorecorder.allutils;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.widget.Chronometer;

import com.example.myaudiorecorder.AudioPlay;

import java.io.IOException;

//使用mediarecord播放音频
public class PlayerManager {

    private Context mcontext;
    private MediaPlayer mediaPlayer;

    private String filename="";

    //构造函数，设置文件名
    public PlayerManager(String s,Context c){
        this.mcontext=c;
        filename =s;
    }

    //设置计时器


    public void create(){
        try {
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory() + "/ARecordFiles/" + filename);
            mediaPlayer.prepare();//进行数据缓冲

        }catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }

    public void play(){
        mediaPlayer.start();
        //监听MediaPlayer播放完成
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //当播放完成后，需要重新设置状态，并且暂停计时器，目前我只会这样写：
                //关闭当前页面，跳转到新的播放页面
                stop();
                Intent it=new Intent(mcontext, AudioPlay.class);
                it.putExtra("file",filename);
                it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//方法1：关闭当前页面
                mcontext.startActivity(it);

            }
        });
    }

    public void pause(){
        if(mediaPlayer!=null) {
            mediaPlayer.pause();
        }
    }

    public void stop(){
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void restart(){
        if(mediaPlayer!=null) {
            mediaPlayer.start();
        }
    }
}
