package com.cxy.baidumap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.baidu.mapapi.SDKInitializer;

public class MainActivity extends AppCompatActivity {

    public String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private BaiduMapFragment mapFragment;

    private Button btn_walk;
    private Button btn_moto;
    private Button btn_start;
    private Button btn_output;
    private Button btn_follow;
    private Button btn_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        requestPermissions();
        addMapFragment();
        initView();
    }

    /**
     * 请求权限
     */
    private void requestPermissions() {
        int GRANTED = PackageManager.PERMISSION_GRANTED;
        if (ContextCompat.checkSelfPermission(this, PERMISSIONS[0]) != GRANTED
                || ContextCompat.checkSelfPermission(this, PERMISSIONS[1]) != GRANTED
                || ContextCompat.checkSelfPermission(this, PERMISSIONS[2]) != GRANTED
                || ContextCompat.checkSelfPermission(this, PERMISSIONS[3]) != GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }
    }

    private void addMapFragment() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        mapFragment = new BaiduMapFragment();
        fm.beginTransaction().replace(R.id.container, mapFragment).commit();
    }

    private void initView() {
        btn_walk = (Button) findViewById(R.id.btn_walk);
        btn_moto = (Button) findViewById(R.id.btn_moto);
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_output = (Button) findViewById(R.id.btn_output);
        btn_follow = (Button) findViewById(R.id.btn_follow);
        btn_location = (Button) findViewById(R.id.btn_location);

        btn_walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragment.setTrackModel(BaiduMapFragment.TRACK_MODEL_WALK);
                btn_walk.setEnabled(false);
                btn_moto.setEnabled(true);
            }
        });

        btn_moto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragment.setTrackModel(BaiduMapFragment.TRACK_MODEL_MOTO);
                btn_moto.setEnabled(false);
                btn_walk.setEnabled(true);
            }
        });
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragment.makeSureFirstPoint();
                btn_start.setEnabled(false);
            }
        });
        btn_output.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragment.outputAllTrackPoints();
                btn_output.setEnabled(false);
            }
        });
        btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragment.setMyLocationMode();
                btn_follow.setEnabled(false);
            }
        });
        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragment.gotoMyLocation();
                btn_location.setEnabled(false);
            }
        });
    }


}
