package com.example.myaudiorecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myaudiorecorder.allutils.PlayerManager;
import com.example.myaudiorecorder.allutils.TimerManager;

public class AudioPlay extends AppCompatActivity {
    private int state;//是否已经开始播放，0-停止，1-开始，2-暂停
    private PlayerManager player;

    private Button playButton;
    private Button stopButton;
    private Button backButton;
    private TextView mess;
    private TimerManager timer;
    private TextView music;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_play);

        //计时器初始化
        timer= new TimerManager();
        timer.set((Chronometer)findViewById(R.id.timer2));

        playButton=(Button)findViewById(R.id.play1);
        stopButton=(Button)findViewById(R.id.stop1);
        backButton=(Button)findViewById(R.id.Backto);
        mess=(TextView)findViewById(R.id.text2);
        music=(TextView)findViewById(R.id.music);

        state=0;
        Intent it=getIntent();
        String filename=it.getStringExtra("file");

        //Toast.makeText(getApplicationContext(),filename+" play",Toast.LENGTH_SHORT).show();
        player=new PlayerManager(filename,this);
        player.set(playButton,stopButton,mess,timer);//与播放自动结束后设置状态有关

        music.setText("播放"+filename);
        //返回上一级
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.stop();
                finish();
            }
        });
    }

    public void playing(View view){
        //对于state状态的调整，用于播放自动结束时，
        //stop按钮的状态为不可见时，表示先前存在播放自动结束，需要对state进行修正
        //因为不能直接在PlayManager中修正state
        if(stopButton.getVisibility()==View.INVISIBLE){
            state=0;
        }


        stopButton.setVisibility(View.VISIBLE);//设置停止按钮可见
        if(state==0){//开始录音
            //录音相关
            player.create();
            player.play();


            //计时器相关
            state=1;
            playButton.setText("暂停");

            timer.start();
            mess.setText("正在播放");
            mess.setVisibility(View.VISIBLE);
        }
        else if(state==1){//点击了暂停
            player.pause();

            timer.pause();
            state=2;
            playButton.setText("播放");
            mess.setText("已暂停");//提示栏

        }
        else {//暂停后重新开始
            player.restart();

            timer.restart();

            state=1;
            playButton.setText("暂停");
            mess.setText("正在播放");//提示栏

        }

    }

    public void stoping(View view){
        //首先停止录音
        player.stop();
        timer.stop();
        state=0;
        playButton.setText("播放");
        stopButton.setVisibility(View.INVISIBLE);//同时设置停止按钮不可见
        mess.setVisibility(View.INVISIBLE);
    }
}