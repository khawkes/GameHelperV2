/*
 * COP4331C - Class Project - The Game Helper App
 * Spring 2015
 *
 * Project authors:
 *   Mark Andrews
 *   Jacob Cassagnol
 *   Kurt Hawkes
 *   Tim McCarthy
 *   Andrew McKenzie
 *   Amber Stewart
 */

package game.gamehelper.DominoMT;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Mark Andrews on 4/11/2015.
 */
public class DetectedShape
{
    ArrayList<Point[]> rectangles = new ArrayList();
    //corners of rectangle and midpoints of long sides
    Point a;
    Point b;
    Point c;
    Point d;
    Point mpTop;
    Point mpBottom;

    double ab = 0;
    double bc = 0;
    double cd = 0;
    double da = 0;

    double lengthThreshold = .25;
    double circleThreshold = .50;
    double upperAreaThreshold = 3;
    double lowerAreaThreshold = .125;
    Bitmap usedShapes;
    Bitmap shapes;
    Canvas canvas = new Canvas();
    Paint paint = new Paint();


    ArrayList<Point[]> circles = new ArrayList();

    public DetectedShape(int width, int height)
    {
        usedShapes = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        shapes = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(usedShapes);
    }

    public boolean addShape(Point lb, Point lt, Point rt, Point rb, int option)
    {
        //redundant but makes it easier to visualize
        a = lb;
        b = lt;
        c = rt;
        d = rb;
        Point[] corners = new Point[7];
        if(getLength(a,b) > getLength(b,c) )
        {
            corners[0] = a;
            corners[1] = b;
            corners[2] = c;
            corners[3] = d;
        }
        else
        {
            corners[0] = b;
            corners[1] = c;
            corners[2] = d;
            corners[3] = a;

        }

        switch(option){
            case 0:
                rectangles.add(corners);
                break;
            case 1:
                circles.add(corners);
                break;
        }

        return true;
    }

    public boolean addCircle(Point t, Point r, Point b, Point l)
    {
        Point[] circle = new Point[7];
        circle[0] = t;
        circle[1] = r;
        circle[2] = b;
        circle[3] = l;

        circles.add(circle);
        return true;
    }

    public boolean checkSquare(Point b, Point l, Point t, Point r)
    {

        //must be square
        if (Math.abs(getLength(t, b) / getLength(l, r)) > 1 + circleThreshold)
        {
            return false;
        }
        if (Math.abs(getLength(t, b) / getLength(l, r)) < 1 - circleThreshold)
        {
            return false;
        }
        return true;
    }

    public boolean checkLong(Point a, Point b, Point c, Point d)
    {
        double side1 = getLength(a, b);
        double side2 = getLength(b, c);
        double sidetoside = Math.abs(side1 / side2);

        //must be rectangle
        if (sidetoside > 2.25 || (sidetoside < 1.85 && sidetoside > .625) || sidetoside < .375)
            return false;
        return true;
    }

    public int countSide(int side, Point[] a)
    {
        //counts the number of circles on one side of a rectangle
        int count = 0;
        ArrayList<Point[]> toRemove = new ArrayList<>();
        switch (side)
        {
            case 1:
                for (Point[] e : circles)
                {
                    //ignore if circle is found in the center of the rectangle
                    if (getLength(e[4], a[6]) < 10)
                        continue;

                    //check if center circle is inside rectangle ABCD
                    if (isInside(e[4], a[0], a[4], a[5], a[3]))
                    {
                        drawShape(e, 1);
                        count++;
                    }
                }
                break;
            case 2:
                for (Point[] e : circles)
                {
                    //ignore if circle is found in the center of the rectangle
                    if (getLength(e[4], a[6]) < 10)
                        continue;

                    //check if center circle is inside rectangle ABCD
                    if (isInside(e[4], a[4], a[1], a[2], a[5]))
                    {
                        toRemove.add(e);
                        drawShape(e, 2);
                        count++;
                    }
                }
                break;
            default:
                break;
        }

        return count;
    }

    public void deleteOutliers(int rem)
    {
        if( rem <= 0 )
        {
            calculateExtraPoints();
            return;
        }
        //filter out any static that got through the width and height ratio test
        //check that all rectangles and circles are about the same size

        double recAvgArea = 0;
        double circleAvgArea = 0;
        ArrayList<Point[]> toRemove = new ArrayList<>();

        for (Point[] r : rectangles)
        {
            recAvgArea += (getLength(r[0], r[1]) * getLength(r[1], r[2]));
        }
        recAvgArea /= rectangles.size();

        for (Point[] c : circles)
        {
            //area of bounding box is good enough
            circleAvgArea += (getLength(c[0], c[1]) * getLength(c[1], c[2]));
        }
        circleAvgArea /= circles.size();

        //remove rectangles that are substantially larger or smaller than the average
        for (Point[] r : rectangles)
        {
            if ((getLength(r[0], r[1]) * getLength(r[1], r[2])) > (upperAreaThreshold * recAvgArea)
                    || (getLength(r[0], r[1]) * getLength(r[1], r[2])) < (lowerAreaThreshold * recAvgArea))
            {
                toRemove.add(r);
            }
        }
        for (Point[] r : toRemove)
        {
            rectangles.remove(r);
            if(rem > 1)
            {
                //add object to category if one more pass is remaining
                circles.add(r);
            }
        }

        toRemove.clear();

        //remove circles that are substantially larger or smaller than the average
        for (Point[] c : circles)
        {
            if ((getLength(c[0], c[1]) * getLength(c[1], c[2])) > (upperAreaThreshold * circleAvgArea)
                    || (getLength(c[0], c[1]) * getLength(c[1], c[2])) < (lowerAreaThreshold * circleAvgArea))
            {
                toRemove.add(c);
            }
        }
        for (Point[] c : toRemove)
        {
            circles.remove(c);
        }

        toRemove.clear();
        deleteOutliers(rem - 1);
    }

