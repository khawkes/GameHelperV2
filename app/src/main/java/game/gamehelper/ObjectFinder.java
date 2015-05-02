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

/**
 * Created by Mark Andrews on 4/11/2015.
 * Uses canny to edge detect, then finds objects as continuous edges and determines the
 * north east south west corners of each object
 */
public class ObjectFinder
{
    private final int picWidth;
    private final int picHeight;

    /**
     * @Param pic 1-channel bitmap of source image
     * @Param ival blurred image, "hypotenuse" of horizontal and vertical blurred pixel
     * @Param ival2 magnitude of image, darkest pixel in ival increased to 255 all other pixels increased by same scale
     * @Param peaks pixels that were determined to be peaks
     * @Param edges pixels that were determined to be edges
     * @Param outpicx horizontal blurred image
     * @Param outpicy vertical blurred image
     * @Param maskx horizontal mask
     * @Param masky vertical mask
     * @Param histogram histogram of the 256 pixel magnitudes
     *
     * ival, ival2, outpicx and outpicy will always be between 0 and 255, some space can be saved by changing
     * the variable type to byte but minor amounts of accuracy would be lost
     */

    private int[][] pic;
    private double[][] ival;
    private double[][] ival2;
    private boolean[][] peaks;
    private boolean[][] edges;
    private double[][] outpicx;
    private double[][] outpicy;
    private double[][] maskx;
    private double[][] masky;
    private int[] histogram = new int[256];

    private Bitmap sourceFile;
    private Bitmap bwImage;
    private Bitmap magnitudeImage;
    private Bitmap peaksImage;
    private Bitmap cannyEdgesImage;
    private Bitmap detectedObjectsImage;

    //pathfinding limiter
    private int stopcheck = 0;
    private int limit = 100;

    //list of list of contiguous points
    public ArrayList<ArrayList<Point>> objectList = new ArrayList<>();

    //list of corners of each object
    public ArrayList<ArrayList<Point>> cornerList = new ArrayList<>();

    //for creating lists of points for object/cornerList
    public ArrayList<Point> points;

    //number of empty spaces object finder can pass over
    private int checkLimit = 2;

    //overwritten by percent
    int low = 150;
    int high = 240;

    //for determining high and low, set to -1 for automatic detection
    private double percent = 9;

    //strength of blur
    private double sigma = 1.0;

    //size of block for calculating horizontal and vertical blur
    private int maskSize = 20;

    //number of pixels that are larger than neighbors
    int peakCount = 0;

