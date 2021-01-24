package com.example.myaudiorecorder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
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
    private String filename;

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

//        //功能一：设置长按监听事件，进行删除
//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                File file= list.get(position);
//                String filename=file.getName();
//                Toast.makeText(getApplicationContext(),"you choose the file : "+filename,Toast.LENGTH_SHORT).show();
//                //弹框确定是否删除
//                myDialog(filename);
//
//                return true;
//                //关于返回值，若返回False，则是当长按时，既调用onItemLongClick，同时调用onItemLongClick后
//                //还会调用onItemClick，就是说会同时调用onItemLongClick，和onItemClick，
//                //若返回true，则只调用onItemLongClick
//            }
//        });

        //功能二：长按不仅能选择删除，还能选择分享


        //方法1：使用ContextMenu
        registerForContextMenu(listView);

        //方法2：使用PopupMenu
//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                File file= list.get(position);
//                filename=file.getName();
//                Toast.makeText(getApplicationContext(),"you choose the file : "+filename,Toast.LENGTH_SHORT).show();
//
//                showPopupMenu(view);
//                return true;
//            }
//        });


        //返回上一级
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    //功能二两种方法相关代码
    //实验证明，ContextMenu更好，可以直接在所按下之处的正下方显示，但是PopupMenu在Item的正下方显示

    /************************功能一种方法1实现**************************/
    //方法1对应
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        filename=list.get(info.position).getName();//保存选中的文件名
        Toast.makeText(getApplicationContext(),"you choose the file : "+filename,Toast.LENGTH_SHORT).show();
        menu.add(0,0,0,"删除");
        menu.add(0,1,0,"分享");
    }
    //方法1对应
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 0: {
                myDialog(filename);
                break;
            }
            case 1:{
                share(filename);
                break;
            }
        }
        return true;
    }
    /******************************************************************/

    /******************功能二中方法2实现相关************************/

    private void showPopupMenu(View v){
        //定义PopupMenu对象
        PopupMenu popupMenu = new PopupMenu(this, v);
        //设置PopupMenu对象的布局
        popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());
        //设置PopupMenu的点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.deleteFile:{
                        myDialog(filename);
                        break;
                    }
                    case R.id.shareFile:{
                        share(filename);
                        break;
                    }
                }

                return true;
            }
        });

        //显示菜单
        popupMenu.show();

    }
    /**********************************************************************/


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

    //应用间分享文件!!!!!
    private void share(String filename){
        //创建要分享的文件
        File file=new File(Environment.getExternalStorageDirectory(),"ARecordFiles/"+filename);
        Intent share = new Intent(Intent.ACTION_SEND);
        Uri uri;

        //Android7.0版本以上使用FileProvider
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            uri = FileProvider.getUriForFile(
                    getApplicationContext(),
                    "com.example.myaudiorecorder.fileprovider",
                    file);//第二个参数为你的包名.fileprovider
        }
        else{
            uri = Uri.fromFile(file);
        }

        share.setType("*/*");//此处可发送多种文件

        share.putExtra(Intent.EXTRA_STREAM, uri);

        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//这一句一定得写

        startActivity(share);
    }
}