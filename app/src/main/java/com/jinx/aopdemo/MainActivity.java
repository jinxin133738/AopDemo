package com.jinx.aopdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jinx.annotation.Permissions;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mTestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTestBtn = findViewById(R.id.mTestBtn);
        mTestBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.mTestBtn){
            askPermission();
        }
    }
    private static final String Tag = "测试RxPermisiion";

    @Permissions(permissions = {Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_WIFI_STATE},
    explain = "我需要申请设备标识，获取使用Wi-Fi等WLAN无线网络权限")
    public void askPermission(){
        Toast.makeText(MainActivity.this,"被切面方法",Toast.LENGTH_LONG).show();
    }
}
