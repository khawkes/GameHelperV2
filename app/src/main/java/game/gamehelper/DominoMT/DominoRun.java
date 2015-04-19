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
 * Class representing a domino run (path)
 *
 * Created by Jacob on 2/11/2015.
 */
public class DominoRun implements Parcelable
{
    private LinkedList<Domino> path;
    private int pointVal;

    /**
     * Constructs a new empty domino run.
     */
    public DominoRun()
    {
        pointVal = 0;
        path = new LinkedList<>();
    }

    /**
     * Construct a new domino run from the passed parcel.
     *
     * @param p the parcel to reconstruct the domino run from
     */
    public DominoRun(Parcel p)
    {
        //constructor for loading from save state
        ArrayList<Domino> tempList = new ArrayList<>();
        path = new LinkedList<>();

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

    /**
     * Returns the domino at the front of the domino run, but does not remove domino from the run.
     *
     * @return the domino at the front of the run.
     */
    public Domino peekFront()
    {
        return path.peek();
    }

    /**
     * Removes and returns the last domino from the run.  Updates the run with the new total point value.
     *
     * @return the last domino from the run
     */
    public Domino popEnd()
    {
        pointVal -= path.getLast().getDominoValue();
        return path.removeLast();
    }

    /**
     * Removes and returns the first domino from the run.  Updates the run with the new total point value.
     *
     * @return the first domino from the run
     */
    public Domino popFront()
    {
        pointVal -= path.getFirst().getDominoValue();
        return path.removeFirst();
    }

    /**
     * Removes all the dominoes from the run.
     */
    public void clear()
    {
        path.clear();
    }

    /**
     * Compares two domino chains by length and returns true if this run is longer than the other.
     *
     * @param other the other domino chain to compare to.
     * @return true, if this one is longer than the other.
     */
    public boolean isLongerThan(DominoRun other)
    {
        return (this.getLength() > other.getLength());
    }

    /**
     * Compares two domino chains by length and returns true if this run is shorter than the other.
     *
     * @param other the other domino chain to compare to.
     * @return True, if this one is shorter than the other.
     */
    public boolean isShorterThan(DominoRun other)
    {
        return (this.getLength() > other.getLength());
    }

    /**
     * Compares two domino chains by point value and returns true if this run has a higher point
     * value than the other run.
     *
     * @param other the other domino chain to compare to.
     * @return true, if this one is has a higher point value than the other.
     */
    public boolean hasMorePointsThan(DominoRun other)
    {
        return (this.getPointVal() > other.getPointVal());
    }

    /**
     * Compares two domino chains by point value and length and returns true if this run is longer
     * and has a higher point value then the other run.
     *
     * @param other the other domino chain to compare to.
     * @return true, if this one has more points and is longer than the other one.
     */
    @SuppressWarnings("unused")
    public boolean isBetterThan(DominoRun other)
    {
        return (this.isLongerThan(other) && this.hasMorePointsThan(other));
    }

    /**
     * Returns the length of this run (number of dominoes in the run).
     *
     * @return the length of the run
     */
    public int getLength()
    {
        return path.size();
    }

    /**
     * Returns the total point value of this run (sum of the individual dominoes in the run).
     *
     * @return the total point value of the run
     */
    public int getPointVal()
    {
        return pointVal;
    }

    /**
     * Returns a clone of this domino run.
     *
     * @return the clone of this run
     */
    public DominoRun deepClone()
    {
        DominoRun clone = new DominoRun();
        clone.path.addAll(path);
        clone.pointVal = pointVal;

        return clone;
    }

    /**
     * Attempt to add a mid-run double domino and return true if successful
     *
     * @param other the other run to compare against (must be equal till the vertex of d)
     * @param dominoVertex the vertex at which to stop
     * @param target the original target of the run.
     * @return true if successful, false otherwise.
     */
    public boolean addMidRunDouble(DominoRun other, int dominoVertex, int target)
    {
        ListIterator<Domino> thisIter = this.path.listIterator();
        ListIterator<Domino> otherIter = other.path.listIterator();
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

    /**
     * Returns the domino run as an array.
     *
     * @return domino run as an array
     */
    public Domino[] toArray()
    {
        return path.toArray(new Domino[path.size()]);
    }

    /**
     * Required for Parcelable interface.
     * Not used.
     *
     * @return zero
     */
    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     * Save this domino graph instance to a Parcel.
     *
     * @param dest the parcel to write the domino graph to
     * @param flags additional flags on how to write the parcel (not used)
     */
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

    /**
     * Parcel CREATOR for the Domino class.
     *
     * @see android.os.Parcelable.Creator
     */
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
