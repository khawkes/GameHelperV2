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

package game.gamehelper.DominoMT.ComputerVision;

import android.graphics.Point;

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
    double circleThreshold = .25;
    double upperAreaThreshold = 2.5;
    double lowerAreaThreshold = .25;

    ArrayList<Point[]> circles = new ArrayList();

    public DetectedShape()
    {
    }

    public boolean addRectangle(Point lb, Point lt, Point rb, Point rt)
    {
        //redundant but makes it easier to visualize
        a = lt;
        b = rt;
        c = rb;
        d = lb;

        Point[] corners = new Point[7];
        corners[0] = a;
        corners[1] = b;
        corners[2] = c;
        corners[3] = d;

        //find midpoints of long sides
        if (getLength(a, b) > getLength(b, c))
        {
            mpTop = new Point((a.x + b.x) / 2, (a.y + b.y) / 2);
            mpBottom = new Point((d.x + c.x) / 2, (c.y + d.y) / 2);
        }
        else
        {
            mpTop = new Point((c.x + b.x) / 2, (b.y + c.y) / 2);
            mpBottom = new Point((d.x + a.x) / 2, (d.y + a.y) / 2);
        }

        //corners 4 and 5 are midpoints, corner 6 is the center of the rectangle
        corners[4] = mpTop;
        corners[5] = mpBottom;
        corners[6] = new Point((a.x + c.x) / 2, (b.y + d.y) / 2);

        rectangles.add(corners);
        return true;
    }

    public boolean addCircle(Point t, Point r, Point b, Point l)
    {
        Point[] circle = new Point[5];
        circle[0] = t;
        circle[1] = r;
        circle[2] = b;
        circle[3] = l;

        //circle center
        circle[4] = new Point((l.x + r.x) / 2, (t.y + b.y) / 2);

        circles.add(circle);
        return true;
    }

    public boolean checkSquare(Point t, Point r, Point b, Point l)
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
        double sidetoside = side1 / side2;

        //must be rectangle
        if (sidetoside > 2.25 || (sidetoside < 1.75 && sidetoside > .625) || sidetoside < .375)
            return false;
        return true;
    }

    public int countSide(int side, Point[] a)
    {
        //counts the number of circles on one side of a rectangle
        int count = 0;
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
                        count++;
                    }
                }
                break;
            default:
                break;
        }

        return count;
    }

    public void deleteOutliers()
    {
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
    }

    public int[][] getDominoes()
    {
        int[][] domlist = new int[rectangles.size()][2];
        int i = 0;

        //iterate through rectangles and record sides
        for (Point[] a : rectangles)
        {
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

        if (area2 > area * 1.05)
        {
            return false;
        }
        return true;
    }

    public double getLength(Point a, Point b)
    {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }
}
