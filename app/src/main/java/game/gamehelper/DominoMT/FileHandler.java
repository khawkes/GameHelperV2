package game.gamehelper.DominoMT;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by Mark Andrews on 3/26/2015.
 */
public class FileHandler {
    static String directoryName = "/GameHelper/";
    static String folder = "dominoes";

    public static void storeDomino(Bitmap b, int name, Context context){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                directoryName + folder;

        try {
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            OutputStream fOut = null;
            File file = new File(path, "" + name + ".png");
            if( file.exists())
                return;
            file.createNewFile();
            fOut = new FileOutputStream(file);

            b.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(),
                    file.getName(), file.getName());

        } catch (Exception e){
            Log.e("FileHandler", e.getMessage());

        }
    }

    public static boolean checkSd(){
        boolean externalStorageAvailable = false;
        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)) {
            externalStorageAvailable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            externalStorageAvailable = true;
        }
        return externalStorageAvailable;
    }

    public static Bitmap loadDomino(int name){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                directoryName + folder;
        Bitmap bitmap = null;
        if (checkSd()) {
            try {
                bitmap = BitmapFactory.decodeFile(path + "/" + name + ".png");
            } catch (Exception e) {
                Log.e("FileHandler", e.getMessage());
            }
        }
        return bitmap;
    }

    public static boolean checkFile(int name){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                directoryName + folder;
        File file = new File(path + "/" + name + ".png");
        return file.exists();
    }
}