    public ObjectFinder(Bitmap file, double sigma, int maskSize, int limit,
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

        pic = new int[picWidth][picHeight];
        outpicx = new double[picWidth][picHeight];
        outpicy = new double[picWidth][picHeight];
        maskx = new double[maskSize][maskSize];
        masky = new double[maskSize][maskSize];
        ival = new double[picWidth][picHeight];
        ival2 = new double[picWidth][picHeight];
        peaks = new boolean[picWidth][picHeight];
        edges = new boolean[picWidth][picHeight];

        //image types are described below, large amounts of space are currently wasted
        //please change if there is something more space efficient

        //byte sized single channel image displayed using alpha channel only
        bwImage = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);
        magnitudeImage = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);

        //binary images
        peaksImage = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);
        cannyEdgesImage = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);
        detectedObjectsImage = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);

        //create grayscale image
        Canvas c = new Canvas(bwImage);
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
                int pixel = bwImage.getPixel(i, j);
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

        //gaussian blur: generate two images with horizontal and vertical blur
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

        //gaussian blur: average the pixels from the 2d blur
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
                magnitudeImage.setPixel(i, j, getBlack((int) ival2[i][j]));
            }
        }

        //Determine if pixels are peaks using the slope of the x and y values of the blurred image
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
                        peaks[i][j] = true;
                    }
                }
                else if ((slope <= 2.4142) && (slope > .4142))
                {
                    if ((ival[i][j] > ival[i - 1][j - 1]) && (ival[i][j] > ival[i + 1][j + 1]))
                    {
                        peaks[i][j] = true;
                    }
                }
                else if ((slope <= -.4142) && (slope > -2.4142))
                {
                    if ((ival[i][j] > ival[i + 1][j - 1]) && (ival[i][j] > ival[i - 1][j + 1]))
                    {
                        peaks[i][j] = true;
                    }
                }
                else
                {
                    if ((ival[i][j] > ival[i - 1][j]) && (ival[i][j] > ival[i + 1][j]))
                    {
                        peaks[i][j] = true;
                    }
                }
            }
        }

        //print part 2, count possible peaks
        for (i = 0; i < picWidth; i++)
        {
            for (j = 0; j < picHeight; j++)
            {
                peaksImage.setPixel(i, j, getBlack( (peaks[i][j]) ? 255 : 0) );
                if (peaks[i][j])
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

        //write the detected edges to file
        for (i = 0; i < picWidth; i++)
        {
            for (j = 0; j < picHeight; j++)
            {
                cannyEdgesImage.setPixel(i, j, (edges[i][j] ? 255 : 0) << 24);
            }
        }
    }

    private int getBlack(int i)
    {
        int black = 0 + i << 24;
        return black;
    }

    public Bitmap getBWImage()
    {
        return bwImage;
    }

    public Bitmap getMagnitudeImage()
    {
        return magnitudeImage;
    }

    public Bitmap getPeaksImage()
    {
        return peaksImage;
    }

    public Bitmap getEdgesImage()
    {
        return cannyEdgesImage;
    }

    public Bitmap getDetectedObjectsImage()
    {
        return detectedObjectsImage;
    }

    public boolean isProcessed()
    {
        return bwImage != null;
    }

    public ArrayList<ArrayList<Point>> getObjectList()
    {
        return objectList;
    }

    public ArrayList<ArrayList<Point>> getCornerList()
    {
        return cornerList;
    }


    private void checkNeighbors(int i, int j)
    {
        //determines if a peak will be a final edge using double thresholding
        stopcheck++;
        if (stopcheck > limit)
            return;
        if (i < 0 || j < 0 || i > (picWidth - 1) || j > (picHeight - 1))
            return;
        if (peaks[i][j])
        {
            if (ival[i][j] > high)
            {
                peaks[i][j] = false;
                edges[i][j] = true;
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
                peaks[i][j] = false;
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
                if (edges[i][j])
                {
                    stopcheck = 0;
                    int pointCount = 1;
                    int pointCount2 = 0;
                    points = new ArrayList<Point>();
                    Point start = new Point();
                    start.set(i, j);
                    points.add(start);
                    edges[i][j] = false;

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
                            detectedObjectsImage.setPixel(p.x, p.y, 255 << 24);
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

        if (edges[i][j])
        {
            Point point = new Point();
            point.set(i, j);
            points.add(point);
            edges[i][j] = false;
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

        if (edges[i][j])
        {
            Point point = new Point();
            point.set(i, j);
            points.add(point);
            edges[i][j] = false;
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

        if (edges[i][j])
        {
            Point point = new Point();
            point.set(i, j);
            points.add(point);
            edges[i][j] = false;
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

        if (edges[i][j])
        {
            Point point = new Point();
            point.set(i, j);
            points.add(point);
            edges[i][j] = false;
        }

        checkUp(i, j - 1, check);
        checkDown(i, j + 1, check);
        checkLeft(i - 1, j, check);
    }

    public void makeShapes()
    {
        for (ArrayList<Point> i : objectList)
        {
            //iterate through the objects found and find the north south east and west corners of the object
            points = new ArrayList<>();
            Point south = new Point(-1, -1);
            Point west = new Point(-1, -1);
            Point east = new Point(-1, -1);
            Point north = new Point(-1, -1);
            for (Point j : i)
            {

                if (south.x == -1 || (j.y >= south.y) || (j.y == south.y && j.x < south.x))
                {
                    south.x = j.x;
                    south.y = j.y;
                }

                if (west.x == -1 || (j.x <= west.x) || (j.x == west.x && j.y < west.y))
                {
                    west.x = j.x;
                    west.y = j.y;
                }

                if (east.x == -1 || (j.x >= east.x) || (j.x == east.x && j.x > east.x))
                {
                    east.x = j.x;
                    east.y = j.y;
                }

                if (north.x == -1 || (j.y <= north.y) || (j.y == north.y && j.x > north.x))
                {
                    north.x = j.x;
                    north.y = j.y;
                }
            }

            points.add(south);
            points.add(west);
            points.add(north);
            points.add(east);
            cornerList.add(points);
        }
    }
}
