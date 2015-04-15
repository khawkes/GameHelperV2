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

    //Scoring rules:     a, b, c, ...
    static final int[] scoreTable = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10};

    //constructs this letter and stores its point value.
    public ScrabbleLetter(char c)
    {
        letter = Character.toLowerCase(c);
        pointVal = scoreTable[letter - 'a'];
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

        switch (letter)
        {
            case 'A':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallA);
                break;
            case 'B':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallB);
                break;
            case 'C':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallC);
                break;
            case 'D':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallD);
                break;
            case 'E':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallE);
                break;
            case 'F':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallF);
                break;
            case 'G':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallG);
                break;
            case 'H':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallH);
                break;
            case 'I':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallI);
                break;
            case 'J':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallJ);
                break;
            case 'K':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallK);
                break;
            case 'L':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallL);
                break;
            case 'M':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallM);
                break;
            case 'N':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallN);
                break;
            case 'O':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallO);
                break;
            case 'P':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallP);
                break;
            case 'Q':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallQ);
                break;
            case 'R':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallR);
                break;
            case 'S':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallS);
                break;
            case 'T':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallT);
                break;
            case 'U':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallU);
                break;
            case 'V':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallV);
                break;
            case 'W':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallW);
                break;
            case 'X':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallX);
                break;
            case 'Y':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallY);
                break;
            case 'Z':
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallZ);
                break;
            case ' ':
            default:
                letterPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.smallBlank);
                break;
        }
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
