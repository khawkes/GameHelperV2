package game.gamehelper.DominoMT;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;

import game.gamehelper.R;

/**
 * Created by Mark Andrews on 3/26/2015.
 */
public class DominoGenerator extends AsyncTask<Context, Integer, Integer> {
    @Override
    protected Integer doInBackground(Context[] params) {
        Bitmap bitmap = null;

        for(int i = 0 ; i <= 16 ; i++){
            for(int j = 0 ; j <= 16 ; j++){
                if(FileHandler.checkFile(i*17 + j))
                    continue;
                bitmap = buildDomino(i,j, params[0]);
                FileHandler.storeDomino(bitmap, (i*17 + j), params[0]);
            }
        }
        return null;
    }

    //Load background and write each side on top
    public Bitmap buildDomino(int val1, int val2, Context context){

        Bitmap side1;
        Bitmap side2;
        Bitmap bg;

        bg = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_bg);
        side1 = Domino.getSide(val1, context);
        side2 = Domino.getSide(val2, context);

        //copy immutable bitmap generated previously to a mutable bitmap and impose the sides
        bg = bg.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bg);
        canvas.drawBitmap(side1, 0, 0, null);
        canvas.drawBitmap(side2, side2.getWidth(), 0, null);

        //free space
        side1.recycle();
        side2.recycle();

        return bg;
    }
}
