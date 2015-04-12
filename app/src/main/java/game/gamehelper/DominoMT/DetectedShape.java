package game.gamehelper.DominoMT;

import android.graphics.Point;

import java.util.ArrayList;

/**
 * Created by Mark Andrews on 4/11/2015.
 */
public class DetectedShape {
    ArrayList<Point[]> rectangles = new ArrayList();
    //corners of rectangle
    Point a;
    Point b;
    Point c;
    Point d;

    double abSlope = 0;
    double bcSlope = 0;
    double cdSlope = 0;
    double daSlope = 0;
    double abLength = 0;
    double bcLength = 0;
    double cdLength = 0;
    double daLength = 0;

    double lengthThreshold = .25;
    double circleThreshold = .25;

    Point mpTop;
    Point mpBottom;
    double mpSlope = 0;

    ArrayList<Point[]> circles = new ArrayList();

    public DetectedShape(){
    }

    public boolean addRectangle(Point lb, Point lt, Point rb, Point rt) {
        a = lt;
        b = rt;
        c = rb;
        d = lb;
        Point[] corners = new Point[7];
        corners[0] = a;
        corners[1] = b;
        corners[2] = c;
        corners[3] = d;

        abLength = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
        bcLength = Math.sqrt(Math.pow(b.x - c.x, 2) + Math.pow(b.y - c.y, 2));
        cdLength = Math.sqrt(Math.pow(c.x - d.x, 2) + Math.pow(c.y - d.y, 2));
        daLength = Math.sqrt(Math.pow(d.x - a.x, 2) + Math.pow(d.y - a.y, 2));

        abSlope = (double) (a.y - b.y) / (a.x - b.x);
        bcSlope = (double) (b.y - c.y) / (b.x - c.x);
        cdSlope = (double) (c.y - d.y) / (c.x - d.x);
        daSlope = (double) (d.y - a.y) / (d.x - a.x);

        if(abLength > bcLength){
            mpTop = new Point((a.x+b.x)/2, (a.y+b.y)/2);
            mpBottom = new Point((d.x+c.x)/2, (c.y+d.y)/2);
        } else {
            mpTop = new Point((c.x + b.x) / 2, (b.y + c.y) / 2);
            mpBottom = new Point((d.x + a.x) / 2, (d.y + a.y) / 2);
        }
        corners[4] = mpTop;
        corners[5] = mpBottom;
        corners[6] = new Point( (a.x + c.x) / 2, (b.y + d.y) / 2);
        mpSlope = (double) (mpTop.y - mpBottom.y) / (mpTop.x - mpBottom.x);
        rectangles.add(corners);
        return true;
    }

    public boolean addCircle(Point t, Point r, Point b, Point l){
        Point[] circle = new Point[5];
        circle[0] = t;
        circle[1] = r;
        circle[2] = b;
        circle[3] = l;


        circle[4] = new Point((l.x + r.x) / 2, (t.y + b.y) / 2);
//        if( (b.y - t.y) - (r.x - l.x) < 5 && (b.y - t.y) - (r.x - l.x)  > -5){
//            if( isInside(circle[4], a, b, c, d) ) {
//                circles.add(circle);
//            }
//        }

        circles.add(circle);
        return true;
    }

    public boolean checkSquare(Point t, Point r, Point b, Point l){

        //must be square
        if(Math.abs(getLength(t,b) / getLength(l,r)) > 1+circleThreshold ){
            return false;
        }
        if(Math.abs(getLength(t,b) / getLength(l,r)) < 1-circleThreshold ){
            return false;
        }
        return true;
    }

    public boolean checkLong(Point a, Point b, Point c, Point d){
        double side1 = getLength(a,b);
        double side2 = getLength(b,c);
        double sidetoside = side1/side2;
        //must be rectangle

        if(sidetoside > 2.25 || (sidetoside < 1.75 && sidetoside > .625) || sidetoside < .375)
            return false;
        return true;
    }

    public int countSide(int side, Point[] a){
        int count = 0;
        switch(side){
            case 1:
                for(Point[] e: circles){
                    if(getLength(e[4], a[6]) < 10)
                        continue;
                    if(isInside(e[4], a[0], a[4], a[5], a[3])) {
                        count++;
                    }
                }
                break;
            case 2:
                for(Point[] e: circles){
                    if(getLength(e[4], a[6]) < 10)
                        continue;
                    if(isInside(e[4], a[4], a[1], a[2], a[5])){
                        count++;
                    }
                }
                break;
            default:
                break;
        }

        return count;
    }

    public int[][] getDominoes(){
        int[][] domlist = new int[rectangles.size()][2];
        int i = 0;
        for(Point[] a: rectangles){
            domlist[i][0] = countSide(1, a);
            domlist[i++][1] = countSide(2, a);
        }
        return domlist;
    }

    public boolean isInside(Point e, Point a, Point b, Point c, Point d){

        abLength = getLength(a,b);
        bcLength = getLength(b,c);
        cdLength = getLength(c,d);
        daLength = getLength(d,a);

        double aeLength = getLength(a,e);
        double beLength = getLength(b, e);
        double ceLength = getLength(c, e);
        double deLength = getLength(d, e);

        double u1 = (abLength + aeLength + beLength) / 2;
        double u2 = (bcLength + beLength + ceLength) / 2;
        double u3 = (cdLength + ceLength + deLength) / 2;
        double u4 = (daLength + deLength + aeLength) / 2;

        double a1 = Math.sqrt(u1*(u1 - abLength)*(u1 - aeLength)*(u1 - beLength));
        double a2 = Math.sqrt(u2*(u2 - bcLength)*(u2 - beLength)*(u2 - ceLength));
        double a3 = Math.sqrt(u3*(u3 - cdLength)*(u3 - ceLength)*(u3 - deLength));
        double a4 = Math.sqrt(u4*(u4 - daLength)*(u4 - deLength)*(u4 - aeLength));

        double area = (abLength * bcLength);
        double area2 = a1+a2+a3+a4;

        if(area2 > area*1.05){
            return false;
        }
        return true;
    }

    public double getLength(Point a, Point b){
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }
}
