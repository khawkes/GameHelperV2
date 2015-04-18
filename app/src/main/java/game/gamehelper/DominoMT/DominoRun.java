/*
 * COP4331C - Class Project - The Game Helper App
 * Spring 2015
 *
 * Project authors:
 *   Mark Andrews
 *   Jacob Cassagnol
 *   Kurt Hawkes
 *   Tim McCarthy
 *   Andrew McKenzie
 *   Amber Stewart
 */

package game.gamehelper.DominoMT;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Jacob on 2/11/2015.
 * A domino run (path)
 */
public class DominoRun implements Parcelable
{
    private LinkedList<Domino> path;
    private int pointVal;

    public DominoRun()
    {
        pointVal = 0;
        path = new LinkedList<Domino>();
    }

    public DominoRun(Parcel p)
    {
        //constructor for loading from save state
        ArrayList<Domino> tempList = new ArrayList<>();
        path = new LinkedList<Domino>();

        p.readList(tempList, null);
        pointVal = p.readInt();

        for (Domino d : tempList)
        {
            path.add(d);
        }
    }

    /**
     * Adds a domino to DominoRun. Can technically not allign with previous dominoes, as we're
     * concerned with efficiency, not with silly things like a real domino run.
     *
     * @param d The new domino to add to the end of the run.
     */
    public void addDomino(Domino d)
    {
        path.addLast(d);
        pointVal += d.getDominoValue();
    }

    public Domino peekFront()
    {
        return path.peek();
    }

    public Domino popEnd()
    {
        pointVal -= path.getLast().getDominoValue();
        return path.removeLast();
    }

    public Domino popFront()
    {
        pointVal -= path.getFirst().getDominoValue();
        return path.removeFirst();
    }

    public void clear()
    {
        path.clear();
    }

    /**
     * compares two domino chains by length.
     *
     * @param other The other domino chain to compare to.
     * @return True, if this one is longer than the other.
     */
    public boolean isLongerThan(DominoRun other)
    {
        return (this.getLength() > other.getLength());
    }

    /**
     * compares two domino chains by length.
     *
     * @param other The other domino chain to compare to.
     * @return True, if this one is shorter than the other.
     */
    public boolean isShorterThan(DominoRun other)
    {
        return (this.getLength() > other.getLength());
    }

    /**
     * compares two domino chains by point value.
     *
     * @param other The other domino chain to compare to.
     * @return True, if this one is worth more points than the other.
     */
    public boolean hasMorePointsThan(DominoRun other)
    {
        return (this.getPointVal() > other.getPointVal());
    }

    /**
     * compares two domino chains by point value and length.
     *
     * @param other The other domino chain to compare to.
     * @return True, if this one has more points and is longer than the other one.
     */
    public boolean isBetterThan(DominoRun other)
    {
        return (this.isLongerThan(other) && this.hasMorePointsThan(other));
    }

    public int getLength()
    {
        return path.size();
    }

    public int numMoves()
    {
        return this.getLength();
    }

    public int getPointVal()
    {
        return pointVal;
    }

    //Deep copy of this run.
    public DominoRun deepCopy()
    {
        DominoRun copy = new DominoRun();

        for (Domino d : path)
        {
            copy.addDomino(d);
        }
        return copy;
    }

    //equals method
    public boolean isEqualTo(DominoRun other)
    {
        //checks for same points, .equals will check for length
        if (this.getPointVal() != other.getPointVal())
            return false;
        return path.equals(other.getPath());
    }

    /**
     * this method attempts to add a mid-run double, returning true if successful
     *
     * @param other        The other run to compare against (must be equal till the vertex of d)
     * @param dominoVertex The vertex at which to stop
     * @param target       The original target of the run.
     * @return True if success, false if failure.
     */
    public boolean addMidRunDouble(DominoRun other, int dominoVertex, int target)
    {
        ListIterator<Domino> thisIter = this.getPath().listIterator();
        ListIterator<Domino> otherIter = other.getPath().listIterator();
        boolean firstRun = true;

        //DominoRun isn't guaranteed to be ordered, as that would be dangerous during development.
        //This method won't be called much, anyways.
        while (thisIter.hasNext() && otherIter.hasNext())
        {
            Domino nextDomino = thisIter.next();

            //if we hit a domino that's not equal, exit; we can't add the double.
            if (!nextDomino.equals(otherIter.next()))
                return false;

            //We know the dominoes are equal, test to see if they share a side with our double.
            if (nextDomino.getVal1() == dominoVertex || nextDomino.getVal2() == dominoVertex)
            {
                //if it's at the first position and the vertex matches what we have to play on,
                // we have to back up the iterators. Otherwise, it's fine.
                if (firstRun && target == dominoVertex)
                {
                    thisIter.previous();
                    otherIter.previous();
                }

                //add to list at new position
                thisIter.add(new Domino(dominoVertex, dominoVertex));
                otherIter.add(new Domino(dominoVertex, dominoVertex));

                //remember to add points!
                this.pointVal += dominoVertex * 2;
                other.pointVal += dominoVertex * 2;

                return true;
            }

            //If we reach here, we're not on the first loop through anymore.
            firstRun = false;
        }

        //since there aren't any more dominoes, we've failed.
        return false;
    }

    //returns these dominoes as an array.
    public Domino[] toArray()
    {
        return path.toArray(new Domino[path.size()]);
    }

    //returns the path for private functionality
    private LinkedList<Domino> getPath()
    {
        return this.path;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        ArrayList<Domino> list = new ArrayList<Domino>();
        for (Domino d : path)
        {
            list.add(d);
        }

        dest.writeList(list);
        dest.writeInt(pointVal);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public DominoRun createFromParcel(Parcel in)
        {
            return new DominoRun(in);
        }

        public DominoRun[] newArray(int size)
        {
            return new DominoRun[size];
        }
    };
}
