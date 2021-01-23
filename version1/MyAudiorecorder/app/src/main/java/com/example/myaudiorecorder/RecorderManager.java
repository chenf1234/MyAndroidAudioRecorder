package com.example.myaudiorecorder;


import android.Manifest;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

//录音类
public class RecorderManager {

    // 音频源：音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    // 采样率
    private final static int AUDIO_SAMPLE_RATE = 48000;
    // 音频通道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    // 采样位数：16位PCM编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    //缓冲区大小
    private  int bufferSize=0;
    //缓冲区
    private byte[] buffer;

    private AudioRecord myrecord=null;

    //是否正在录音
    private boolean isRecording;

    private String DirName="ARecordFiles";
    private String pcmFileName="record1.pcm";
    //private String wavFileName="record1.wav";

    //删除临时pcm文件
    public void deleteFile(){
        File checkFile = new File(Environment.getExternalStorageDirectory(),DirName+"/"+pcmFileName);//打开文件
        if(checkFile.exists()){
            checkFile.delete();
        }

    }

    //判断文件是否存在
    public boolean isFile(String filename){
        File checkFile = new File(Environment.getExternalStorageDirectory(),DirName+"/"+filename+".wav");//打开文件
        if(checkFile.exists()){
            return true;
        }
        else return false;

    }

    //停止录音
    public void stop(){
        isRecording = false;
        if(myrecord!=null){
            myrecord.stop();
            myrecord.release();
            myrecord=null;
        }

    }

    //将pcm文件转成wav
    public void PcmTOWav(String wavName){
        Pcm2WavUtil pcm=new Pcm2WavUtil(AUDIO_SAMPLE_RATE,AUDIO_CHANNEL,AUDIO_ENCODING,bufferSize);
        pcm.pcmToWav(DirName+"/"+pcmFileName,DirName+"/"+wavName);
    }

    //开始录音
    public void start(){
        //初始化AudioRecord
        bufferSize=AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,AUDIO_CHANNEL,AUDIO_ENCODING);

        myrecord=new AudioRecord(AUDIO_INPUT,AUDIO_SAMPLE_RATE,AUDIO_CHANNEL,AUDIO_ENCODING,bufferSize);

        buffer = new byte[bufferSize];

        myrecord.startRecording();
        isRecording=true;

        new Thread(()->{

            FileOutputStream os=null;

            try {

                //首先创建文件夹
                File dirFile = new File(Environment.getExternalStorageDirectory(),DirName);
                if(!dirFile.exists()){
                    dirFile.mkdirs();
                }
                File myfile=new File(dirFile,pcmFileName);

                //如果文件存在，先删除文件
                if(myfile.exists()){
                    myfile.delete();
                }

                //然后创建新文件
                myfile.createNewFile();

                os = new FileOutputStream(myfile);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                Log.e("TestFile", "Error on write File:" + e);
            }

            if(os!=null){
                while(isRecording){
                    int read = myrecord.read(buffer, 0, bufferSize);

                    // 如果读取音频数据没有出现错误，就将数据写入到文件
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            os.write(buffer);
                            os.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        ).start();
    }

}
