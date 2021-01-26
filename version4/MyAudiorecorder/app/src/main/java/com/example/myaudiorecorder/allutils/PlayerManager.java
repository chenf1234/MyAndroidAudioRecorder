package com.example.myaudiorecorder.allutils;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;

//使用mediarecord播放音频
public class PlayerManager {

    private Context mcontext;
    private MediaPlayer mediaPlayer;

    private String filename="";

    //与录音自动播放结束有关
    private Button playBt;
    private Button stopBt;
    private TimerManager timer;
    private TextView mes;
    //构造函数，设置文件名
    public PlayerManager(String s,Context c){
        this.mcontext=c;
        filename =s;
    }

    public void set(Button play,Button stop,TextView m,TimerManager t){
        playBt=play;
        stopBt=stop;
        mes=m;
        timer=t;
    }
    


    public void create(){
        try {
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory() + "/ARecordFiles/" + filename);
            mediaPlayer.prepare();//进行数据缓冲

        }catch (IOException e)
        {

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

                stop();

                //当播放完成后，需要重新设置状态，并且暂停计时器
                //方法1：关闭当前页面，跳转到新的播放页面
//                Intent it=new Intent(mcontext, AudioPlay.class);
//                it.putExtra("file",filename);
//                it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                mcontext.startActivity(it);

                //方法2：获取各个组件并自行设置状态，解决了每次结束都需要进行页面跳转才能重新设置状态的问题
                Toast.makeText(mcontext.getApplicationContext(),"播放结束",Toast.LENGTH_SHORT).show();
                timer.stop();
                playBt.setText("播放");
                stopBt.setVisibility(View.INVISIBLE);
                mes.setVisibility(View.INVISIBLE);

                
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
