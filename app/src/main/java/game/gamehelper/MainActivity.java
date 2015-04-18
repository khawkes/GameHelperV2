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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import game.gamehelper.DominoMT.DetectedShape;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Activity responsible for the capture, display, and detection of dominoes from the
 * device camera.
 */
public class MainActivity extends ActionBarActivity implements View.OnClickListener
{
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int[] VIEW_BUTTONS = new int[] {

            R.id.btnTakePicture,
            R.id.btnProcess,
            R.id.btnReturn,
            R.id.btnShowGray,
            R.id.btnShowHistogram,
            R.id.btnShowPeaks,
            R.id.btnShowShapes,
            R.id.btnShowFinal,
            R.id.btnShowOriginal,
            R.id.btnShowUsedShapes
    };

    private Bitmap file;
    private File currentPhotoPath;
    private DominoDetection detector;
    int[][] domList = null;

    private TextView countText;
    private ImageView picture;

    private int photoTaken = 0;

    static final int RULES_EXIT = 88;

    private HashMap<Integer, Button> buttons = new HashMap<>();

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putString("currentPhotoPath",
                (currentPhotoPath == null ? "" :
                    currentPhotoPath.getAbsolutePath()));
        outState.putInt("photoTaken", photoTaken);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        currentPhotoPath = new File(savedInstanceState.getString("currentPhotoPath"));
        photoTaken = savedInstanceState.getInt("photoTaken");

        //attempt to handle reconstruction
        switch (photoTaken)
        {
            case 1:
                //photo window called
                onActivityResult(RESULT_OK, REQUEST_IMAGE_CAPTURE, null);
                break;
            case 2:
                //returned from photo window
                processPicture();
                break;
            default:

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        countText = (TextView) findViewById(R.id.countText);
        picture = (ImageView) findViewById(R.id.imageView);

        for (Integer buttonRes : VIEW_BUTTONS)
        {

            Button button = (Button) findViewById(buttonRes);
            button.setOnClickListener(this);
            button.setEnabled(false);
            buttons.put(buttonRes, button);
        }

        buttons.get(R.id.btnTakePicture).setEnabled(true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId())
        {
            case R.id.menu_rules:
            {
                startActivityForResult(new Intent(MainActivity.this, RulesActivity.class), RULES_EXIT);

                break;
            }

            case R.id.menu_exit:
            {
                finish();
                System.exit(0);
                break;
            }

            default:

        }

        return (super.onOptionsItemSelected(item));
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnTakePicture:
                createImageFile();
                dispatchTakePictureIntent();
                setButtons(false);
                break;

            case R.id.btnProcess:
                //convert bitmap to list of points[]s representing contiguous segments
                detector.findShapes();
                Log.w("ImageProcessing", "Find Shapes Finished");
                picture.setImageBitmap(detector.getShapesImage());

                //find corners of point[]s and determine if rectangle circle or neither
                detector.makeShapes();
                Log.w("ImageProcessing", "Make Shapes Finished");

                DetectedShape shapes = detector.getDetectedShape();
                if (shapes != null)
                {
                    //iterate through rectangle list and find circles that are within each side
                    domList = shapes.getDominoes();
                    countText.setText(Integer.toString(domList.length));
                    buttons.get(R.id.btnReturn).setEnabled(true);
                }
                break;

            case R.id.btnReturn:
                //return results to caller
                Bundle b = new Bundle();
                Intent intent = getIntent();

                b.putSerializable("dominoList", domList);
                b.putInt("dominoTotal", domList.length);

                intent.putExtras(b);
                setResult(RESULT_OK, intent);
                finish();
                break;

            case R.id.btnShowGray:
                picture.setImageBitmap(detector.getBWImage());
                countText.setText("Gray");
                break;
            case R.id.btnShowHistogram:
                picture.setImageBitmap(detector.getHistogramImage());
                countText.setText("Histogram");
                break;
            case R.id.btnShowPeaks:
                picture.setImageBitmap(detector.getPeaksImage());
                countText.setText("Peaks");
                break;
            case R.id.btnShowShapes:
                picture.setImageBitmap(detector.getShapesImage());
                countText.setText("Shapes");
                break;
            case R.id.btnShowFinal:
                picture.setImageBitmap(detector.getFinalImage());
                countText.setText("Final");
                break;
            case R.id.btnShowOriginal:
                //show peaks image
                picture.setImageBitmap(file);
                countText.setText("Original");
                break;
            case R.id.btnShowUsedShapes:
                //show shapes generated from points
                picture.setImageBitmap(detector.getDetectedShape().getUsedShapes());
                countText.setText("Shapes From Points");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            photoTaken = 2;

            processPicture();
        }
    }

    private void processPicture()
    {
        //read file and scale to 10% (if image is too large the stack will overflow on edge detection)
        //size picked arbitrarily, don't know optimal size
        file = BitmapFactory.decodeFile(currentPhotoPath.getAbsolutePath());

        int longestSide = Math.max(file.getWidth(), file.getHeight());
        double scale = longestSide / 1000;
        int width = (int) (file.getWidth() / scale);
        int height = (int) (file.getHeight() / scale);

        file = Bitmap.createScaledBitmap(file, width, height, false);
        picture.setImageBitmap(file);

        detector = new DominoDetection(file, 1, 20, 25, 3, 9);
        detector.processImage();
        Log.w("ImageProcessing", "Processing Image Done");

        setButtons(true);
    }

    private void createImageFile()
    {
        try
        {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
            currentPhotoPath = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        }
        catch (IOException ioe)
        {

            // TODO: Display error message to user, cannot process image.
            // Use manual domino entry.
            ioe.printStackTrace();
        }
    }

    private void dispatchTakePictureIntent()
    {
        if (currentPhotoPath == null) return;

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentPhotoPath));
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            photoTaken = 1;
        }
    }

    private void setButtons(boolean enabled)
    {
        buttons.get(R.id.btnProcess).setEnabled(true);

        buttons.get(R.id.btnShowGray).setEnabled(enabled && detector.isProcessed());
        buttons.get(R.id.btnShowHistogram).setEnabled(enabled && detector.isProcessed());
        buttons.get(R.id.btnShowPeaks).setEnabled(enabled && detector.isProcessed());
        buttons.get(R.id.btnShowShapes).setEnabled(enabled && detector.isProcessed());
        buttons.get(R.id.btnShowFinal).setEnabled(enabled && detector.isProcessed());
        buttons.get(R.id.btnShowOriginal).setEnabled(enabled && detector.isProcessed());
        buttons.get(R.id.btnShowUsedShapes).setEnabled(enabled && detector.isProcessed());
    }
}
