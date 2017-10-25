package com.cxy.baidumap;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.mapapi.utils.DistanceUtil.getDistance;

/**
 * Created by TD01 on 2017/10/25.
 */

public class NoiseFilterUtil {

    private static int N = 12;

    private static int n = 0;

    private static List<LatLng> points = new ArrayList<>();

    private static double[] distances = new double[N];

    private static double temp;

    public static LatLng medianAverageFiltering(LatLng ll, double maxDistance) {
        if (n < N + 1) {
            points.add(ll);
            n++;
            if (n == N + 1) {
//                double sum = 0;
//                double average = 0;
                double minDiff = 0;
                int index = 0;
                //计算距离数组
                for (int i = 0; i < points.size() - 1; i++) {
                    distances[i] = getDistance(points.get(i), points.get(i + 1));
                }
                //排序，升序
                for (int i = 0; i < distances.length - 1; i++) {
                    for (int j = 0; j < distances.length - i - 1; j++) {
                        if (distances[i] > distances[i + 1]) {
                            temp = distances[i];
                            distances[i] = distances[i + 1];
                            distances[i] = temp;
                        }
                    }
                }
                //去头尾，算平均
//                for (int i = 1; i < N - 1; i++) {
//                    sum += distances[i];
//                    average = sum / (N - 2);
//                }
                //去头尾，找出最接近阈值的那个
                for (int i = 1; i < N - 1; i++) {
                    double diff = Math.abs(distances[i] - maxDistance);
                    if (diff < minDiff) {
                        minDiff = diff;
                        index = i;
                    }
                }
                //根据差值数据的index算出对应的坐标点
                LatLng result = points.get(index + 1);
                points.clear();
                n = 0;
                return result;
            }
        }
        return null;
    }

    private static int time;

    private static double currMaxDistance;

    private static LatLng firstPoint;

    public static LatLng nosiseFiltering(LatLng firstLatlng, BDLocation location, double maxDistance) {
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        if (firstPoint == null) {
            firstPoint = firstLatlng;
        } else {
            if (location.getSpeed() == 0) {//静止
                time = 0;
                firstPoint = ll;
            } else if (location.getSpeed() > 0) {//运动
                time++;
                currMaxDistance = maxDistance * time;//maxDistance需要随比较的次数递增
                double distance = DistanceUtil.getDistance(firstPoint, ll);
                if (distance > 0 && distance < currMaxDistance) {//运动中
                    firstPoint = ll;
                    time = 0;
                    currMaxDistance = 0;
                    return ll;
                }
            }
        }
        return null;
    }
}