    public int[][] getDominoes()
    {
        int[][] domlist = new int[rectangles.size()][2];
        int i = 0;

        //iterate through rectangles and record sides
        for (Point[] a : rectangles)
        {
            drawShape(a, 0);
            domlist[i][0] = countSide(1, a);
            domlist[i++][1] = countSide(2, a);
        }
        return domlist;
    }

    public boolean isInside(Point e, Point a, Point b, Point c, Point d)
    {
        //using heron's formula to determine if point is inside shape

        //if the sum of the areas of all triangles generated using point e is larger than the area
        //of the rectangle, then the point is outside
        ab = getLength(a, b);
        bc = getLength(b, c);
        cd = getLength(c, d);
        da = getLength(d, a);

        double ae = getLength(a, e);
        double be = getLength(b, e);
        double ce = getLength(c, e);
        double de = getLength(d, e);

        double u1 = (ab + ae + be) / 2;
        double u2 = (bc + be + ce) / 2;
        double u3 = (cd + ce + de) / 2;
        double u4 = (da + de + ae) / 2;

        double a1 = Math.sqrt(u1 * (u1 - ab) * (u1 - ae) * (u1 - be));
        double a2 = Math.sqrt(u2 * (u2 - bc) * (u2 - be) * (u2 - ce));
        double a3 = Math.sqrt(u3 * (u3 - cd) * (u3 - ce) * (u3 - de));
        double a4 = Math.sqrt(u4 * (u4 - da) * (u4 - de) * (u4 - ae));

        double area = (ab * bc);
        double area2 = a1 + a2 + a3 + a4;

        if (area2 > area * 1.02)
        {
            return false;
        }
        return true;
    }

    public double getLength(Point a, Point b)
    {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    public Bitmap getUsedShapes()
    {
        return usedShapes;
    }

    public void drawShape(Point[] points, int option)
    {
        if(option == 0)
        {
            paint.setColor(Color.BLACK);
            canvas.drawLine(points[0].x, points[0].y, points[1].x, points[1].y, paint);
            canvas.drawLine(points[1].x, points[1].y, points[2].x, points[2].y, paint);
            canvas.drawLine(points[2].x, points[2].y, points[3].x, points[3].y, paint);
            canvas.drawLine(points[3].x, points[3].y, points[0].x, points[0].y, paint);
            paint.setColor(Color.RED);
            canvas.drawLine(points[4].x, points[4].y, points[5].x, points[5].y, paint);
        }
        else if(option == 1)
        {
            paint.setColor(Color.MAGENTA);
            canvas.drawLine(points[0].x, points[0].y, points[1].x, points[1].y, paint);
            canvas.drawLine(points[1].x, points[1].y, points[2].x, points[2].y, paint);
            canvas.drawLine(points[2].x, points[2].y, points[3].x, points[3].y, paint);
            canvas.drawLine(points[3].x, points[3].y, points[0].x, points[0].y, paint);
            canvas.drawPoint(points[4].x, points[4].y, paint);
        }
        else if(option == 2)
        {
            paint.setColor(Color.BLUE);
            canvas.drawLine(points[0].x, points[0].y, points[1].x, points[1].y, paint);
            canvas.drawLine(points[1].x, points[1].y, points[2].x, points[2].y, paint);
            canvas.drawLine(points[2].x, points[2].y, points[3].x, points[3].y, paint);
            canvas.drawLine(points[3].x, points[3].y, points[0].x, points[0].y, paint);
            canvas.drawPoint(points[4].x, points[4].y, paint);
        }
    }

    private Point getCenter(Point[] a)
    {
        return new Point((a[0].x + a[2].x) / 2, (a[1].y + a[3].y) / 2);
    }

    private void calculateExtraPoints()
    {
        //calculates middle of circle and midpoints of top and bottom of rectangles
        for(Point[] r: rectangles)
        {
            //find midpoints of long sides
            if (getLength(r[0], r[1]) > getLength(r[1], r[2]))
            {
                mpTop = new Point((r[0].x + r[1].x) / 2, (r[0].y + r[1].y) / 2);
                mpBottom = new Point((r[3].x + r[2].x) / 2, (r[2].y + r[3].y) / 2);
            }
            else
            {
                mpTop = new Point((r[2].x + r[1].x) / 2, (r[1].y + r[2].y) / 2);
                mpBottom = new Point((r[3].x + r[0].x) / 2, (r[3].y + r[0].y) / 2);
            }

            //corners 4 and 5 are midpoints, corner 6 is the center of the rectangle
            r[4] = mpTop;
            r[5] = mpBottom;
            r[6] = new Point((r[0].x + r[2].x) / 2, (r[1].y + r[3].y) / 2);
        }

        for(Point[] c: circles){
            c[4] = getCenter(c);
        }

    }

    public Bitmap getShapes(){
        //draw current categorization to bitmap
        calculateExtraPoints();
        canvas.setBitmap(shapes);

        for(Point[] r: rectangles)
        {
            drawShape(r, 0);
        }

        for(Point[] c: circles)
        {
            drawShape(c, 2);
        }

        canvas.setBitmap(usedShapes);
        return shapes;
    }
}
