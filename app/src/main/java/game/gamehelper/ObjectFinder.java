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
import android.util.Pair;

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

//    private int[] pic;
//    private double[][] ival;
//    private double[][] ival2;
    private boolean[][] edges;
//    private double[][] outpicx;
//    private double[][] outpicy;
//    private double[][] maskx;
//    private double[][] masky;
//    private int[] histogram = new int[256];

    private Bitmap sourceFile;
    private Bitmap bwImage;
    private Bitmap magnitudeImage;
    private Bitmap peaksImage;
    private Bitmap cannyEdgesImage;
    private Bitmap detectedObjectsImage;

    //pathfinding limiter
    private final int LIMIT;

    //list of list of contiguous points
    public ArrayList<ArrayList<Point>> objectList = new ArrayList<>();

    //list of corners of each object
    public ArrayList<ArrayList<Point>> cornerList = new ArrayList<>();

    //for creating lists of points for object/cornerList
    public ArrayList<Point> points;

    //number of empty spaces object finder can pass over
    private final int checkLimit;

    //records high points and low points.
    int low;
    int high;

    //for determining high and low, set to -1 for automatic detection
    private double percent = 9;

    //strength of blur
    private final double sigma;

    //size of block for calculating horizontal and vertical blur
    private int maskSize = 20;

    //number of pixels that are larger than neighbors
    int peakCount = 0;

    private final int NORTH = 1;
    private final int SOUTH = -1;
    private final int EAST = -2;
    private final int WEST = 2;

    public ObjectFinder(Bitmap file, double sigma, int maskSize, int limit, int checkLimit, double percent)
    {
        this.sourceFile = file;
        this.sigma = sigma;
        this.maskSize = maskSize;
        this.LIMIT = limit;
        this.checkLimit = checkLimit;
        this.percent = percent;

        picWidth = sourceFile.getWidth();
        picHeight = sourceFile.getHeight();

        processImage(picWidth, picHeight);
    }

    /**
     * Returns a grayscale int array of the original Bitmap.
     * @param original the original bitmap to convert.
     * @return the grayscale representation.
     */
    public int[] getGrayscale(Bitmap original)
    {
        //We reserve space for our bitmap.
        int picWidth = original.getWidth();
        int picHeight = original.getHeight();
        bwImage = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);

        //create the canvas to draw on.
        Canvas blackAndWhiteCanvas = new Canvas(bwImage);
        ColorMatrix desaturatedColorMatrix = new ColorMatrix();
        desaturatedColorMatrix.setSaturation(0);
        ColorMatrixColorFilter grayscaleFilter = new ColorMatrixColorFilter(desaturatedColorMatrix);

        //apply the filter. Going to change this to be more efficient and do it in a single step.
        //Y = 0.299R + 0.587G + 0.114B
        Paint grayscale = new Paint();
        grayscale.setColorFilter(grayscaleFilter);
        blackAndWhiteCanvas.drawBitmap(original, 0, 0, grayscale);

        //Copy pixels from bwImage
        int pic[] = new int[picWidth * picHeight];
        bwImage.getPixels(pic, 0, bwImage.getWidth(), 0, 0, picWidth, picHeight);

        //convert RGB grayscale bitmap to 1 channel int array
        for (int i = 0; i < pic.length; i++)
        {
            pic[i] = (int) (0.21 * Color.red(pic[i]) + 0.72 * Color.green(pic[i]) + 0.07 * Color.blue(pic[i]));
        }

        return pic;
    }

    /**
     * Dynamically creates the masks for the Gaussian blur algorithm.
     * @return Pair(xMask, yMask)
     */
    private Pair<double[][], double[][]> getMasksForGaussianBlur()
    {
        //constants for mask calculations
        final double sigsigtwo = (sigma * sigma * -2);
        final double twopiesigfour = (2 * Math.PI * sigma * sigma * sigma * sigma);
        final int mr = (int) (sigma * 3);
        final int centx = (maskSize / 2);
        final int centy = (maskSize / 2);

        double[][] xMask = new double[maskSize][maskSize];
        double[][] yMask = new double[maskSize][maskSize];

        //part 1
        for (int p = -mr; p <= mr; p++)
        {
            for (int q = -mr; q <= mr; q++)
            {
                //create x and y masks
                double maskval;
                maskval = ((-p / twopiesigfour) * Math.exp(((p * p) + (q * q)) / sigsigtwo));
                xMask[p + centy][q + centx] = maskval;

                maskval = ((-q / twopiesigfour) * Math.exp(((p * p) + (q * q)) / sigsigtwo));
                yMask[p + centy][q + centx] = maskval;
            }
        }

        return new Pair<>(xMask, yMask);
    }

    private void processImage(int picWidth, int picHeight)
    {
        //gets the grayscale version of the source
        int[] pic = getGrayscale(sourceFile);

        //constants for blur calculations
        final int offset = (int) (sigma * 3);
        final int maskOffsetX = (maskSize / 2);
        final int maskOffsetY = (maskSize / 2);

        //get masks for blur
        Pair<double[][], double[][]> masks = getMasksForGaussianBlur();
        double[][] xMask = masks.first;
        double[][] yMask = masks.second;

        //clear pair.
        masks = null;

        double[][] outPicX = new double[picWidth][picHeight];
        double[][] outPicY = new double[picWidth][picHeight];

        //gaussian blur: generate two images with horizontal and vertical blur
        for (int i = offset; i <= (picWidth - 1) - offset; i++)
        {
            for (int j = offset; j <= (picHeight - 1) - offset; j++)
            {
                double sum1 = 0;
                double sum2 = 0;
                for (int p = -offset; p <= offset; p++)
                {
                    for (int q = -offset; q <= offset; q++)
                    {
                        sum1 += pic[i + p + (j + q) * picWidth] * xMask[p + maskOffsetY][q + maskOffsetX];
                        sum2 += pic[i + p + (j + q) * picWidth] * yMask[p + maskOffsetY][q + maskOffsetX];
                    }
                }
                outPicX[i][j] = sum1;
                outPicY[i][j] = sum2;
            }
        }

        //cleanup memory
        pic = null;
        xMask = null;
        yMask = null;

        double[][] ival = new double[picWidth][picHeight];

        double maxival = 0;

        //gaussian blur: average the pixels from the 2d blur
        for (int i = offset; i < picWidth - offset; i++)
        {
            for (int j = offset; j < picHeight - offset; j++)
            {
                ival[i][j] = Math.sqrt((double) ((outPicX[i][j] * outPicX[i][j]) +
                        (outPicY[i][j] * outPicY[i][j])));

                if (ival[i][j] > maxival)
                    maxival = ival[i][j];
            }
        }

        boolean[][] peaks = new boolean[picWidth][picHeight];

        //Determine if pixels are peaks using the slope of the x and y values of the blurred image
        for (int i = offset; i < picWidth - offset; i++)
        {
            for (int j = offset; j < picHeight - offset; j++)
            {
                if ((outPicY[i][j] == 0))
                {
                    outPicY[i][j] = .00001;
                }

                double slope = outPicX[i][j] / outPicY[i][j];
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

        //cleanup memory
        outPicX = null;
        outPicY = null;

        peaksImage = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);

        //print part 2, count possible peaks
        for (int i = 0; i < picWidth; i++)
        {
            for (int j = 0; j < picHeight; j++)
            {
                peaksImage.setPixel(i, j, getBlack( (peaks[i][j]) ? 255 : 0) );
                if (peaks[i][j])
                    peakCount++;
            }
        }

        //part 4, find high and low thresholds
        low = high = 0;

        //determine how many pixels are in the x percent
        percent = (picWidth * picWidth - 1) * (percent / 100);

        // negative entered for percent will automatically find a percent to use
        //this formula is in no way perfect, I literally made arbitrary calculations until
        //the generated percent worked well for different pictures
        if (percent < 0)
        {
            final int sigmaInt = offset / 3;
            final double sigMod = sigmaInt * Math.log(sigmaInt * sigmaInt);
            percent = sigMod > 1 ? sigMod : 1;
            percent = (percent * peakCount) / (picWidth * picHeight) * 100 - sigmaInt * 3.3;
            Log.v("DominoDetection", String.format("Using percent: %f\n", percent));
            percent = (percent / 100) * (picWidth * picHeight);
        }

        int[] histogram = new int[256];
        //create histogram of image
        {
            //byte sized single channel image displayed using alpha channel only
            magnitudeImage = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);

            //create histogram
            for (int i = 0; i < picWidth; i++)
            {
                for (int j = 0; j < picHeight; j++)
                {
                    //counts how many pixels are at each value 0-255
                    final double ival2 = (ival[i][j] / maxival) * (255);
                    histogram[(int) ival2] += 1;
                    //blurred image
                    magnitudeImage.setPixel(i, j, getBlack((int) ival2));
                }
            }
        }

        //starting from 255, add the total number of pixels with each value until it exceeds the
        //threshold then use that index for the high threshold
        for (int i = (255); i >= 0; i--)
        {
            high += histogram[i];
            if (high >= percent)
            {
                high = i;
                low = (int) (high * .35);
                break;
            }
        }

        //edges is used in checkNeigbors
        edges = new boolean[picWidth][picHeight];
        //double thresholding, if the blurred image's pixel is larger than threshold set it
        //as an edge, if it between high and low then check neighbors for pixels that are edges
        //and if found make pixel an edge
        for (int i = 0; i < picWidth; i++)
        {
            for (int j = 0; j < picHeight; j++)
            {
                checkNeighbors(i, j, ival, peaks, LIMIT);
            }
        }

        //temporarily disabled images
        cannyEdgesImage = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);
