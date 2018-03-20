package com.duanyy.media.glutil;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yangyu on 16/6/29.
 */
public class GeometryTool {

    public static PointF getFaceDeltaPoint(PointF startPt, PointF endPt, float outrate, float updown) {
        PointF tempPt = new PointF(endPt.x-startPt.x, endPt.y-startPt.y);
        PointF dstPt = new PointF();
        float tempBeta = 0.0f;
        if (updown < 0)
            tempBeta = (float)-Math.sqrt(-updown);
        else
            tempBeta = (float) Math.sqrt(updown);
        dstPt.x = startPt.x + outrate * tempPt.x + updown * tempPt.y;
        dstPt.y = startPt.y + outrate * tempPt.y - updown * tempPt.x;
        return dstPt;
    }

    public static PointF getCenterPoint(List<PointF> pointList) {
        if (pointList == null)
            return null;
        PointF centerPt = new PointF();
        float sumx=0.0f, sumy=0.0f;
        for (int i=0; i<pointList.size(); i++) {
            sumx += pointList.get(i).x;
            sumy += pointList.get(i).y;
        }
        centerPt.x = sumx/pointList.size();
        centerPt.y = sumy/pointList.size();
        return centerPt;
    }

    public static RectF getPointsRect(List<PointF> pointList) {
        float maxX= Float.MIN_VALUE, maxY= Float.MIN_VALUE;
        float minX= Float.MAX_VALUE, minY= Float.MAX_VALUE;
        for (int i=0; i<pointList.size(); i++) {
            maxX = maxX<pointList.get(i).x ? pointList.get(i).x : maxX;
            maxY = maxY<pointList.get(i).y ? pointList.get(i).y : maxY;
            minX = minX>pointList.get(i).x ? pointList.get(i).x : minX;
            minY = minY>pointList.get(i).y ? pointList.get(i).y : minY;
        }
        RectF rect = new RectF(minX, minY, maxX, maxY);
        return rect;
    }

    public static float getPointsDist(PointF pt1, PointF pt2) {
        if (pt1 == null || pt2 == null)
            return 0.0f;
        return (float) Math.sqrt((pt1.x-pt2.x)*(pt1.x-pt2.x) + (pt1.y-pt2.y)*(pt1.y-pt2.y));
    }


    public static float getValueFromSpLineLUT(ArrayList<Float> lut, float minVal, float curVal, float maxVal, int type) {
         if (lut == null)
             return 0.0f;
        if (lut.size() != 256)
            return 0.0f;

        float tempVal = Math.max(minVal, Math.min(curVal, maxVal));
        int curIndex = (int)(255.0f *(tempVal-minVal)/(maxVal-minVal));

        if (curIndex > lut.size() - 1)
            return 0.0f;
        float dstVal = 0.0f;
        if (type == 0) // 0~1
            dstVal = lut.get(curIndex)/255.0f;
        else  // -1 ~ 1
            dstVal = lut.get(curIndex)/128.0f - 1.0f;

        return dstVal;
    }

