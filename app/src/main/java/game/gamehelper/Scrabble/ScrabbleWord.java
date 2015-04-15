package game.gamehelper.Scrabble;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

/**
 * Created by Andrew on 4/13/2015.
 */
public class ScrabbleWord implements Parcelable
{
    int score;
    String word;

    //stores word, and sets score for word.
    ScrabbleWord(String in)
    {
        word = in.toUpperCase();
        setScore();
    }

    //loads a ScrabbleWord from a Parcel.
    ScrabbleWord(Parcel in)
    {
        word = in.readString();
        score = in.readInt();
    }

    private void setScore()
    {
        score = 0;
        //Scoring rules:     a, b, c, ...
        int[] scoreTable = { 1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10 };

        char[] charArray = word.toCharArray();
        for (char c: charArray)
        {
            score += scoreTable[c - 'A'];
        }

        //special scrabble word. TECHNICALLY doesn't account for start tile.
        if (word.length() == 8)
            score += 150;
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
        public int compare(ScrabbleWord lhs, ScrabbleWord rhs) {
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
        return word.length() * score;
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
        dest.writeString(word);
        dest.writeInt(score);
    }

    public static Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        @Override
        public ScrabbleWord createFromParcel(Parcel source)
        {
            return new ScrabbleWord(source);
        }

        @Override
        public ScrabbleWord[] newArray(int size)
        {
            return new ScrabbleWord[size];
        }
    };
}
