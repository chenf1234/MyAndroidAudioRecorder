package com.example.myaudiorecorder.allutils;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.example.myaudiorecorder.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

//录音类
public class RecorderManager {
    //共享变量，避免写文件和合并文件时发生同步互斥
    private AtomicInteger count=new AtomicInteger(0);

    /*********************与AudioRecord相关的都在这***************************/
    // 音频源：音频输入-麦克风
    // VOICE_COMMUNICATION录制立体声都是用上方麦克风；MIC和VOICE_RECOGNITION使用上下两个麦克风录制立体声
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    // 采样率
    private int AUDIO_SAMPLE_RATE = 48000;
    // 音频通道 双声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    // 采样位数：16位PCM编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    //缓冲区大小
    private  int bufferSize=0;
    //缓冲区
    private byte[] buffer;

    private AudioRecord myrecord=null;


    /*************************************************************************/

    //改变采样率
    public void changeSampleRate(int samplerate){
        this.AUDIO_SAMPLE_RATE=samplerate;
    }

    /*******************与录音状态相关的都在这*********************************/
    //录音状态
    public enum Status {
        //未开始
        STATUS_NO_READY,
        //预备
        STATUS_READY,
        //录音
        STATUS_START,
        //暂停
        STATUS_PAUSE,
        //停止
        STATUS_STOP
    }
    //初始状态为未开始
    private Status Recordstatus=Status.STATUS_NO_READY;
    /*************************************************************************/



    /*******************与文件命名及操作相关的都在这****************************/
    //Environment.getExternalStorageDirectory()获取手机存储的根目录

    private String DirName="ARecordfiles";//文件夹名称
    private String BasepcmFileName="record";//临时文件base名称
    private int FileVersion;//临时文件版本号，用来控制暂停继续
    private List<String> AllFiles = null;//保存所有临时文件名
    private String Afilename="AllRecord.pcm";//最终文件名
    private String recordDir = String.format(Locale.getDefault(),
            "%s/",
            Environment.getExternalStorageDirectory().getAbsolutePath());


    //删除临时最后的pcm文件
    public void deleteFile(){
        Log.d("RecorderManager-deleterFile","删除最后的临时pcm文件");
        File checkFile = new File(recordDir,DirName+"/"+Afilename);//打开文件
        if(checkFile.exists()){
            checkFile.delete();
        }

    }

    //判断文件是否存在
    public boolean isFile(String filename){
        File checkFile = new File(recordDir,DirName+"/"+filename+".wav");//打开文件
        if(checkFile.exists()){
            return true;
        }
        else return false;

    }

    //合并所有临时文件成为一个文件
    public void mergeAllFiles(){

        while(count.get()>0){
            Log.d("RecorderManager-mergeAllFiles","录音数据还没保存完毕，暂时还不能合并文件");
            Log.d("RecorderManager-mergeAllFiles","还有"+String.valueOf(count.get())+"个文件在保存录音数据");
        }
        Log.d("RecorderManager-mergeAllFiles","开始合并所有的临时文件");
        File finalFile =new File(recordDir,DirName+"/"+Afilename);

        FileOutputStream os=null;

        try {
            //创建最终文件

            finalFile.createNewFile();

            os = new FileOutputStream(finalFile);

            //将每个临时文件的内容读出来并保存到最终文件中，同时删除临时文件

            for (String fileName : AllFiles) {
                FileInputStream in =null;
                File infile=new File(recordDir,DirName+"/"+fileName);
                Log.d("subfile",infile.getAbsolutePath());
                in = new FileInputStream(infile);

                byte[] data = new byte[bufferSize];
                while (in.read(data) != -1) {
                    os.write(data);
                }
                in.close();
                infile.delete();//删除临时文件
            }

            AllFiles.clear();//清除ArrayList
            FileVersion=1;//清除版本号

            os.close();
            Log.d("RecorderManager-mergeAllFiles","合并完成");

        }catch (FileNotFoundException e) {

            e.printStackTrace();
        }
        catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }

    }

    //将pcm文件转成指定名称的wav文件
    public void PcmTOWav(String wavName){
        Log.d("RecorderManager-PcmToWav","pcm文件转成wav文件");
        Pcm2WavUtil pcm=new Pcm2WavUtil(AUDIO_SAMPLE_RATE,AUDIO_CHANNEL,AUDIO_ENCODING,bufferSize);
        pcm.pcmToWav(DirName+"/"+Afilename,DirName+"/"+wavName);

    }
    /**************************************************************************************/


    /*******************录音有关操作，包括create，stop，start，pause*************************/

    //创建audiorecord对象
    public void create(){
        Log.d("RecorderManager-create","创建audiorecord对象，采样率为："+String.valueOf(AUDIO_SAMPLE_RATE));
        //初始化AudioRecord
        bufferSize=AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,AUDIO_CHANNEL,AUDIO_ENCODING);

        myrecord=new AudioRecord(AUDIO_INPUT,AUDIO_SAMPLE_RATE,AUDIO_CHANNEL,AUDIO_ENCODING,bufferSize);

        buffer = new byte[bufferSize];

        Recordstatus=Status.STATUS_READY;

        FileVersion=1;

        AllFiles = new ArrayList<>();
    }


    //停止录音
    public void stop(){

        myrecord.stop();
        Recordstatus=Status.STATUS_STOP;

        myrecord.release();
        myrecord=null;
        mergeAllFiles();//合并所有临时文件

        Recordstatus=Status.STATUS_NO_READY;

    }

    //暂停录音
    public void pause(){
        myrecord.stop();
        Recordstatus=Status.STATUS_PAUSE;
    }


    //开始录音
    public void start(){

        if (Recordstatus == Status.STATUS_NO_READY||myrecord==null) {
            throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
        }

        //设置本次录音保存的临时文件名
        String curFileName="";
        if(Recordstatus==Status.STATUS_READY){//第一次开始录音
            curFileName=BasepcmFileName+".pcm";
        }
        else if(Recordstatus==Status.STATUS_PAUSE){//从暂停重新开始录音
            curFileName=BasepcmFileName+String.valueOf(FileVersion)+".pcm";
            ++FileVersion;
        }
        Log.d("RecorderManage-start","此次的临时文件名为"+curFileName);
        //开始录音并保存状态
        myrecord.startRecording();
        Recordstatus=Status.STATUS_START;
        AllFiles.add(curFileName);
        final String finalFileName=curFileName;

        //使用新线程读取音频数据并保存
        new Thread(()->{
            //count++;
            count.incrementAndGet();
            FileOutputStream os=null;

            try {

                //首先创建文件夹

                File dirFile = new File(recordDir,DirName);

                if(!dirFile.exists()){

                    dirFile.mkdirs();
                }
                File myfile=new File(dirFile,finalFileName);

                //如果文件存在，先删除文件
                if(myfile.exists()){

                    myfile.delete();
                }

                //然后创建新文件

                myfile.createNewFile();
                if(myfile.exists()) {

                    os = new FileOutputStream(myfile);
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (Exception e) {

                Log.e("TestFile", "Error on write File:" + e);
            }

            if(os!=null){
                while(Recordstatus==Status.STATUS_START){
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
                count.decrementAndGet();
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