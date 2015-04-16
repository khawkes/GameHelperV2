package game.gamehelper.Scrabble;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import game.gamehelper.DominoMT.Domino;
import game.gamehelper.DominoMT.DominoRun;
import game.gamehelper.Hand;

/**
 * Created by Jacob on 4/15/2015.
 */
public class ScrabbleHand implements Hand, Parcelable
{
    private ArrayList<ScrabbleLetter> currentHand;
    private ScrabbleLetter playoff;

    public ScrabbleHand()
    {
        currentHand = new ArrayList<>();
        playoff = new ScrabbleLetter(' ');
    }

    public ScrabbleHand(char playoffLetter)
    {
        currentHand = new ArrayList<>();
        playoff = new ScrabbleLetter(playoffLetter);
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

    public void setPlayoffLetter(ScrabbleLetter l)
    {

    }

    public void letterPlayed(int position, ScrabbleWindow.WindowContext context)
    {

    }

    public void addLetter(ScrabbleLetter l)
    {

    }

    public ScrabbleLetter[] findUnused(ScrabbleWord compareAgainst)
    {
        return null;
    }

    public ScrabbleLetter getPlayoffLetter()
    {
        return null;
    }

    public ScrabbleWord getMostPointWord()
    {
        return null;
    }

    public ScrabbleWord getLongestWord()
    {
        return null;
    }

    public boolean undo()
    {
        return true;
    }

    public int getTotalLetters()
    {
        return currentHand.size();
    }

    public ScrabbleLetter[] toArray()
    {
        return currentHand.toArray(new ScrabbleLetter[currentHand.size()]);
    }
}