    //spline curve
    public static ArrayList<Float> createSplineCurve(PointF[] points) {
        if (points == null || points.length <= 0) {
            return null;
        }

        // Sort the array
        PointF[] pointsSorted = points.clone();
        Arrays.sort(pointsSorted, new Comparator<PointF>() {
            @Override
            public int compare(PointF point1, PointF point2) {
                if (point1 == null || point2 == null)
                    return 0;
                if (point1.x < point2.x) {
                    return -1;
                } else if (point1.x > point2.x) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        // Convert from (0, 1) to (0, 255).
        Point[] convertedPoints = new Point[pointsSorted.length];
        for (int i = 0; i < points.length; i++) {
            PointF point = pointsSorted[i];
            convertedPoints[i] = new Point((int) (point.x * 255), (int) (point.y * 255));
        }

        ArrayList<Point> splinePoints = createSplineCurve2(convertedPoints);

        // If we have a first point like (0.3, 0) we'll be missing some points at the beginning
        // that should be 0.
        Point firstSplinePoint = splinePoints.get(0);
        if (firstSplinePoint.x > 0) {
            for (int i = firstSplinePoint.x-1; i >= 0; i--) {
                splinePoints.add(0, new Point(i, 0));
            }
        }

        // Insert points similarly at the end, if necessary.
        Point lastSplinePoint = splinePoints.get(splinePoints.size() - 1);
        if (lastSplinePoint.x < 255) {
            for (int i = lastSplinePoint.x + 1; i <= 255; i++) {
                splinePoints.add(new Point(i, 255));
            }
        }

        // Prepare the spline points.
        ArrayList<Float> preparedSplinePoints = new ArrayList<Float>(splinePoints.size());
        for (Point newPoint : splinePoints) {
            Point origPoint = new Point(newPoint.x, newPoint.x);

            float distance = (float) Math.sqrt(Math.pow((origPoint.x - newPoint.x), 2.0) + Math.pow((origPoint.y - newPoint.y), 2.0));

            if (origPoint.y > newPoint.y) {
                distance = -distance;
            }

            preparedSplinePoints.add(distance);
        }

        return preparedSplinePoints;
    }

    private static ArrayList<Point> createSplineCurve2(Point[] points) {
        ArrayList<Double> sdA = createSecondDerivative(points);

        // Is [points count] equal to [sdA count]?
//    int n = [points count];
        int n = sdA.size();
        if (n < 1) {
            return null;
        }
        double sd[] = new double[n];

        // From NSMutableArray to sd[n];
        for (int i = 0; i < n; i++) {
            sd[i] = sdA.get(i);
        }


        ArrayList<Point> output = new ArrayList<Point>(n + 1);

        for (int i = 0; i < n - 1; i++) {
            Point cur = points[i];
            Point next = points[i + 1];

            for (int x = cur.x; x < next.x; x++) {
                double t = (double) (x - cur.x) / (next.x - cur.x);

                double a = 1 - t;
                double b = t;
                double h = next.x - cur.x;

                double y = a * cur.y + b * next.y + (h * h / 6) * ((a * a * a - a) * sd[i] + (b * b * b - b) * sd[i + 1]);

                if (y > 255.0) {
                    y = 255.0;
                } else if (y < 0.0) {
                    y = 0.0;
                }

                output.add(new Point(x, (int) Math.round(y)));
            }
        }

        // If the last point is (255, 255) it doesn't get added.
        if (output.size() == 255) {
            output.add(points[points.length - 1]);
        }
        return output;
    }

    private static ArrayList<Double> createSecondDerivative(Point[] points) {
        int n = points.length;
        if (n <= 1) {
            return null;
        }

        double matrix[][] = new double[n][3];
        double result[] = new double[n];
        matrix[0][1] = 1;
        // What about matrix[0][1] and matrix[0][0]? Assuming 0 for now (Brad L.)
        matrix[0][0] = 0;
        matrix[0][2] = 0;

        for (int i = 1; i < n - 1; i++) {
            Point P1 = points[i - 1];
            Point P2 = points[i];
            Point P3 = points[i + 1];

            matrix[i][0] = (double) (P2.x - P1.x) / 6;
            matrix[i][1] = (double) (P3.x - P1.x) / 3;
            matrix[i][2] = (double) (P3.x - P2.x) / 6;
            result[i] = (double) (P3.y - P2.y) / (P3.x - P2.x) - (double) (P2.y - P1.y) / (P2.x - P1.x);
        }

        // What about result[0] and result[n-1]? Assuming 0 for now (Brad L.)
        result[0] = 0;
        result[n - 1] = 0;

        matrix[n - 1][1] = 1;
        // What about matrix[n-1][0] and matrix[n-1][2]? For now, assuming they are 0 (Brad L.)
        matrix[n - 1][0] = 0;
        matrix[n - 1][2] = 0;

        // solving pass1 (up->down)
        for (int i = 1; i < n; i++) {
            double k = matrix[i][0] / matrix[i - 1][1];
            matrix[i][1] -= k * matrix[i - 1][2];
            matrix[i][0] = 0;
            result[i] -= k * result[i - 1];
        }
        // solving pass2 (down->up)
        for (int i = n - 2; i >= 0; i--) {
            double k = matrix[i][2] / matrix[i + 1][1];
            matrix[i][1] -= k * matrix[i + 1][0];
            matrix[i][2] = 0;
            result[i] -= k * result[i + 1];
        }

        ArrayList<Double> output = new ArrayList<Double>(n);
        for (int i = 0; i < n; i++) output.add(result[i] / matrix[i][1]);

        return output;
    }
}
