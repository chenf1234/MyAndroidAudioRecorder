package com.example.myaudiorecorder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myaudiorecorder.allutils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private ListView listView;
    private List<File> list = new ArrayList<>();
    private FileListAdapter adapter;
    private Button back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listView = (ListView) findViewById(R.id.listView);
        back =(Button)findViewById(R.id.getBack);
        list = FileUtil.getWavFiles();


        adapter = new FileListAdapter(this, list);
        listView.setAdapter(adapter);


        //设置单击监听事件，跳转到其他界面
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent recordView=new Intent(ListActivity.this,MainActivity.class);
//                startActivity(recordView);
                File file= list.get(position);
                String filename=file.getName();
                Intent playView=new Intent(ListActivity.this,AudioPlay.class);

                //Toast.makeText(getApplicationContext(),"you choose the file : "+filename,Toast.LENGTH_SHORT).show();
                playView.putExtra("file",filename);
                startActivity(playView);
            }
        });

        //设置长按监听事件，进行删除
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                File file= list.get(position);
                String filename=file.getName();
                Toast.makeText(getApplicationContext(),"you choose the file : "+filename,Toast.LENGTH_SHORT).show();
                //弹框确定是否删除
                myDialog(filename);

                return true;
                //关于返回值，若返回False，则是当长按时，既调用onItemLongClick，同时调用onItemLongClick后
                //还会调用onItemClick，就是说会同时调用onItemLongClick，和onItemClick，
                //若返回true，则只调用onItemLongClick
            }
        });

        //返回上一级
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //删除操作时的确定弹框
    private void myDialog(String filename){
        AlertDialog myalert = new AlertDialog.Builder(this).setTitle("是否删除该文件？")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File deletefile=new File(Environment.getExternalStorageDirectory(),"ARecordFiles/"+filename);
                        if(deletefile.exists()){
                            deletefile.delete();
                            Toast.makeText(getApplicationContext(),filename+"删除成功",Toast.LENGTH_SHORT).show();
                        }
                        //跳转回当前页面进行刷新，同时删除旧页面
                        Intent it = new Intent();
                        it.setClass(ListActivity.this, ListActivity.class);
                        it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//方法1：关闭当前页面
                        startActivity(it);
                        //finish();//方法2：关闭当前页面
                    }
                })
                .setNegativeButton("取消",null).show();
    }
}