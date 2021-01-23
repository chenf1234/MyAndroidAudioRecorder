package com.example.myaudiorecorder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Chronometer;
import android.view.View;
import android.os.SystemClock;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Chronometer timer;
    private int state;//是否已经开始录音，0-停止，1-开始，2-暂停
    private Button startButtom;
    private Button stopButton;
    private TextView note;
    private static  long elaspedTime = 0;//暂停时间
    private String filename;
    private int isSure;//是否点击了确定
    //需要申请的运行时权限
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int MY_PERMISSIONS_REQUEST = 1001;

    private RecorderManager audiorecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //申请权限
        ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);

        timer = (Chronometer) findViewById(R.id.timer);
        startButtom= (Button)findViewById(R.id.start);
        stopButton=(Button)findViewById(R.id.stop) ;
        note =(TextView)findViewById(R.id.text1);
        state=0;

        audiorecord=new RecorderManager();
        //Log.d("main","here");
    }

    public void start(View view){
        //stopButton.setEnabled(true);

        stopButton.setVisibility(View.VISIBLE);//设置停止按钮可见
        if(state==0){//开始录音
            audiorecord.start();
            state=1;
            startButtom.setText("暂停");
            timer.setBase(SystemClock.elapsedRealtime());//计时器清零
            int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 3600);

            timer.setFormat("0"+String.valueOf(hour)+":%s");
            //Log.d("start:",String.valueOf(SystemClock.elapsedRealtime() - timer.getBase()));
            timer.start();

            note.setVisibility(View.VISIBLE);
        }
        else if(state==1){//点击了暂停
            timer.stop();
            //Log.d("pause:",String.valueOf(SystemClock.elapsedRealtime() - timer.getBase()));
            elaspedTime = SystemClock.elapsedRealtime()-timer.getBase();
            state=2;
            startButtom.setText("开始");
            note.setText("已暂停");//提示栏

        }
        else {//暂停后重新开始
            timer.setBase(SystemClock.elapsedRealtime()-elaspedTime);
            int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 3600);
            timer.setFormat("0"+String.valueOf(hour)+":%s");
            //Log.d("restart:",String.valueOf(SystemClock.elapsedRealtime() - timer.getBase()));
            timer.start();
            state=1;
            startButtom.setText("暂停");
            note.setText("正在录音");//提示栏

        }

    }
    void myDialog(){//可获取输入值的alertdialog

        final EditText init = new EditText(this);
        AlertDialog myalert = new AlertDialog.Builder(this).setTitle("命名和保存")

                .setView(init)
                .setPositiveButton("保存", null)
                .setNegativeButton("删除",null).create();
        myalert.show();
        myalert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String input = init.getText().toString();

                    if (input.equals("")) {
                        //Log.d("filename1: ","1 "+input);

                        Toast.makeText(getApplicationContext(), "文件名不能为空！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else if(audiorecord.isFile(input)) {//判断文件是否存在
                        //Log.d("filename2: ","1 "+input);

                        Toast.makeText(getApplicationContext(), "该文件已存在!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        //Log.d("filename3: ","1 "+input);

                        //转换，必须写在dialog内部！
                        audiorecord.PcmTOWav(input+".wav");

                        Toast.makeText(getApplicationContext(), "文件保存成功!", Toast.LENGTH_SHORT).show();
                        //让AlertDialog消失
                        myalert.dismiss();
                    }
                }

        });
    }

    public void stop(View view){
        //首先停止录音
        audiorecord.stop();

        //使用alertdialog获得文件名,同时将pcm转为指定文件名的wav文件
        myDialog();


        elaspedTime=0;
        state=0;
        startButtom.setText("开始");
        stopButton.setVisibility(View.INVISIBLE);//同时设置停止按钮不可见
        note.setVisibility(View.INVISIBLE);
        timer.setBase(SystemClock.elapsedRealtime());//计时器清零
        timer.setFormat("00:%s");
        timer.stop();


    }

}