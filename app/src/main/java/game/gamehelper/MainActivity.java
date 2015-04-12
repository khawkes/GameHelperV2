package game.gamehelper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_PICTURES;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int[] VIEW_BUTTONS = new int[] {

            R.id.btnProcess,
            R.id.btnShowBlur,
            R.id.btnShowCanny,
            R.id.btnShowGray,
            R.id.btnShowPicture,
            R.id.btnShowProcessed,
            R.id.btnTakePicture,
            R.id.btnReturn,
            R.id.btnShapes
    };
    private Bitmap file;
    private File currentPhotoPath;
    private ImageProcessor imgProcessor;
    private testdetection testimg;
    int[][] domList = null;

    private TextView countText;
    private ImageView picture;

    private int photoTaken = 0;

    static final int RULES_EXIT = 88;

    private HashMap<Integer, Button> buttons = new HashMap<>();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("currentPhotoPath", currentPhotoPath.getAbsolutePath());
        outState.putInt("photoTaken", photoTaken);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        currentPhotoPath = new File(savedInstanceState.getString("currentPhotoPath"));
        photoTaken = savedInstanceState.getInt("photoTaken");
        if(photoTaken >= 1)
            onActivityResult(RESULT_OK, REQUEST_IMAGE_CAPTURE, null);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imgProcessor = new ImageProcessor(this);

        setContentView(R.layout.activity_main);
        countText = (TextView) findViewById(R.id.countText);
        picture = (ImageView) findViewById(R.id.imageView);

        for (Integer buttonRes : VIEW_BUTTONS) {

            Button button = (Button)findViewById(buttonRes);
            button.setOnClickListener(this);
            button.setEnabled(false);
            buttons.put(buttonRes, button);
        }

        buttons.get(R.id.btnTakePicture).setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            case R.id.btnTakePicture:
                createImageFile();
                dispatchTakePictureIntent();
                setButtons();
                break;
//            case R.id.btnShowPicture:
//                picture.setImageBitmap(imgProcessor.getBitmapImage());
//                countText.setText(currentPhotoPath.getAbsolutePath());
//                break;
//            case R.id.btnShowProcessed:
//                picture.setImageBitmap(imgProcessor.getBitmapProcessed());
//                countText.setText("Processed");
//                break;
//            case R.id.btnShowGray:
//                picture.setImageBitmap(imgProcessor.getBitmapGray());
//                countText.setText("Gray");
//                break;
//            case R.id.btnShowBlur:
//                picture.setImageBitmap(imgProcessor.getBitmapBlur());
//                countText.setText("Blur");
//                break;
//            case R.id.btnShowCanny:
//                picture.setImageBitmap(imgProcessor.getBitmapCanny());
//                countText.setText("Canny");
            case R.id.btnShowPicture:
                picture.setImageBitmap(file);
                countText.setText(currentPhotoPath.getAbsolutePath());
                break;
            case R.id.btnShowProcessed:
                picture.setImageBitmap(testimg.finalfile);
                countText.setText("Processed");
                break;
            case R.id.btnShowGray:
                picture.setImageBitmap(testimg.bwfile);
                countText.setText("Gray");
                break;
            case R.id.btnShowBlur:
                picture.setImageBitmap(testimg.histogramfile);
                countText.setText("Blur");
                break;
            case R.id.btnShowCanny:
                picture.setImageBitmap(testimg.peaksfile);
                countText.setText("Canny");
                break;
            case R.id.btnProcess:
                testimg.findShapes();
                Log.w("finding shapes", "finished");
                picture.setImageBitmap(testimg.shapesfile);
                buttons.get(R.id.btnShapes).setEnabled(true);
                break;
            case R.id.btnShapes:
                testimg.makeShapes();
                if(testimg.rectangle != null) {
                    domList = testimg.rectangle.getDominoes();
                    countText.setText(Integer.toString(domList.length));
                    buttons.get(R.id.btnReturn).setEnabled(true);
                }
                break;
            case R.id.btnReturn:
                Bundle b = new Bundle();
                Intent intent = getIntent();

                b.putSerializable("dominoList", domList);
                b.putInt("dominoTotal", domList.length);

                intent.putExtras(b);
                setResult(RESULT_OK, intent);
                finish();
                return;
        }
    }

    private int safeInt(CharSequence val, int def) {

        int ret = def;
        try {

            ret = Integer.parseInt(val.toString());
        }
        catch (Exception e) { }

        return ret;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            photoTaken = 2;
//            imgProcessor.setSource(currentPhotoPath);
//            imgProcessor.loadImageBitmap();
//            picture.setImageBitmap(imgProcessor.getBitmapImage());
            file = BitmapFactory.decodeFile(currentPhotoPath.getAbsolutePath());
            file = Bitmap.createScaledBitmap(file, (int)(file.getWidth()*.1), (int)(file.getHeight()*.1), false);
            picture.setImageBitmap(file);
            testimg = new testdetection(file);
            setButtons();
        }
    }

    private void createImageFile() {

        try {
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
        catch(IOException ioe) {

            // TODO: Display error message to user, cannot process image.
            // Use manual domino entry.
            ioe.printStackTrace();
        }
    }

    private void dispatchTakePictureIntent() {

        if (currentPhotoPath == null) return;

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentPhotoPath));
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            photoTaken = 1;
        }
    }

    private void setButtons() {

//        if (imgProcessor.getBitmapImage() != null) {
//
//            buttons.get(R.id.btnShowPicture).setEnabled(true);
//            buttons.get(R.id.btnProcess).setEnabled(true);
//        }
//
//        if (imgProcessor.getBitmapProcessed() != null) buttons.get(R.id.btnShowProcessed).setEnabled(true);
//        if (imgProcessor.getBitmapGray() != null) buttons.get(R.id.btnShowGray).setEnabled(true);
//        if (imgProcessor.getBitmapBlur() != null) buttons.get(R.id.btnShowBlur).setEnabled(true);
//        if (imgProcessor.getBitmapCanny() != null) buttons.get(R.id.btnShowCanny).setEnabled(true);

        buttons.get(R.id.btnProcess).setEnabled(true);
        buttons.get(R.id.btnShowProcessed).setEnabled(true);
        buttons.get(R.id.btnShowGray).setEnabled(true);
        buttons.get(R.id.btnShowBlur).setEnabled(true);
        buttons.get(R.id.btnShowCanny).setEnabled(true);
    }
}
