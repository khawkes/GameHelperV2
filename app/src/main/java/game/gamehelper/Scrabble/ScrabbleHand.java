package game.gamehelper.Scrabble;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import game.gamehelper.Hand;

/**
 * Created by Jacob on 4/15/2015.
 */
public class ScrabbleHand implements Hand, Parcelable
{
    private ArrayList<ScrabbleLetter> currentHand;

    public ScrabbleHand()
    {
        currentHand = new ArrayList<>();
    }


    @Override
    public int getTotalPointsHand()
    {
        return 0;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {

    }
}
