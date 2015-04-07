package game.gamehelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.arcLength;
import static org.opencv.imgproc.Imgproc.isContourConvex;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.minEnclosingCircle;

/**
 * Created by khawkes on 3/29/15.
 */
public class ImageProcessor {

    private boolean openCVReady = false;

    private Bitmap bitmapImage, bitmapProcessed, bitmapGray, bitmapBlur, bitmapCanny;
    private File sourceFile;

    public ImageProcessor(Context context) {

        try {

            BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {

                @Override
                public void onManagerConnected(int status) {
                    super.onManagerConnected(status);

                    if(status == LoaderCallbackInterface.SUCCESS) {

                        System.err.println("OpenCV SUCCESS!");
                        openCVReady = true;
                    }
                    else System.err.println("OpenCV FAILURE!");
                }
            };

            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, context, mLoaderCallback);
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    public boolean isOpenCVReady() {

        return openCVReady;
    }

    public void setSource(File sourceFile) {

        this.sourceFile = sourceFile;
    }

    public void loadImageBitmap() {

        bitmapImage = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
        bitmapProcessed = null;
        bitmapGray = null;
        bitmapBlur = null;
        bitmapCanny = null;
    }

    public Bitmap getBitmapImage() {

        return bitmapImage;
    }

    public Bitmap getBitmapProcessed() {

        return bitmapProcessed;
    }

    public Bitmap getBitmapGray() {

        return bitmapGray;
    }

    public Bitmap getBitmapBlur() {

        return bitmapBlur;
    }

    public Bitmap getBitmapCanny() {

        return bitmapCanny;
    }

    public int[][] createTileList(List<Rect> rectangles, List<Point> circles ) {

        int[][] TileList = new int[rectangles.size()][2];

        Rect tempRect1, tempRect2;
        int tempInt1, tempInt2;
        int tempX1, tempX2;
        int tempY1, tempY2;
        int tempH1, tempH2;
        int tempW1, tempW2;

        for(int i = 0; i < rectangles.size(); i++){

            tempInt1 = 0;
            tempInt2 = 0;

            //Check if tall or long
            //to split in x or y
            if(rectangles.get(i).height > rectangles.get(i).width){
                //if tall split by y
                tempX1 = rectangles.get(i).x;
                tempX2 = rectangles.get(i).x;

                tempY1 = rectangles.get(i).y;//top rectangle same UL corner
                tempY2 = (rectangles.get(i).y - (rectangles.get(i).height / 2));//bottom rectangle lower UL corner

                tempW1 = rectangles.get(i).width;
                tempW2 = rectangles.get(i).width;
                //new half height
                tempH1 = (rectangles.get(i).height/2);
                tempH2 = (rectangles.get(i).height/2);

                tempRect1 = new Rect(tempX1, tempY1, tempW1, tempH1);
                tempRect2 = new Rect(tempX2, tempY2, tempW2, tempH2);
            }else{
                //else wide split by x
                tempX1 = rectangles.get(i).x;//left rectangle same UL corner
                tempX2 = (rectangles.get(i).x + (rectangles.get(i).width / 2));//right rectangle righter UL corner

                tempY1 = rectangles.get(i).y;
                tempY2 = rectangles.get(i).y;
                //new half width
                tempW1 = (rectangles.get(i).width/2);
                tempW2 = (rectangles.get(i).width/2);

                tempH1 = rectangles.get(i).height;
                tempH2 = rectangles.get(i).height;

                tempRect1 = new Rect(tempX1, tempY1, tempW1, tempH1);
                tempRect2 = new Rect(tempX2, tempY2, tempW2, tempH2);
            }

            //check which circles are in these temp rectangles
            for(int j = 0; j < circles.size(); j++){

                //check first rectangle
                if(tempRect1.contains(circles.get(i))){
                    tempInt1++;//increment count
                    circles.remove(i);//remove from list, each dot only in 1 domino
                }

                //check second rectangle
                if(tempRect2.contains(circles.get(i))){
                    tempInt2++;//increment count
                    circles.remove(i);//remove from list, each dot only in 1 domino
                }
            }

            //add the new domino to the list
            TileList[i][0] = tempInt1;
            TileList[i][1] = tempInt1;
        }
        return TileList;
    }


    public int process(int colorReduce, int blurSize, int blurSigmaX, int threshold1, int threshold2) {

        if (!isOpenCVReady() || bitmapImage == null) return -1;

        Bitmap myBitmap32 = decreaseColorDepth(
                bitmapImage.copy(Bitmap.Config.RGB_565, true),
                colorReduce);

        //mats for picture conversion
        Mat rgba = new Mat(myBitmap32.getWidth(), myBitmap32.getHeight(), CvType.CV_8U);
        Mat gray = new Mat(myBitmap32.getWidth(), myBitmap32.getHeight(), CvType.CV_8U);
        Mat blur = new Mat(myBitmap32.getWidth(), myBitmap32.getHeight(), CvType.CV_8U);
        Mat cany = new Mat(myBitmap32.getWidth(), myBitmap32.getHeight(), CvType.CV_8U);

        //Convert Bitmap to MAT
        Utils.bitmapToMat(myBitmap32, rgba, true);

        //Take MAT and generate grayscale
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_BGR2GRAY);

        //make blur from grayscale
        Imgproc.GaussianBlur(gray, blur, new Size(blurSize, blurSize), blurSigmaX);

        //find canny edges from blurred grayscale
        Imgproc.Canny(blur, cany, threshold1, threshold2);

        //find all contours in the canny MAT
        Mat hierarchy = new Mat();

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(cany, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //creat MATs needed for finding things
        MatOfPoint2f tempMOP2f = new MatOfPoint2f();
        MatOfPoint2f approxMOP2F = new MatOfPoint2f();

        List<Rect> rectangles = new ArrayList<>();
        List<Point> circleCenters = new ArrayList<>();

        //check every contour
        for (int i = 0; i < contours.size(); i++) {

            //get approx polly
            contours.get(i).convertTo(tempMOP2f, CvType.CV_32FC2);
            approxPolyDP(tempMOP2f, approxMOP2F, (arcLength(tempMOP2f, true) * .02), true);

            //check for samll and non-convex
            if (/*Math.abs(contourArea(contours.get(i))) < 100 ||*/ !(isContourConvex(contours.get(i))))
            {
                continue;
            }

            //check for approx pollys with 4 sides
            if(approxMOP2F.cols()== 4 )
            {
                //Convert to MatOfPoint
                MatOfPoint points = new MatOfPoint( approxMOP2F.toArray() );

                // Get bounding rect of contour
                Rect rect = Imgproc.boundingRect(points);
                //adds to list of rectangles
                rectangles.add(rect);

            }

            //checks for circles
            if(approxMOP2F.cols() > 6 )
            {
                double area = Imgproc.contourArea(contours.get(i));
                Rect rect = Imgproc.boundingRect(contours.get(i));
                double radius = rect.width / 2;

                if((1 - ((double)rect.width/(double)rect.height) <= .2) && (1 - (area /Math.PI  * Math.pow(radius, 2))) <= .2)
                {
                    //create a small bounding circle and save its center
                    Point point = new Point();
                    float[] r = new float[contours.size()];
                    Imgproc.minEnclosingCircle(tempMOP2f, point,r);
                    circleCenters.add(point);
                }
            }
        }
        Random rnd = new Random();
        rnd.setSeed(7);

        //Imgproc.drawContours(rgba, contours, - 1, new Scalar(255,0,0,255));

        for( int i = 0; i< contours.size(); i++ )
        {
            Scalar color = new Scalar( rnd.nextInt(255), rnd.nextInt(255),  rnd.nextInt(255) );
            Imgproc.drawContours(rgba, contours, i, color, 2, 8, hierarchy, 0, new Point(0,0));
        }

        bitmapProcessed = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba, bitmapProcessed);
        bitmapGray = Bitmap.createBitmap(gray.cols(), gray.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(gray, bitmapGray);
        bitmapBlur = Bitmap.createBitmap(blur.cols(), blur.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(blur, bitmapBlur);
        bitmapCanny = Bitmap.createBitmap(cany.cols(), cany.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cany, bitmapCanny);

        createTileList(rectangles, circleCenters);//create the domino list

        return circleCenters.size();
    }

    public static Bitmap decreaseColorDepth(Bitmap src, int bitOffset) {

        int width = src.getWidth();     // get image size
        int height = src.getHeight();

        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig()); // create output bitmap
        int R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {

            for (int y = 0; y < height; ++y) {

                // get pixel color
                pixel = src.getPixel(x, y);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // round-off color offset
                R = ((R + (bitOffset / 2)) - ((R + (bitOffset / 2)) % bitOffset) - 1);
                if (R < 0) R = 0;

                G = ((G + (bitOffset / 2)) - ((G + (bitOffset / 2)) % bitOffset) - 1);
                if (G < 0) G = 0;

                B = ((B + (bitOffset / 2)) - ((B + (bitOffset / 2)) % bitOffset) - 1);
                if (B < 0) B = 0;

                // set pixel color to output bitmap
                bmOut.setPixel(x, y, Color.rgb(R, G, B));
            }
        }

        // return final image
        return bmOut;
    }
}
