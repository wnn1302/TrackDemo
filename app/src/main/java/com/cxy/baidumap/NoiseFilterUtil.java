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

    private static final double HUMAN_WALK_SPEED = 1.39;//m/s
    private static final double HUMAN_RUN_SPEED = 2.78;//m/s
    private static final double VEHICLE_SPEED = 22.22;//m/s
    private static final double MODE_STATIC = 0;//静止状态

    public static LatLng filterNoise(LatLng firstLatlng, BDLocation location, int scanSpan) {
        LatLng currLatlng = new LatLng(location.getLatitude(), location.getLongitude());
        double currSpeedMode = 0;

        //设置首点
        if (firstPoint == null) {
            firstPoint = firstLatlng;
            System.out.println("----> make sure firstPoint.");
            return null;
        }

        //判断当前运动模式
        if (location.getSpeed() == 0) {//静止
            currSpeedMode = MODE_STATIC;
        } else if (0 < location.getSpeed() && location.getSpeed() < HUMAN_WALK_SPEED) { //运动
            currSpeedMode = HUMAN_WALK_SPEED;
        } else if (HUMAN_WALK_SPEED < location.getSpeed() && location.getSpeed() < HUMAN_RUN_SPEED) {//运动-跑步
            currSpeedMode = HUMAN_RUN_SPEED;
        } else if (HUMAN_RUN_SPEED < location.getSpeed() && location.getSpeed() < VEHICLE_SPEED) {//运动-摩托/电动
            currSpeedMode = VEHICLE_SPEED;
        }

        //首点获取之后的第一个点的上一个状态与当前状态认为是一致的
        if (speedMode == -1) {
            speedMode = currSpeedMode;
        }

        //速度模式切换，time必置0，重新开始计算
        if (speedMode != currSpeedMode) {
            if (null != checkLatlngWhenSpeedChange(currSpeedMode, scanSpan, currLatlng)) {
                time = 0;
            } else {
                time = 1;
            }
            speedMode = currSpeedMode;
        }

        //如果是保持静止状态无论漂移多少次都不累计
        if (speedMode == MODE_STATIC) {

        } else {//如果是保持运动状态累计漂移次数
            time++;
            System.out.println("----> speed mode:" + speedMode);
            System.out.println("----> time:" + time);
            double currMaxDistance = speedMode * scanSpan * time;
            double distance = DistanceUtil.getDistance(firstPoint, currLatlng);
            if (0 < distance && distance < currMaxDistance) {
                time = 0;
                firstPoint = currLatlng;
                System.out.println("----> return latlng");
                return currLatlng;
            }
        }

        return null;
    }

    /**
     * 初次切换态时，有一个减速的过程，
     * 根据两种状态的平均速度判断下一个状态的首点
     */
    private static LatLng checkLatlngWhenSpeedChange(double currSpeedMode, double scanSpan, LatLng currLatlng) {
        //从运动的最后一个点减速到新状态的第一个点之间最大理论距离
        double currMaxDistance = (speedMode + currSpeedMode) / 2 * scanSpan;
        double distance = DistanceUtil.getDistance(firstPoint, currLatlng);
        if (distance < currMaxDistance) {
            firstPoint = currLatlng;
            speedMode = currSpeedMode;
            return currLatlng;
        }
        return null;
    }

    //中位值+均值滤波
/*  private static int N = 12;

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
