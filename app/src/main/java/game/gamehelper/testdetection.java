package game.gamehelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import game.gamehelper.DominoMT.DetectedShape;

/**
 * Created by Mark Andrews on 4/11/2015.
 */
public class testdetection {
    private int  PICSIZEW = 256;
    private int  PICSIZEH = 256;
    private int  MAXMASK = 20;
    private double PIE = 3.14159;
    private int stopcheck = 0;
    private int limit = 100;

    public Bitmap bwfile;
    public Bitmap histogramfile;
    public Bitmap peaksfile;
    public Bitmap finalfile;
    public Bitmap shapesfile;
    public DetectedShape rectangle;
    public ArrayList<DetectedShape> dominoList = new ArrayList<>();
    public ArrayList<ArrayList<Point>> objectList = new ArrayList<>();
    public ArrayList<Point> points = new ArrayList<Point>();
    //number of empty spaces object finder can pass over
    int checkLimit = 2;

    int    [][]pic;
    double [][]outpicx;
    double [][]outpicy;
    int    [][]edgeflag;
    int    []histogram;
    double [][]maskx;
    double [][]masky;
    double [][]ival;
    double [][]ival2;
    double [][]peaks;
    double [][]finalPic;
    double [][]finalPic2;
    double [][]conv;

    int low = 150;
    int high = 240;
    int peakCount = 0;

    public testdetection(Bitmap file){
        int     i,j,p,q,s,t,mr,centx,centy;
        double  maskval,sum1, sum2,sig = 0.0,maxival = 0, slope = 0, percent = 0,minival,maxval,ZEROTOL,sigsigtwo,twopiesigfour,sigMod;
        File fo1, fo2, fo3,fp1;
        char[]    foobar;
        char[]    str = new char[10];
        int currentPixel = 0;

        PICSIZEW = file.getWidth();
        PICSIZEH = file.getHeight();

        int width, height;
        height = file.getHeight();
        width = file.getWidth();



        pic = new int[PICSIZEW][PICSIZEH];
        outpicx = new double[PICSIZEW][PICSIZEH];
        outpicy = new double[PICSIZEW][PICSIZEH];
        edgeflag = new int[PICSIZEW][PICSIZEH];
        histogram = new int[PICSIZEW];
        maskx = new double[MAXMASK][MAXMASK];
        masky = new double[MAXMASK][MAXMASK];
        ival = new double[PICSIZEW][PICSIZEH];
        ival2 = new double[PICSIZEW][PICSIZEH];
        peaks = new double[PICSIZEW][PICSIZEH];
        finalPic = new double[PICSIZEW][PICSIZEH];
        conv = new double[PICSIZEW][PICSIZEH];

        histogramfile = Bitmap.createBitmap(PICSIZEW, PICSIZEH, Bitmap.Config.ARGB_8888);
        peaksfile = Bitmap.createBitmap(PICSIZEW, PICSIZEH, Bitmap.Config.ARGB_8888);
        finalfile = Bitmap.createBitmap(PICSIZEW, PICSIZEH, Bitmap.Config.ARGB_8888);
        shapesfile = Bitmap.createBitmap(PICSIZEW, PICSIZEH, Bitmap.Config.ARGB_8888);


        bwfile = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bwfile);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(file, 0, 0, paint);

        for(i = 0 ; i < PICSIZEW  ; i++ ){
            for(j = 0 ; j < PICSIZEH ; j++){
                int pixel = bwfile.getPixel(i,j);
                int red = Color.red(pixel);
                int blue = Color.blue(pixel);
                int green = Color.green(pixel);
                int black = (int) (0.21*red + 0.72*green + 0.07*blue);
//                int black = (int) (red + green + blue) /3;
//                bwfile.setPixel(i,j, getBlack(black));
                pic[i][j] = black;
            }
        }

        sig = 1.0;

        percent = 9;

