package com.example.myaudiorecorder;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Chronometer;
import android.view.View;
import android.os.SystemClock;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myaudiorecorder.allutils.RecorderManager;
import com.example.myaudiorecorder.allutils.TimerManager;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private int state;//是否已经开始录音，0-停止，1-开始，2-暂停
    private Button startButtom;
    private Button stopButton;
    private Button WavListButton;
    private TextView note;
    private TimerManager timer;

    //需要申请的运行时权限
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int MY_PERMISSIONS_REQUEST = 1001;

    //与录音相关的对象
    private RecorderManager audiorecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //申请权限
        ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);


        //计时器初始化
        timer= new TimerManager();
        timer.set((Chronometer)findViewById(R.id.timer));

        startButtom= (Button)findViewById(R.id.start);
        stopButton=(Button)findViewById(R.id.stop) ;
        note =(TextView)findViewById(R.id.text1);
        WavListButton=(Button)findViewById(R.id.wavList);
        state=0;

        audiorecord=new RecorderManager();

        WavListButton.setEnabled(true);
        //Log.d("main","here");
    }


    public void start(View view){

        //录音期间不允许点击“文件列表按钮”
        WavListButton.setEnabled(false);
        stopButton.setVisibility(View.VISIBLE);//设置停止按钮可见
        if(state==0){//开始录音
            //录音相关
            audiorecord.create();
            audiorecord.start();

            //计时器相关
            state=1;
            startButtom.setText("暂停");

            timer.start();
            note.setText("正在录音");
            note.setVisibility(View.VISIBLE);
        }
        else if(state==1){//点击了暂停
            audiorecord.pause();

            timer.pause();
            state=2;
            startButtom.setText("开始");
            note.setText("已暂停");//提示栏

        }
        else {//暂停后重新开始
            audiorecord.start();

            timer.restart();

            state=1;
            startButtom.setText("暂停");
            note.setText("正在录音");//提示栏

        }

    }
    void myDialog(){//可获取输入值的alertdialog

        final EditText init = new EditText(this);
        AlertDialog myalert = new AlertDialog.Builder(this).setTitle("命名和保存")
                .setIcon(R.mipmap.ic_launcher)
                .setView(init)
                .setPositiveButton("保存", null)
                .setNegativeButton("删除",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        audiorecord.deleteFile();//点击删除后将最后的临时文件删除
                    }
                }).create();
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

                        audiorecord.deleteFile();//将最后的临时文件删除
                        
                        //跳转到文件列表
                        Intent it=new Intent(MainActivity.this,ListActivity.class);
                        startActivity(it);
                    }
                }

        });
    }


    public void stop(View view){
        //首先停止录音
        audiorecord.stop();

        WavListButton.setEnabled(true);
        //使用alertdialog获得文件名,同时将pcm转为指定文件名的wav文件
        myDialog();

        timer.stop();
        state=0;
        startButtom.setText("开始");
        stopButton.setVisibility(View.INVISIBLE);//同时设置停止按钮不可见
        note.setVisibility(View.INVISIBLE);


    }

    //显示所有的wav文件
    public void showWav(View view){
        Intent showWavList = new Intent(MainActivity.this, ListActivity.class);

        startActivity(showWavList);
    }

}