package com.cxy.baidumap;

import android.graphics.Color;
import android.os.Environment;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CXY-PC on 2017/5/02.
 */
public class BaiduMapFragment extends SupportMapFragment {
    private LocationClient mBDLocClient;
    private MyBDLocationListenner mBDLocListener = new MyBDLocationListenner();

    private List<LatLng> trackPoints = new ArrayList<>();
    private Polyline trackLine = null;

    private static final double HUMAN_WALK_SPEED_NORMAL = 1.39;//m/s
    private static final double MOTO_SPEED_NORMAL = 22.22;//m/s
    private static final double STATIC_LOCATION_ALLOWABLE_ERROR = 3.0;//静态定位允许误差：3m
    private double maxDistance;

    //运动模式
    public static final int TRACK_MODEL_WALK = 0;
    public static final int TRACK_MODEL_MOTO = 1;

    //定位间隔
    private int scanSpan = 3000;

    //是否开始轨迹
    private boolean isSureFirstPoint;
    private List<LatLng> allTrackPoints = new ArrayList<>();

    @Override
    public void onStart() {
        super.onStart();
        initMap();
        initMapLoaction();
//        List<LatLng> points = readPoints();
//        List<LatLng> resultPoints = new ArrayList<>();
//        //noiseFiltering(points);
//        for (LatLng ll : points) {
//            LatLng result = NoiseFilterUtil.nosiseFiltering(points.get(0), ll, 44.44);
//            if (null != result) {
//                resultPoints.add(result);
//            }
//        }
//        resultPoints = simplify(resultPoints);
//        System.out.println("----> points size: " + resultPoints.size());
//        addPolyline(resultPoints);
//        LatLng ll = new LatLng(26.023313, 119.418714);
//        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
//        getBaiduMap().animateMapStatus(u);//移动到定位点
    }

    private List<LatLng> readPoints() {
        InputStream is;
        InputStreamReader isr;
        BufferedReader br;
        List<LatLng> lls = new ArrayList<>();
        try {
            is = getResources().getAssets().open("All_Track_Points.txt");
            isr = new InputStreamReader(is);// 字符流
            br = new BufferedReader(isr);// 缓冲流
            String str;
            while ((str = br.readLine()) != null) {
                String[] llStr = str.split(",");
                double la = Double.valueOf(llStr[0]);
                double lo = Double.valueOf(llStr[1]);
                lls.add(new LatLng(la, lo));
            }
            is.close();
            isr.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("----> 文件读取错误！");
        }

        System.out.println("----> points size before simplify: " + lls.size());
        return lls;
    }

    private List<LatLng> simplify(List<LatLng> points) {
        List<LatLng> lls = DouglasPeuckerUtil.DouglasPeucker(points, 1.35);//阈值单位：米
        System.out.println("----> points size after simplify: " + lls.size());
        return lls;
    }

    private void addMarker(LatLng latLng) {
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.mipmap.marker);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(latLng)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        getBaiduMap().addOverlay(option);
    }

    private void addPolyline(List<LatLng> lls) {
        if (lls.size() < 2) return;

        if (trackLine == null) {
            OverlayOptions options = new PolylineOptions()
                    .color(Color.RED)
                    .width(10)
                    .points(lls);
            trackLine = (Polyline) getBaiduMap().addOverlay(options);
        } else {
            trackLine.setPoints(lls);
        }
    }

    private void initMap() {
        getBaiduMap().setMapType(BaiduMap.MAP_TYPE_NORMAL);
        getBaiduMap().setMapStatus(MapStatusUpdateFactory.zoomTo(16));
        getBaiduMap().setMyLocationEnabled(true);
        getBaiduMap().setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));
    }

    private void initMapLoaction() {
        if (getBaiduMap() == null) return;

        ///LocationClientOption类用来设置定位SDK的定位方式，
        LocationClientOption option = new LocationClientOption(); //以下是给定位设置参数
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(scanSpan);

        mBDLocClient = new LocationClient(getActivity().getApplicationContext());
        mBDLocClient.registerLocationListener(mBDLocListener);
        mBDLocClient.setLocOption(option);
        mBDLocClient.start();
    }

    private void updateBDMapStatus(BDLocation location) {
        BaiduMap map = getBaiduMap();

        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        map.setMyLocationData(locData);

        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
        map.animateMapStatus(u);//移动到定位点
        allTrackPoints.add(ll);
        //绘制轨迹
        drawTrackLine(location);
    }

    /**
     * 根据运动模式计算滤噪阈值
     *
     * @param model
     */
    public void setTrackModel(int model) {
        double currSpeed;
        if (model == TRACK_MODEL_WALK)
            currSpeed = HUMAN_WALK_SPEED_NORMAL;
        else
            currSpeed = MOTO_SPEED_NORMAL;

        maxDistance = currSpeed * scanSpan / 1000;
    }

    private void drawTrackLine(BDLocation location) {
        LatLng currPoint = new LatLng(location.getLatitude(), location.getLongitude());
        //选取起始点
        if (trackPoints.size() == 0 && isSureFirstPoint) {
            trackPoints.add(currPoint);
            return;
        } else if (trackPoints.size() > 0) {
            //过滤噪点
            if (null != NoiseFilterUtil.nosiseFiltering(trackPoints.get(0), location, maxDistance)) {
                trackPoints.add(currPoint);
                //绘制轨迹
                addPolyline(trackPoints);
                System.out.println("----> track point size:" + trackPoints.size());
            }
        }
    }

    /**
     * 人工确认起始点是否正确
     */
    public void makeSureFirstPoint() {
        if (!isSureFirstPoint)
            isSureFirstPoint = true;
    }

    public void outputSpeed() {
        File file = new File(Environment.getExternalStorageDirectory() + "/TrackSpeed/" + System.currentTimeMillis() + ".txt");
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            OutputStream out = new FileOutputStream(file);
            out.write(speedBuffer.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void outputAllTrackPoints() {
        StringBuffer buffer = new StringBuffer();
        for (LatLng ll : allTrackPoints) {
            buffer.append(ll.latitude + "," + ll.longitude + "\n");
        }

        File file = new File(Environment.getExternalStorageDirectory() + "/TrackPoints/All_Track_Points.txt");
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            OutputStream out = new FileOutputStream(file);
            out.write(buffer.toString().getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    StringBuffer speedBuffer = new StringBuffer();

    public class MyBDLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            final BaiduMap map = getBaiduMap();
            if (map == null || location == null || getActivity() == null)
                return;
            updateBDMapStatus(location);
            speedBuffer.append(String.valueOf(location.getSpeed() + "," + location.getRadius()) + "\n");
        }
    }

}
