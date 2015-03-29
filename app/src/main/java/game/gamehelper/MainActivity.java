package game.gamehelper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.arcLength;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.isContourConvex;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private Mat canny;
    private Mat gray;
    private Mat hierarchy;
    private Mat blur;
    private Mat rgba;

    private String CurrentPhotoPath;

    private Button pictureButton;
    private Button pictureButtonGray;
    private Button pictureButtonBlur;
    private Button pictureButtonCanny;
    private Button pictureButtonProc;
    private Button countButton;

    private TextView countText;
    private ImageView picture;

    private Bitmap bitmapRgba;
    private Bitmap bitmapGray;
    private Bitmap bitmapBlur;
    private Bitmap bitmapCanny;

    static final int RULES_EXIT = 88;



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);

            if(status == LoaderCallbackInterface.SUCCESS) {

                System.err.println("SUCCESS!");
            }
            else System.err.println("FAILURE");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        countText = (TextView) findViewById(R.id.countText);
        pictureButton = (Button) findViewById(R.id.pictureButton);
        pictureButton.setOnClickListener(this);
        countButton = (Button) findViewById(R.id.countButton);
        countButton.setOnClickListener(this);
        picture = (ImageView) findViewById(R.id.imageView);
        pictureButton = (Button) findViewById(R.id.showPicture);
        pictureButton.setOnClickListener(this);
        pictureButtonGray = (Button) findViewById(R.id.showPictureGray);
        pictureButtonGray.setOnClickListener(this);
        pictureButtonProc = (Button) findViewById(R.id.showPictureProcessed);
        pictureButtonProc.setOnClickListener(this);
        pictureButtonBlur = (Button) findViewById(R.id.showPictureBlur);
        pictureButtonBlur.setOnClickListener(this);
        pictureButtonCanny = (Button) findViewById(R.id.showPictureCanny);
        pictureButtonCanny.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (item.getItemId())
        {
            case R.id.menu_rules:
            {
                startActivityForResult(new Intent(MainActivity.this, RulesActivity.class),RULES_EXIT);

                break;
            }

            case R.id.menu_exit:
            {
                finish();
                System.exit( 0 );
                break;
            }

            default:

        }

        return( super.onOptionsItemSelected(item) );
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.countButton:
                String count = new String();
                count = Integer.toString(findDominoesInPicture());
                countText.setText(count);
                break;
            case R.id.pictureButton:
                dispatchTakePictureIntent();
                break;
            case R.id.showPicture:
                Bitmap bitmap = BitmapFactory.decodeFile(CurrentPhotoPath);
                picture.setImageBitmap(bitmap);
                countText.setText(CurrentPhotoPath);
                break;
            case R.id.showPictureGray:
                bitmapGray = Bitmap.createBitmap(gray.cols(), gray.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(gray, bitmapGray);
                picture.setImageBitmap(bitmapGray);
                countText.setText("Gray");
                break;
            case R.id.showPictureBlur:
                bitmapBlur = Bitmap.createBitmap(blur.cols(), blur.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(blur, bitmapBlur);
                picture.setImageBitmap(bitmapBlur);
                countText.setText("Processed");
                break;
            case R.id.showPictureCanny:
                bitmapCanny = Bitmap.createBitmap(canny.cols(), canny.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(canny, bitmapCanny);
                picture.setImageBitmap(bitmapCanny);
                countText.setText("Processed");
                break;
            case R.id.showPictureProcessed:
                bitmapRgba = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(rgba, bitmapRgba);
                picture.setImageBitmap(bitmapRgba);
                countText.setText("Processed");
                break;

        }
    }

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Bitmap bitmap = BitmapFactory.decodeFile(CurrentPhotoPath);
            picture.setImageBitmap(bitmap);
            //countText.setText(finddominoesInPicture());
            // crash if processing step is added here
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        CurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public boolean onTouch(View v, MotionEvent event) {

        return false;
    }

    public int findDominoesInPicture()
    {

        List<Rect> rectangles = new ArrayList<Rect>();
        List<Point> circleCenters = new ArrayList<Point>();

        //open picture and convert to Bitmap
        File file = new File(CurrentPhotoPath);
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        Bitmap myBitmap32 = myBitmap.copy(Bitmap.Config.ARGB_8888, true);

        //mats for picture conversion
        rgba = new Mat(myBitmap32.getWidth(),myBitmap32.getHeight(), CvType.CV_8UC1);
        gray = new Mat(myBitmap32.getWidth(),myBitmap32.getHeight(), CvType.CV_8UC1);
        canny = new Mat(myBitmap32.getWidth(),myBitmap32.getHeight(), CvType.CV_8UC1);
        blur = new Mat(myBitmap32.getWidth(),myBitmap32.getHeight(), CvType.CV_8UC1);
        hierarchy = new Mat();

        //Convert Bitmap to MAT
        Utils.bitmapToMat(myBitmap32,rgba,true);

        //Take MAT and generate grayscale
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_BGR2GRAY);
        //make blur from grayscale
        Imgproc.GaussianBlur(gray, blur, new Size(9, 9), 0);
        //find canny edges from blurred grayscale
        Imgproc.Canny(blur, canny, 100, 200);

        //find all contours in the canny MAT
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //creat MATs needed for finding things
        MatOfPoint2f tempMOP2f = new MatOfPoint2f();
        MatOfPoint2f approxMOP2F = new MatOfPoint2f();

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
                    //creat a small bounding circle and save its center
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







        return circleCenters.size();
    }


}
