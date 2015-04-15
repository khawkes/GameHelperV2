package game.gamehelper.Scrabble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

import game.gamehelper.R;

/**
 * Created by Andrew on 4/13/2015.
 */
public class ScrabbleWord implements Parcelable
{
    int score;
    LinkedList<ScrabbleLetter> word;

    //stores word, and sets score for word.
    ScrabbleWord(String in)
    {
        score = 0;

        for (char c : in.toCharArray())
        {
            word.add(new ScrabbleLetter(c));
            score += word.getLast().getPointVal();
        }

        //special rule; scrabble bingo
        if (word.size() == 8)
        {
            score += 50;
        }
    }

    //loads a ScrabbleWord from a Parcel.
    ScrabbleWord(Parcel in)
    {
        word = new LinkedList<>();
        in.readTypedList(word, ScrabbleLetter.CREATOR);
        score = in.readInt();
    }

    public int getScore()
    {
        return score;
    }

    //comparator for sorting classes.
    public class compareByScore implements Comparator<ScrabbleWord>
    {
        @Override
        //compares two ScrabbleWords by score.
        public int compare(ScrabbleWord lhs, ScrabbleWord rhs)
        {
            if (lhs.score < rhs.score)
                return -1;
            else if (lhs.score > rhs.score)
                return 1;
            else
                return 0;
        }
    }

    @Override
    //to allow for list-usage.
    public int hashCode()
    {
        return word.size() * score;
    }

    @Override
    //to allow for list-usage.
    public boolean equals(Object obj)
    {
        if (obj == null || (obj.getClass() != this.getClass()))
        {
            return false;
        }

        ScrabbleWord other = (ScrabbleWord) obj;

        if (other.score > this.score || other.score < this.score)
            return false;
        else if (other.word.equals(this.word))
            return false;
        else
            return true;
    }

    @Override
    //Required for Parcelable
    public int describeContents()
    {
        return 0;
    }

    @Override
    //Writes this scrabble-word to a parcel
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeTypedList(word);
        dest.writeInt(score);
    }
}
