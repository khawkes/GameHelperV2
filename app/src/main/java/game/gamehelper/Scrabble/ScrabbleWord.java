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
public class ScrabbleWord
{
    int score;
    String word;

    /**
     * Creates a scrabble word, and calculates the score.
     * @param in The string to create the word from.
     */
    ScrabbleWord(String in)
    {
        word = in.toUpperCase();
        setScore();
    }

    /**
     * Creates a scrabble word, minus the deviation score.
     * @param in The string to create the word from.
     * @param devScore The deviation score to subtract from the found score.
     */
    ScrabbleWord(String in, int devScore)
    {
        word = in.toUpperCase();
        setScore();
        score -= devScore;
    }

    /**
     * Sets the score according to scrabble rules
     */
    private void setScore()
    {
        score = 0;
        int[] scoreTable = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10};

        char[] charArray = word.toCharArray();
        for (char c : charArray)
        {
            score += scoreTable[c - 'A'];
        }

        if (word.length() == 8)
            score += 50;
    }

    /**
     * validates and scores a string.
     * @param in the string to validate.
     * @return the score of the string.
     */
    public static int findScore(String in) {
        int[] scoreTable = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10};
        int scoreTemp = 0;

        for (char c : in.toLowerCase().toCharArray())
        {
            //validation
            if (c >= 'a' && c <= 'z' && c < 128)
            {
                scoreTemp += scoreTable[c - 'a'];
            }
        }

        return scoreTemp;
    }

    @Override
    /**
     * Converts the score to a string, and appends a Bingo notice at the end, if it occurs.
     * @return the converted word-to-string, with point value and bingo notice at end.
     */
    public String toString()
    {
        if (word.length() == 8)
            return word + " " + (score - 50) + "\nScrabble Bingo! +50 points (" + score + ")";
        else
            return word + " " + score;
    }

    public int getPointVal()
    {
        return score;
    }

    public int getLength()
    {
        return word.length();
    }

    //comparator for score-based comparison.
    public static class compareByScore implements Comparator<ScrabbleWord>
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

    //comparator for score-based comparison.
    public static class compareByLength implements Comparator<ScrabbleWord>
    {
        @Override
        //compares two ScrabbleWords by score.
        public int compare(ScrabbleWord lhs, ScrabbleWord rhs)
        {
            if (lhs.getLength() < rhs.getLength())
                return -1;
            else if (lhs.getLength() > rhs.getLength())
                return 1;
            else
                return 0;
        }
    }


    //comparator for score-based comparison.
    public static class invertCompareByScore implements Comparator<ScrabbleWord>
    {
        @Override
        //compares two ScrabbleWords by score.
        public int compare(ScrabbleWord lhs, ScrabbleWord rhs)
        {
            if (lhs.score > rhs.score)
                return -1;
            else if (lhs.score < rhs.score)
                return 1;
            else
                return 0;
        }
    }

    //comparator for score-based comparison.
    public static class invertCompareByLength implements Comparator<ScrabbleWord>
    {
        @Override
        //compares two ScrabbleWords by score.
        public int compare(ScrabbleWord lhs, ScrabbleWord rhs)
        {
            if (lhs.getLength() > rhs.getLength())
                return -1;
            else if (lhs.getLength() < rhs.getLength())
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

}