        percent = (PICSIZEW * PICSIZEW - 1) * (percent / 100);


        //variables for mask calculations
        sigsigtwo = (sig*sig*-2);
        twopiesigfour = (2*PIE*sig*sig*sig*sig);

        mr = (int)(sig * 3);
        centx = (MAXMASK / 2);
        centy = (MAXMASK / 2);

        //part 1
        for (p=-mr; p<=mr; p++)
        {
            for (q=-mr; q<=mr; q++)
            {
                //create x and y masks
                maskval = ( (-p / twopiesigfour) * Math.exp(((p * p) + (q * q)) / sigsigtwo) );
                (maskx[p+centy][q+centx]) = maskval;

                maskval = ( (-q / twopiesigfour) * Math.exp(((p * p) + (q * q)) / sigsigtwo) );
                (masky[p+centy][q+centx]) = maskval;
            }
        }


        for (i=mr; i<=(PICSIZEW - 1)-mr; i++)
        {
            for (j=mr; j<=(PICSIZEH - 1)-mr; j++)
            {
                sum1 = 0;
                sum2 = 0;
                for (p=-mr; p<=mr; p++)
                {
                    for (q=-mr; q<=mr; q++)
                    {
                        sum1 += pic[i+p][j+q] * maskx[p+centy][q+centx];
                        sum2 += pic[i+p][j+q] * masky[p+centy][q+centx];
                    }
                }
                outpicx[i][j] = sum1;
                outpicy[i][j] = sum2;
            }
        }


        for (i=mr; i<PICSIZEW-mr; i++)
        {
            for (j=mr; j<PICSIZEH-mr; j++)
            {
                ival[i][j]= Math.sqrt((double) ((outpicx[i][j] * outpicx[i][j]) +
                        (outpicy[i][j] * outpicy[i][j])));
                //printf("%d\n", ival[i][j]);
                if (ival[i][j] > maxival)
                    maxival = ival[i][j];
            }
        }

        //print part 1, create histogram
        for (i=0; i<PICSIZEW; i++)
        {
            for (j=0; j<PICSIZEH; j++)
            {
                ival2[i][j] = ((double)ival[i][j] / maxival) * (255);
                histogram[(int)ival2[i][j]] += 1;
                histogramfile.setPixel(i,j, getBlack((int)ival2[i][j]));
            }
        }

        //part 2
        for(i = mr ; i < PICSIZEW - mr ; i++)
        {
            for (j = mr ; j<PICSIZEH - mr ; j++)
            {
                if( (outpicy[i][j] == 0) )
                {
                    outpicy[i][j] = .00001;
                }

                slope = outpicx[i][j] / outpicy[i][j];
                if( (slope <= .4142) && (slope > -.4142))
                {
                    if( (ival[i][j] > ival[i][j-1]) && (ival[i][j] > ival[i][j+1]))
                    {
                        peaks[i][j] = (255);
                    }
                }
                else if( (slope <= 2.4142) && (slope > .4142))
                {
                    if( (ival[i][j] > ival[i-1][j-1]) && (ival[i][j] > ival[i+1][j+1]))
                    {
                        peaks[i][j] = (255);
                    }
                }
                else if( (slope <= -.4142) && (slope > -2.4142))
                {
                    if( (ival[i][j] > ival[i+1][j-1]) && (ival[i][j] > ival[i-1][j+1]))
                    {
                        peaks[i][j] = (255);
                    }
                }
                else
                {
                    if( (ival[i][j] > ival[i-1][j]) && (ival[i][j] > ival[i+1][j]))
                    {
                        peaks[i][j] = (255);
                    }
                }
            }
        }

        //print part 2, count possible peaks
        for (i=0; i<PICSIZEW; i++)
        {
            for (j=0; j<PICSIZEH; j++)
            {
                peaksfile.setPixel(i,j,getBlack((int)peaks[i][j]));
                if( peaks[i][j] > 0 )
                    peakCount++;
            }
        }

