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

package game.gamehelper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;

import game.gamehelper.DominoMT.DetectedShape;

/**
 * Created by Mark Andrews on 4/11/2015.
 */
public class DominoDetection
{
    private final int picWidth;
    private final int picHeight;

    private double[][] ival;
    private double[][] peaks;
    private double[][] finalPic;

    private Bitmap bwfile;
    private Bitmap histogramfile;
    private Bitmap peaksfile;
    private Bitmap finalfile;
    private Bitmap shapesfile;
    private DetectedShape rectangle;

    //pathfinding limiter
    private int stopcheck = 0;
    private int limit = 100;

    //list of list of contiguous points
    public ArrayList<ArrayList<Point>> objectList = new ArrayList<>();
    public ArrayList<Point> points = new ArrayList<Point>();

    //number of empty spaces object finder can pass over
    private int checkLimit = 2;

    //not used
    int low = 150;
    int high = 240;

    //for determining high and low, set to -1 for automatic detection
    private double percent = 9;

    //sig
    private double sigma = 1.0;
    private int maskSize = 20;
    private Bitmap sourceFile;

    //number of pixels that are larger than neighbors
    int peakCount = 0;

    public DominoDetection(Bitmap file, double sigma, int maskSize, int limit,
            int checkLimit, double percent)
    {

        this.sourceFile = file;
        this.sigma = sigma;
        this.maskSize = maskSize;
        this.limit = limit;
        this.checkLimit = checkLimit;
        this.percent = percent;

        picWidth = sourceFile.getWidth();
        picHeight = sourceFile.getHeight();
    }