//        cannyEdgesImage = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);

        //write the detected edges to file
        for (int i = 0; i < picWidth; i++)
        {
            for (int j = 0; j < picHeight; j++)
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


    private void checkNeighbors(int i, int j, double[][] checkArray, boolean[][] peaks, int numChecksLeft)
    {
        //determines if a peak will be a final edge using double thresholding
        numChecksLeft--;
        if (numChecksLeft < 0)
            return;
        if (i < 0 || j < 0 || i > (picWidth - 1) || j > (picHeight - 1))
            return;
        if (peaks[i][j])
        {
            if (checkArray[i][j] > high)
            {
                peaks[i][j] = false;
                edges[i][j] = true;
                checkNeighbors(i - 1, j, checkArray, peaks, numChecksLeft);
//                checkNeighbors(i - 1, j - 1, checkArray, peaks, numChecksLeft);
//                checkNeighbors(i, j - 1, checkArray, peaks, numChecksLeft);
//                checkNeighbors(i + 1, j - 1, checkArray, peaks, numChecksLeft);
//                checkNeighbors(i + 1, j, checkArray, peaks, numChecksLeft);
//                checkNeighbors(i + 1, j + 1, checkArray, peaks, numChecksLeft);
//                checkNeighbors(i, j + 1, checkArray, peaks, numChecksLeft);
//                checkNeighbors(i - 1, j + 1, checkArray, peaks, numChecksLeft);
            }
            else if (checkArray[i][j] < low)
            {
                peaks[i][j] = false;
            }
        }
    }


    //iterate through image until an edge is found then look for other edges nearby and group as
    //a single object
    public void findShapes()
    {
        detectedObjectsImage = Bitmap.createBitmap(picWidth, picHeight, Bitmap.Config.ARGB_8888);
        objectList = new ArrayList<>();

        for (int i = 0; i < picWidth; i++)
        {
            for (int j = 0; j < picHeight; j++)
            {
                if (edges[i][j])
                {
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
                            seedCheckDirection(p.x, p.y - 1, checkLimit, NORTH);
                            seedCheckDirection(p.x + 1, p.y, checkLimit, EAST);
                            seedCheckDirection(p.x - 1, p.y, checkLimit, WEST);
                            seedCheckDirection(p.x, p.y + 1, checkLimit, SOUTH);
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

    /**
     * seedCheckDirection "seeds" dumb path checkers to the right and left as it goes up
     * traversal functions, check is the number of empty spaces skipped over, checkLimit being how many
     * pixels this program is allowed to skip
     * @param x x in image.
     * @param y y in image.
     * @param numSpacesToCheck how many times it's recursed.
     */
    private void seedCheckDirection(int x, int y, int numSpacesToCheck, int DIRECTION)
    {
        //recursion limiter.
        numSpacesToCheck--;
        if (numSpacesToCheck < 0)
        {
            return;
        }
        //prevents image falloff.
        if (x < 0 || y < 0 || x > (picWidth - 1) || y > (picHeight - 1))
            return;

        ifEdgesAddPointAtXY(x, y);

        seedCheckDirection(x, y - 1, numSpacesToCheck, DIRECTION);

        //seeds its dumb checker workers.
        switch (DIRECTION)
        {
            //if going up or down, shoot its workers to the right and left.
            case NORTH:
            case SOUTH:
                checkDirectionDumb(x + 1, y, numSpacesToCheck, EAST);
                checkDirectionDumb(x - 1, y, numSpacesToCheck, WEST);
                break;
            //if going right or left, shoot its workers up and down.
            case EAST:
            case WEST:
                checkDirectionDumb(x, y - 1, numSpacesToCheck, NORTH);
                checkDirectionDumb(x, y + 1, numSpacesToCheck, SOUTH);
                break;
            default:
                assert false : "incorrect direction specified to seedCheckDirection.";
        }
    }

    /**
     * Adds a point at x, y, if edges is true.
     * @param x the x of the point to check.
     * @param y the y of the point to check.
     */
    private void ifEdgesAddPointAtXY(int x, int y)
    {
        if (edges[x][y])
        {
            Point point = new Point();
            point.set(x, y);
            points.add(point);
            edges[x][y] = false;
        }
    }

    /**
     * CheckDirectionDumb just goes direction, and doesn't look right or left.
     * traversal functions, check is the number of empty spaces skipped over, checkLimit being how many
     * pixels this program is allowed to skip
     * @param x x in image.
     * @param y y in image.
     * @param lookLimit limits the number of times it can go looking in a direction.
     * @param DIRECTION which direction to go
     */
    private void checkDirectionDumb(int x, int y, int lookLimit, int DIRECTION)
    {
        //prevents image falloff.
        if (x < 0 || y < 0 || x > (picWidth - 1) || y > (picHeight - 1))
            return;

        //picks a dumb checker direction to follow.
        switch (DIRECTION)
        {
            case NORTH:
                //goes lookLimit times north.
                for (int i = 0; i < lookLimit; i++)
                {
                    ifEdgesAddPointAtXY(x, y);
                    y--;
                    //ensures we don't fall off the image
                    if (y < 0)
                        break;
                }
                break;
            case SOUTH:
                //goes lookLimit times south.
                for (int i = 0; i < lookLimit; i++)
                {
                    ifEdgesAddPointAtXY(x, y);
                    y++;
                    //ensures we don't fall off the image
                    if (y > (picHeight - 1))
                        break;
                }
                break;
            case EAST:
                //goes lookLimit times east.
                for (int i = 0; i < lookLimit; i++)
                {
                    ifEdgesAddPointAtXY(x, y);
                    x++;
                    //ensures we don't fall off the image
                    if (x > (picWidth - 1))
                        break;
                }
                break;
            case WEST:
                //goes lookLimit times west.
                for (int i = 0; i < lookLimit; i++)
                {
                    ifEdgesAddPointAtXY(x, y);
                    x--;
                    //ensures we don't fall off the image
                    if (x < 0)
                        break;
                }
                break;
        }
    }

    final int INIT_POINT = -1;

    //should be placed in a FindRectangle class. It was too complex for Android to know what was going on.
    private void compareSouthwestPointRectangle(Point compareAgainst, Point current)
    {
        //go south, southwest
        if (current.x == INIT_POINT || (compareAgainst.y > current.y) || (compareAgainst.y == current.y && compareAgainst.x < current.x))
        {
            current.x = compareAgainst.x;
            current.y = compareAgainst.y;
        }
    }

    private void compareNorthwestPointRectangle(Point compareAgainst, Point current)
    {
        //go west, northwest
        if (current.x == INIT_POINT || (compareAgainst.x < current.x) || (compareAgainst.x == current.x && compareAgainst.y < current.y))
        {
            current.x = compareAgainst.x;
            current.y = compareAgainst.y;
        }
    }

    private void compareSoutheastPointRectangle(Point compareAgainst, Point current)
    {
        //go east, southeast
        if (current.x == INIT_POINT || (compareAgainst.x > current.x) || (compareAgainst.x == current.x && compareAgainst.y > current.y))
        {
            current.x = compareAgainst.x;
            current.y = compareAgainst.y;
        }
    }

    private void compareNortheastPointRectangle(Point compareAgainst, Point current)
    {
        //go north, northeast
        if (current.x == INIT_POINT || (compareAgainst.y < current.y) || (compareAgainst.y == current.y && compareAgainst.x > current.x))
        {
            current.x = compareAgainst.x;
            current.y = compareAgainst.y;
        }
    }

    private void lookForRectangle(ArrayList<Point> points, Point southWest, Point northWest, Point southEast, Point northEast)
    {
        for (Point j : points)
        {
            compareSouthwestPointRectangle(j, southWest);
            compareNorthwestPointRectangle(j, northWest);
            compareSoutheastPointRectangle(j, southEast);
            compareNortheastPointRectangle(j, northEast);
        }
    }

    public void makeShapes()
    {
        cornerList = new ArrayList<>();

        //iterate through the objects found and find the north south east and west corners of the object
        for (ArrayList<Point> i : objectList)
        {
            points = new ArrayList<>();
            Point southWest = new Point(INIT_POINT, INIT_POINT);
            Point northWest = new Point(INIT_POINT, INIT_POINT);
            Point southEast = new Point(INIT_POINT, INIT_POINT);
            Point northEast = new Point(INIT_POINT, INIT_POINT);

            lookForRectangle(i, southWest, northWest, southEast, northEast);

            points.add(southWest);
            points.add(northWest);
            points.add(northEast);
            points.add(southEast);
            cornerList.add(points);
        }
    }
}