        //part 4, find high and low thresholds
        high = 0;

        // negative entered for percent will automatically find a percent to use
        if( percent < 0)
        {
            mr /= 3;
            sigMod = mr*Math.log(mr * mr);
            percent = sigMod > 1 ? sigMod : 1;
            percent = (percent * peakCount) / (PICSIZEW*PICSIZEH)*100 - mr*3.3;
            Log.w("testdetection", String.format("Using percent: %f\n", percent));
            percent = (percent / 100) * (PICSIZEW*PICSIZEH);
        }

        //starting from 255, add the total number of pixels with each value until it exceeds the
        //threshold then use that index for the high threshold
        for(i=(255); i>=0; i--)
        {
            high += histogram[i];
            if( high >= percent )
            {
                high = i;
                low = (int) (high * .35);
                break;
            }
        }

        //part 3
        for(i=0; i<PICSIZEW; i++)
        {
            for (j=0; j<PICSIZEH; j++)
            {
                {
                    stopcheck = 0;
                    checkNeighbors(i,j);
                }
            }
        }

        //print part 3
        for (i=0; i<PICSIZEW; i++)
        {
            for (j=0; j<PICSIZEH; j++)
            {
                   finalfile.setPixel(i,j, (int)finalPic[i][j]<<24);
//                fprintf(fo3,"%c",(char)((int)(finalPic[i][j])));
            }
        }
    }

    int getBlack(int i){

        int black = 0 + i<<24;
//        int black = (int)(i/.21)<<16 + (int)(0.72*i)<<8 + (int)(0.07*i);
        return black;
    }

    void checkNeighbors(int i, int j)
    {
        stopcheck++;
        if(stopcheck > limit)
            return;
        if(i < 0 || j < 0 || i > (PICSIZEW - 1) || j > (PICSIZEH - 1))
            return;
        if(peaks[i][j] == (255))
        {
            if(ival[i][j] > high)
            {
                peaks[i][j] = 0;
                finalPic[i][j] = (255);
                checkNeighbors(i-1, j);
                checkNeighbors(i-1, j-1);
                checkNeighbors(i, j-1);
                checkNeighbors(i+1, j-1);
                checkNeighbors(i+1, j);
                checkNeighbors(i+1, j+1);
                checkNeighbors(i, j+1);
                checkNeighbors(i-1, j+1);
            }
            else if( ival[i][j] < low)
            {
                peaks[i][j] = 0;
            }
        }
    }


    void findShapes()
    {
        finalPic2 = finalPic.clone();

        for(int i = 0 ; i < PICSIZEW ; i++){
            for(int j = 0 ; j < PICSIZEH ; j++){
                if(finalPic[i][j] >=  (1))
                {
                    stopcheck = 0;
                    int pointCount = 1;
                    int pointCount2 = 0;
                    points = new ArrayList<Point>();
                    Point start = new Point();
                    start.set(i, j);
                    points.add(start);
                    finalPic[i][j] = 0;
//                    if(i > 1 && i < PICSIZEW-1 && j > 1 && j < PICSIZEH-1){
//                        if(finalPic[i][j-1] == 255 ) {
//                            j++;
//
//                        }if (finalPic[i+1][j] == 255){
//                            checkRight(i+1,j,0);
//                        } if (finalPic[])
//
//
//
//                    }
                    for(int k = 0 ; k < pointCount ; k = pointCount2 ){
                        for(int m = k ; m < pointCount ; m++) {
                            Point p = points.get(m);
                            checkUp(p.x, p.y - 1, 0);
                            checkRight(p.x + 1, p.y, 0);
                            checkLeft(p.x - 1, p.y, 0);
                            checkDown(p.x, p.y + 1, 0);
                        }
                            pointCount2 = pointCount;
                            pointCount = points.size();
                    }
                    if(points.size() > 20){
                        objectList.add(points);
                        for(Point p: points){
                            shapesfile.setPixel(p.x, p.y, 255<<24);
                        }
                    } else {
                        points.clear();
                    }

                }
            }
        }

        finalPic = finalPic2;
    }

    void checkUp(int i, int j, int check){
        check++;
        if(check > checkLimit){
            return;
        }
        if(i < 0 || j < 0 || i > (PICSIZEW - 1) || j > (PICSIZEH - 1))
            return;

        if(finalPic[i][j] >=  (1)) {
            Point point = new Point();
            point.set(i, j);
            points.add(point);
            finalPic[i][j] = 0;
        }

        checkUp(i, j - 1, check);
        checkRight(i + 1, j, check);
        checkLeft(i - 1, j, check);
    }

    void checkRight(int i, int j, int check){
        check++;
        if(check > checkLimit){
            return;
        }
        if(i < 0 || j < 0 || i > (PICSIZEW - 1) || j > (PICSIZEH - 1))
            return;

        if(finalPic[i][j] >=  (1)) {
            Point point = new Point();
            point.set(i, j);
            points.add(point);
            finalPic[i][j] = 0;
        }

        checkUp(i, j - 1, check);
        checkRight(i + 1, j, check);
        checkDown(i, j + 1, check);
    }

    void checkDown(int i, int j, int check){
        check++;
        if(check > checkLimit){
            return;
        }
        if(i < 0 || j < 0 || i > (PICSIZEW - 1) || j > (PICSIZEH - 1))
            return;

        if(finalPic[i][j] >=  (1)) {
            Point point = new Point();
            point.set(i, j);
            points.add(point);
            finalPic[i][j] = 0;
        }

        checkDown(i, j + 1, check);
        checkRight(i + 1, j, check);
        checkLeft(i-1, j, check);
    }

    void checkLeft(int i, int j, int check){
        check++;
        if(check > checkLimit){
            return;
        }
        if(i < 0 || j < 0 || i > (PICSIZEW - 1) || j > (PICSIZEH - 1))
            return;

        if(finalPic[i][j] >=  (1)) {
            Point point = new Point();
            point.set(i, j);
            points.add(point);
            finalPic[i][j] = 0;
        }

        checkUp(i, j - 1, check);
        checkDown(i, j + 1, check);
        checkLeft(i-1, j, check);
    }

    void makeShapes(){
        rectangle = new DetectedShape();
        for(ArrayList<Point> i: objectList){
            Point leftBottom = new Point(-1,-1);
            Point leftTop = new Point(-1,-1);
            Point rightBottom = new Point(-1,-1);
            Point rightTop = new Point(-1,-1);
            for(Point j: i){
                if(leftBottom.x == -1 || (j.y >= leftBottom.y)){
                    leftBottom.x = j.x;
                    leftBottom.y = j.y;
                }

                if(leftTop.x == -1 || (j.x <= leftTop.x )){
                    leftTop.x = j.x;
                    leftTop.y = j.y;
                }

                if(rightBottom.x == -1 || (j.x >= rightBottom.x )){
                    rightBottom.x = j.x;
                    rightBottom.y = j.y;
                }


                if(rightTop.x == -1 || (j.y <= rightTop.y )){
                    rightTop.x = j.x;
                    rightTop.y = j.y;
                }
            }
            boolean check1 = rectangle.checkLong(leftBottom, leftTop, rightBottom, rightTop);
            boolean check2 = rectangle.checkSquare(rightTop, rightBottom, leftBottom, leftTop);
            if(rectangle.checkLong(leftBottom, leftTop, rightBottom, rightTop)) {
                rectangle.addRectangle(leftBottom, leftTop, rightBottom, rightTop);
            }else if(rectangle.checkSquare(rightTop, rightBottom, leftBottom, leftTop)){
                rectangle.addCircle(rightTop, rightBottom, leftBottom, leftTop);
            }
        }
    }

}