    public void processImage()
    {

        int i, j, p, q, mr, centx, centy;
        double maskval, sum1, sum2, maxival = 0, slope = 0, sigsigtwo, twopiesigfour, sigMod;

        int[][] pic = new int[picWidth][picHeight];
        double[][] outpicx = new double[picWidth][picHeight];
        double[][] outpicy = new double[picWidth][picHeight];
        int[] histogram = new int[256];
        double[][] maskx = new double[maskSize][maskSize];
        double[][] masky = new double[maskSize][maskSize];
        ival = new double[picWidth][picHeight];
        double[][] ival2 = new double[picWidth][picHeight];
        peaks = new double[picWidth][picHeight];
        finalPic = new double[picWidth][picHeight];
        double[][] conv = new double[picWidth][picHeight];

        histogramfile = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);
        peaksfile = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);
        finalfile = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);
        shapesfile = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);
        bwfile = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);

        //create grayscale image
        Canvas c = new Canvas(bwfile);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(sourceFile, 0, 0, paint);

        //convert RGB grayscale bitmap to 1 channel int array
        for (i = 0; i < picWidth; i++)
        {
            for (j = 0; j < picHeight; j++)
            {
                int pixel = bwfile.getPixel(i, j);
                int red = Color.red(pixel);
                int blue = Color.blue(pixel);
                int green = Color.green(pixel);
                int black = (int) (0.21 * red + 0.72 * green + 0.07 * blue);
                pic[i][j] = black;
            }
        }


        //determine how many pixels are in the x percent
        percent = (picWidth * picWidth - 1) * (percent / 100);

        //variables for mask calculations
        sigsigtwo = (sigma * sigma * -2);
        twopiesigfour = (2 * Math.PI * sigma * sigma * sigma * sigma);

        mr = (int) (sigma * 3);
        centx = (maskSize / 2);
        centy = (maskSize / 2);

        //part 1
        for (p = -mr; p <= mr; p++)
        {
            for (q = -mr; q <= mr; q++)
            {
                //create x and y masks
                maskval = ((-p / twopiesigfour) * Math.exp(((p * p) + (q * q)) / sigsigtwo));
                (maskx[p + centy][q + centx]) = maskval;

                maskval = ((-q / twopiesigfour) * Math.exp(((p * p) + (q * q)) / sigsigtwo));
                (masky[p + centy][q + centx]) = maskval;
            }
        }

        //gaussian blur
        for (i = mr; i <= (picWidth - 1) - mr; i++)
        {
            for (j = mr; j <= (picHeight - 1) - mr; j++)
            {
                sum1 = 0;
                sum2 = 0;
                for (p = -mr; p <= mr; p++)
                {
                    for (q = -mr; q <= mr; q++)
                    {
                        sum1 += pic[i + p][j + q] * maskx[p + centy][q + centx];
                        sum2 += pic[i + p][j + q] * masky[p + centy][q + centx];
                    }
                }
                outpicx[i][j] = sum1;
                outpicy[i][j] = sum2;
            }
        }

        //gaussian blur cont.
        for (i = mr; i < picWidth - mr; i++)
        {
            for (j = mr; j < picHeight - mr; j++)
            {
                ival[i][j] = Math.sqrt((double) ((outpicx[i][j] * outpicx[i][j]) +
                        (outpicy[i][j] * outpicy[i][j])));

                if (ival[i][j] > maxival)
                    maxival = ival[i][j];
            }
        }

        //create histogram
        for (i = 0; i < picWidth; i++)
        {
            for (j = 0; j < picHeight; j++)
            {
                //counts how many pixels are at each value 0-255
                ival2[i][j] = ((double) ival[i][j] / maxival) * (255);
                histogram[(int) ival2[i][j]] += 1;
                //blurred image
                histogramfile.setPixel(i, j, getBlack((int) ival2[i][j]));
            }
        }

        //Checking pixel neighbors and marking pixel as peak if it is the largest
        for (i = mr; i < picWidth - mr; i++)
        {
            for (j = mr; j < picHeight - mr; j++)
            {
                if ((outpicy[i][j] == 0))
                {
                    outpicy[i][j] = .00001;
                }

                slope = outpicx[i][j] / outpicy[i][j];
                if ((slope <= .4142) && (slope > -.4142))
                {
                    if ((ival[i][j] > ival[i][j - 1]) && (ival[i][j] > ival[i][j + 1]))
                    {
                        peaks[i][j] = (255);
                    }
                }
                else if ((slope <= 2.4142) && (slope > .4142))
                {
                    if ((ival[i][j] > ival[i - 1][j - 1]) && (ival[i][j] > ival[i + 1][j + 1]))
                    {
                        peaks[i][j] = (255);
                    }
                }
                else if ((slope <= -.4142) && (slope > -2.4142))
                {
                    if ((ival[i][j] > ival[i + 1][j - 1]) && (ival[i][j] > ival[i - 1][j + 1]))
                    {
                        peaks[i][j] = (255);
                    }
                }
                else
                {
                    if ((ival[i][j] > ival[i - 1][j]) && (ival[i][j] > ival[i + 1][j]))
                    {
                        peaks[i][j] = (255);
                    }
                }
            }
        }

        //print part 2, count possible peaks
        for (i = 0; i < picWidth; i++)
        {
            for (j = 0; j < picHeight; j++)
            {
                peaksfile.setPixel(i, j, getBlack((int) peaks[i][j]));
                if (peaks[i][j] > 0)
                    peakCount++;
            }
        }

        //part 4, find high and low thresholds
        high = 0;

        // negative entered for percent will automatically find a percent to use
        //this formula is in no way perfect, I literally made arbitrary calculations until
        //the generated percent worked well for different pictures
        if (percent < 0)
        {
            mr /= 3;
            sigMod = mr * Math.log(mr * mr);
            percent = sigMod > 1 ? sigMod : 1;
            percent = (percent * peakCount) / (picWidth * picHeight) * 100 - mr * 3.3;
            Log.w("DominoDetection", String.format("Using percent: %f\n", percent));
            percent = (percent / 100) * (picWidth * picHeight);
        }

        //starting from 255, add the total number of pixels with each value until it exceeds the
        //threshold then use that index for the high threshold
        for (i = (255); i >= 0; i--)
        {
            high += histogram[i];
            if (high >= percent)
            {
                high = i;
                low = (int) (high * .35);
                break;
            }
        }

        //double thresholding, if the blurred image's pixel is larger than threshold set it
        //as an edge, if it between high and low then check neighbors for pixels that are edges
        //and if found make pixel an edge
        for (i = 0; i < picWidth; i++)
        {
            for (j = 0; j < picHeight; j++)
            {
                stopcheck = 0;
                checkNeighbors(i, j);
            }
        }

        //print part 3
        for (i = 0; i < picWidth; i++)
        {
            for (j = 0; j < picHeight; j++)
            {
                finalfile.setPixel(i, j, (int) finalPic[i][j] << 24);
            }
        }
    }

    private int getBlack(int i)
    {
//        int black = (int)(i/.21)<<16 + (int)(0.72*i)<<8 + (int)(0.07*i);

        int black = 0 + i << 24;
        return black;
    }

    public Bitmap getBWImage()
    {
        return bwfile;
    }

    public Bitmap getHistogramImage()
    {
        return histogramfile;
    }

    public Bitmap getPeaksImage()
    {
        return peaksfile;
    }

    public Bitmap getFinalImage()
    {
        return finalfile;
    }

    public Bitmap getShapesImage()
    {
        return shapesfile;
    }

    public boolean isProcessed()
    {
        return bwfile != null;
    }

    public DetectedShape getDetectedShape()
    {
        return rectangle;
    }

    private void checkNeighbors(int i, int j)
    {
        stopcheck++;
        if (stopcheck > limit)
            return;
        if (i < 0 || j < 0 || i > (picWidth - 1) || j > (picHeight - 1))
            return;
        if (peaks[i][j] == (255))
        {
            if (ival[i][j] > high)
            {
                peaks[i][j] = 0;
                finalPic[i][j] = (255);
                checkNeighbors(i - 1, j);
                checkNeighbors(i - 1, j - 1);
                checkNeighbors(i, j - 1);
                checkNeighbors(i + 1, j - 1);
                checkNeighbors(i + 1, j);
                checkNeighbors(i + 1, j + 1);
                checkNeighbors(i, j + 1);
                checkNeighbors(i - 1, j + 1);
            }
            else if (ival[i][j] < low)
            {
                peaks[i][j] = 0;
            }
        }
    }


    //iterate through image until an edge is found then look for other edges nearby and group as
    //a single object
    public void findShapes()
    {
        for (int i = 0; i < picWidth; i++)
        {
            for (int j = 0; j < picHeight; j++)
            {
                if (finalPic[i][j] >= (1))
                {
                    stopcheck = 0;
                    int pointCount = 1;
                    int pointCount2 = 0;
                    points = new ArrayList<Point>();
                    Point start = new Point();
                    start.set(i, j);
                    points.add(start);
                    finalPic[i][j] = 0;

                    //workaround for stack issues, continue looking for edges around new edges until
                    //no new edges are found
                    for (int k = 0; k < pointCount; k = pointCount2)
                    {
                        for (int m = k; m < pointCount; m++)
                        {
                            Point p = points.get(m);
                            checkUp(p.x, p.y - 1, 0);
                            checkRight(p.x + 1, p.y, 0);
                            checkLeft(p.x - 1, p.y, 0);
                            checkDown(p.x, p.y + 1, 0);
                        }
                        pointCount2 = pointCount;
                        pointCount = points.size();
                    }

                    //threshold for possible static picked up
                    if (points.size() > 20)
                    {
                        objectList.add(points);

                        //write the edges found to be part of a larger object to image
                        for (Point p : points)
                        {
                            shapesfile.setPixel(p.x, p.y, 255 << 24);
                        }

                    }
                    else
                    {
                        points.clear();
                    }

                }
            }
        }
    }

    //traversal functions, check is the number of empty spaces skipped over, checkLimit being how many
    //pixels this program is allowed to skip
    private void checkUp(int i, int j, int check)
    {
        check++;
        if (check > checkLimit)
        {
            return;
        }
        if (i < 0 || j < 0 || i > (picWidth - 1) || j > (picHeight - 1))
            return;

        if (finalPic[i][j] >= (1))
        {
            Point point = new Point();
            point.set(i, j);
            points.add(point);
            finalPic[i][j] = 0;
        }

        checkUp(i, j - 1, check);
        checkRight(i + 1, j, check);
        checkLeft(i - 1, j, check);
    }

    private void checkRight(int i, int j, int check)
    {
        check++;
        if (check > checkLimit)
        {
            return;
        }
        if (i < 0 || j < 0 || i > (picWidth - 1) || j > (picHeight - 1))
            return;

        if (finalPic[i][j] >= (1))
        {
            Point point = new Point();
            point.set(i, j);
            points.add(point);
            finalPic[i][j] = 0;
        }

        checkUp(i, j - 1, check);
        checkRight(i + 1, j, check);
        checkDown(i, j + 1, check);
    }

    private void checkDown(int i, int j, int check)
    {
        check++;
        if (check > checkLimit)
        {
            return;
        }
        if (i < 0 || j < 0 || i > (picWidth - 1) || j > (picHeight - 1))
            return;

        if (finalPic[i][j] >= (1))
        {
            Point point = new Point();
            point.set(i, j);
            points.add(point);
            finalPic[i][j] = 0;
        }

        checkDown(i, j + 1, check);
        checkRight(i + 1, j, check);
        checkLeft(i - 1, j, check);
    }

    private void checkLeft(int i, int j, int check)
    {
        check++;
        if (check > checkLimit)
        {
            return;
        }
        if (i < 0 || j < 0 || i > (picWidth - 1) || j > (picHeight - 1))
            return;

        if (finalPic[i][j] >= (1))
        {
            Point point = new Point();
            point.set(i, j);
            points.add(point);
            finalPic[i][j] = 0;
        }

        checkUp(i, j - 1, check);
        checkDown(i, j + 1, check);
        checkLeft(i - 1, j, check);
    }

    public void makeShapes()
    {
        rectangle = new DetectedShape();
        for (ArrayList<Point> i : objectList)
        {
            //iterate through the objects found and find the corners of rectangles or sides of circles

            Point leftBottom = new Point(-1, -1);
            Point leftTop = new Point(-1, -1);
            Point rightBottom = new Point(-1, -1);
            Point rightTop = new Point(-1, -1);
            for (Point j : i)
            {

                if (leftBottom.x == -1 || (j.y >= leftBottom.y))
                {
                    leftBottom.x = j.x;
                    leftBottom.y = j.y;
                }

                if (leftTop.x == -1 || (j.x <= leftTop.x))
                {
                    leftTop.x = j.x;
                    leftTop.y = j.y;
                }

                if (rightBottom.x == -1 || (j.x >= rightBottom.x))
                {
                    rightBottom.x = j.x;
                    rightBottom.y = j.y;
                }

                if (rightTop.x == -1 || (j.y <= rightTop.y))
                {
                    rightTop.x = j.x;
                    rightTop.y = j.y;
                }
            }

            //checking if length is about twice as long as height
            boolean check1 = rectangle.checkLong(leftBottom, leftTop, rightBottom, rightTop);

            //checking if length is about the same as height
            boolean check2 = rectangle.checkSquare(rightTop, rightBottom, leftBottom, leftTop);

            //add object to corresponding list
            if (check1)
            {
                rectangle.addRectangle(leftBottom, leftTop, rightBottom, rightTop);
            }
            else if (check2)
            {
                rectangle.addCircle(rightTop, rightBottom, leftBottom, leftTop);
            }
        }
        //remove wrongly added shapes
        rectangle.deleteOutliers();
    }
}
