package com.cxy.baidumap;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TD01 on 2017/10/23.
 */

public class DouglasPeuckerUtil {

    public static List<LatLng> DouglasPeucker(List<LatLng> points, double epsilon) {
        // 找到最大阈值点，即操作（1）
        double maxH = 0;
        int index = 0;
        int end = points.size();
        for (int i = 1; i < end - 1; i++) {
            double h = H(points.get(i), points.get(0), points.get(end - 1));
            if (h > maxH) {
                maxH = h;
                index = i;
            }
        }

        // 如果存在最大阈值点，就进行递归遍历出所有最大阈值点
        List<LatLng> result = new ArrayList<>();
        if (maxH > epsilon) {
            List<LatLng> leftPoints = new ArrayList<>();// 左曲线
            List<LatLng> rightPoints = new ArrayList<>();// 右曲线
            // 分别提取出左曲线和右曲线的坐标点
            for (int i = 0; i < end; i++) {
                if (i <= index) {
                    leftPoints.add(points.get(i));
                    if (i == index)
                        rightPoints.add(points.get(i));
                } else {
                    rightPoints.add(points.get(i));
                }
            }

            // 分别保存两边遍历的结果
            List<LatLng> leftResult = new ArrayList<>();
            List<LatLng> rightResult = new ArrayList<>();
            leftResult = DouglasPeucker(leftPoints, epsilon);
            rightResult = DouglasPeucker(rightPoints, epsilon);

            // 将两边的结果整合
            rightResult.remove(0);
            leftResult.addAll(rightResult);
            result = leftResult;
        } else {// 如果不存在最大阈值点则返回当前遍历的子曲线的起始点
            result.add(points.get(0));
            result.add(points.get(end - 1));
        }
        return result;
    }

    /**
     * 计算点到直线的距离
     *
     * @param p
     * @param s
     * @param e
     * @return
     */
    public static double H(LatLng p, LatLng s, LatLng e) {
        double AB = DistanceUtil.getDistance(s, e);
        double CB = DistanceUtil.getDistance(p, s);
        double CA = DistanceUtil.getDistance(p, e);

        double S = helen(CB, CA, AB);
        double H = 2 * S / AB;

        return H;
    }

    /**
     * 海伦公式，已知三边求三角形面积
     *
     * @param CB
     * @param CA
     * @param AB
     * @return 面积
     */
    public static double helen(double CB, double CA, double AB) {
        double p = (CB + CA + AB) / 2;
        double S = Math.sqrt(p * (p - CB) * (p - CA) * (p - AB));
        return S;
    }
}
