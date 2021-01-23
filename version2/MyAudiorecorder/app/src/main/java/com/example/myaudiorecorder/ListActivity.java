package com.example.myaudiorecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

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
        //返回上一级
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}