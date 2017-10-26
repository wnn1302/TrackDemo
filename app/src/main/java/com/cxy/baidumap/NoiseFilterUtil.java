package com.cxy.baidumap;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

/**
 * Created by TD01 on 2017/10/25.
 */

public class NoiseFilterUtil {
    private static int time;//轨迹漂移次数记录

    private static double speedMode = -1;//速度模式

    private static LatLng firstPoint;

    private static double currMaxDistance;

    private static final double HUMAN_WALK_SPEED_NORMAL = 1.39;//m/s
    private static final double HUMAN_RUN_SPEED_NORMAL = 2.78;//m/s
    private static final double VEHICLE_SPEED_NORMAL = 22.22;//m/s
    private static final double MODE_STATIC = 0;//静止状态

    public static LatLng filterNoise(LatLng firstLatlng, BDLocation location, int scanSpan) {
        LatLng currLatlng = new LatLng(location.getLatitude(), location.getLongitude());
        double currSpeedMode = 0;
        if (firstPoint == null) {
            firstPoint = firstLatlng;
        } else {
            if (location.getSpeed() == 0) {//静止
                currSpeedMode = MODE_STATIC;
            } else if (0 < location.getSpeed()) {//运动
                if (location.getSpeed() < HUMAN_WALK_SPEED_NORMAL) { //运动
                    //运动状态切换时重置其他运动状态下的漂移记录
                    currSpeedMode = HUMAN_WALK_SPEED_NORMAL;
                } else if (HUMAN_WALK_SPEED_NORMAL < location.getSpeed() && location.getSpeed() < HUMAN_RUN_SPEED_NORMAL) {//运动-跑步
                    currSpeedMode = HUMAN_RUN_SPEED_NORMAL;
                } else if (HUMAN_RUN_SPEED_NORMAL < location.getSpeed() && location.getSpeed() < VEHICLE_SPEED_NORMAL) {//运动-摩托/电动
                    currSpeedMode = VEHICLE_SPEED_NORMAL;
                }
            }

            //首点获取之后的第一个点的上一个状态与当前状态认为是一样的
            if (speedMode == -1) {
                speedMode = currSpeedMode;
            }

            //速度模式切换了，time必置0，相当于重新开始
            if (speedMode != currSpeedMode) time = 0;

            checkLatlng(currLatlng, currSpeedMode, scanSpan);
        }
        return null;
    }

    private static LatLng checkLatlng(LatLng currLatlng, double currSpeedMode, int scanSpan) {
        if (currSpeedMode == MODE_STATIC) {
            return firstPoint;
        } else {
            time++;
            currMaxDistance = speedMode * scanSpan * time;
            double distance = DistanceUtil.getDistance(firstPoint, currLatlng);
            if (0 < distance && distance < currMaxDistance) {
                time = 0;
                firstPoint = currLatlng;
                return currLatlng;
            }
        }
        return null;
    }

    //中位值+均值滤波
/*    private static int N = 12;

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
    }*/

}
