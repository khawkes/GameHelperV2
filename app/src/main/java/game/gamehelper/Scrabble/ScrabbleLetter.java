package game.gamehelper.Scrabble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import game.gamehelper.R;

/**
 * Created by Jacob on 4/15/2015.
 */
public class ScrabbleLetter implements Parcelable
{
    final char letter;
    final int pointVal;

    //holds the picture references
    public static final int[] scrabIdList = new int[]
    {
        R.drawable.smallBlank,
        R.drawable.smallA,
        R.drawable.smallB,
        R.drawable.smallC,
        R.drawable.smallD,
        R.drawable.smallE,
        R.drawable.smallF,
        R.drawable.smallG,
        R.drawable.smallH,
        R.drawable.smallI,
        R.drawable.smallJ,
        R.drawable.smallK,
        R.drawable.smallL,
        R.drawable.smallM,
        R.drawable.smallN,
        R.drawable.smallO,
        R.drawable.smallP,
        R.drawable.smallQ,
        R.drawable.smallR,
        R.drawable.smallS,
        R.drawable.smallT,
        R.drawable.smallU,
        R.drawable.smallV,
        R.drawable.smallW,
        R.drawable.smallX,
        R.drawable.smallY,
        R.drawable.smallZ,
    };

    //Scoring rules:     a, b, c, ...
    static final int[] scoreTable = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10};

    //constructs this letter and stores its point value.
    public ScrabbleLetter(char c)
    {
        letter = Character.toLowerCase(c);
        if (c != ' ')
            pointVal = scoreTable[letter - 'a'];
        else
            pointVal = 0;
    }

    //constructs a letter from a parcel
    public ScrabbleLetter(Parcel p) {
        letter = (char) p.readInt();
        pointVal = p.readInt();
    }

    public char getChar()
    {
        return letter;
    }

    public int getPointVal()
    {
        return  pointVal;
    }

    //Loads an image for a ScrabbleLetter.
    public static Bitmap getLetterPic(ScrabbleLetter l, Context context)
    {
        return getLetterPic(l.getChar(), context);
    }

    //Loads an image for a char
    public static Bitmap getLetterPic(char letter, Context context)
    {
        Bitmap letterPic;
        letter = Character.toLowerCase(letter);

        //if it's not a space, look it up in the table above, shifting to match the table.
        if (letter != ' ')
            letterPic = BitmapFactory.decodeResource(context.getResources(), scrabIdList[letter - 'a' + 1]);
        else
            letterPic = BitmapFactory.decodeResource(context.getResources(), scrabIdList[0]);

        return letterPic;
    }

    @Override
    //required for parcelable
    public int describeContents()
    {
        return 0;
    }

    @Override
    //stores this letter in a parcel.
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(letter);
        dest.writeInt(pointVal);
    }

    public static Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        @Override
        public ScrabbleLetter createFromParcel(Parcel source)
        {
            return new ScrabbleLetter(source);
        }

        @Override
        public ScrabbleLetter[] newArray(int size)
        {
            return new ScrabbleLetter[size];
        }
    };
}
