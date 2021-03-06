package com.example.myaudiorecorder;

import android.media.AudioFormat;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//pcm转wav工具
public class Pcm2WavUtil {
    private int sampleRateInHz;//采样率
    private int channelConfig;//声道数
    private int audioFormat;//采样位数
    private int mBufferSize;//最小缓冲区大小
    public Pcm2WavUtil(int sampleRate,int channel,int format,int size){
        sampleRateInHz=sampleRate;
        channelConfig=(channel == AudioFormat.CHANNEL_IN_MONO )? 1 : 2;//声道数
        audioFormat=(format==AudioFormat.ENCODING_PCM_16BIT) ? 16 : 8;//采样位数
        mBufferSize=size;
    }
    public  void pcmToWav(String inFilename, String outFilename) {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;

        long byteRate =  sampleRateInHz *audioFormat *channelConfig  ;//码率
        byte[] data = new byte[mBufferSize];
        try {
            //首先创建文件夹
            File inFile = new File(Environment.getExternalStorageDirectory(),inFilename);
            File outFile =new File(Environment.getExternalStorageDirectory(),outFilename);
            //如果文件存在，先删除文件
            if(outFile.exists()){
                outFile.delete();
            }

            //然后创建新文件
            outFile.createNewFile();

            in = new FileInputStream(inFile);
            out = new FileOutputStream(outFile);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                    byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 加入wav文件头
     */
    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                            long totalDataLen, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        //WAVE
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // 'fmt ' chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // 4 bytes: size of 'fmt ' chunk
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // format = 1
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channelConfig;
        header[23] = 0;
        header[24] = (byte) (sampleRateInHz & 0xff);
        header[25] = (byte) ((sampleRateInHz >> 8) & 0xff);
        header[26] = (byte) ((sampleRateInHz >> 16) & 0xff);
        header[27] = (byte) ((sampleRateInHz >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // block align
        header[32] = (byte) (channelConfig *audioFormat  / 8);
        header[33] = 0;
        // bits per sample
        header[34] = (byte)audioFormat;
        header[35] = 0;
        //data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

}
