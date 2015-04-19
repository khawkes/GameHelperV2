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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import game.gamehelper.R;

/**
 * Defines a single domino instance consisting of
 * two values for each side of the domino (val1, val2)
 * and a sum of those values.
 *
 * Created by Jacob on 2/11/2015.
 */
public class Domino implements Parcelable
{
    public static final int[] domIdList = new int[] {
            R.drawable.side_border,
            R.drawable.dom_one,
            R.drawable.dom_two,
            R.drawable.dom_three,
            R.drawable.dom_four,
            R.drawable.dom_five,
            R.drawable.dom_six,
            R.drawable.dom_seven,
            R.drawable.dom_eight,
            R.drawable.dom_nine,
            R.drawable.dom_ten,
            R.drawable.dom_eleven,
            R.drawable.dom_twelve,
            R.drawable.dom_thirteen,
            R.drawable.dom_fourteen,
            R.drawable.dom_fifteen,
            R.drawable.dom_sixteen,
            R.drawable.dom_seventeen,
            R.drawable.dom_eighteen
    };

    // Setting values to be final.
    // Dominoes are not mutable.
    private final int val1;
    private final int val2;
    private final int sum;

    /**
     * Create a new domino based on the two provided
     * values (one for each side of the domino)/
     *
     * @param value1 number of dots on first side
     * @param value2 number of dots on second side
     */
    public Domino(int value1, int value2)
    {
        val1 = value1;
        val2 = value2;
        sum = getVal1() + getVal2();
    }

    /**
     * Reconstruct a domino from an Android Parcel.  Used by Android
     * to pass parameters between activities and intents.  Performance
     * is faster than Java's serializable.
     *
     * @param p the parcel to construct the domino from.
     */
    public Domino(Parcel p)
    {
        val1 = p.readInt();
        val2 = p.readInt();
        sum = val1 + val2;
    }

    /**
     * Return the value of the first side of the domino.
     *
     * @return domino side 1 value (number of dots on the side)
     */
    public int getVal1()
    {
        return val1;
    }

    /**
     * Return the value of the second side of the domino.
     *
     * @return domino side 2 value (number of dots on the side)
     */
    public int getVal2()
    {
        return val2;
    }

    /**
     * Returns the value of the side that is not the side value passed
     * in.  Assumes that the provided side is valid for this domino.
     * This is an internal function used by the RunController and
     * should not be used outside that object.
     *
     * @param val the value of the side we have
     * @return the value of the other side.
     */
    int getOtherVal(int val)
    {
        return (val == val1) ? val2 : val1;
    }

    /**
     * Returns the total value of this domino (sum of both side values).
     *
     * @return total value of the domino
     */
    public int getDominoValue()
    {
        return sum;
    }

    /**
     * Compares two dominoes and returns true if the dominoes are equal.
     * Note! Dominoes are equal if both sides equal the other sides,
     * independent of side order.
     *
     * @param o the domino to compare this domino two
     * @return true if both the dominoes are equal
     */
    public boolean compareTo(Domino o)
    {
        return o != null &&
               ((val1 == o.getVal1() && val2 == o.getVal2()) ||
                (val1 == o.getVal2() && val2 == o.getVal1()));
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
     * Save this domino instance to a Parcel.
     *
     * @param dest the parcel to write the domino to
     * @param flags additional flags on how to write the parcel (not used)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(val1);
        dest.writeInt(val2);
    }

    /**
     * Compares two dominoes and returns true if they are equal.
     * Will return false if obj is null or is not a Domino.
     *
     * @see #compareTo(Domino)
     * @param obj the domino to compare to
     * @return true if the dominoes are equal
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;

        final Domino other = (Domino) obj;
        return compareTo(other);
    }

    /**
     * Returns the hashcode representation of this domino, based
     * on the domino's total value.
     *
     * @return domino hash code
     */
    @Override
    public int hashCode()
    {
        //multiplied by a small prime.
        return getDominoValue() * 137;
    }

    /**
     * Returns a bitmap representation of a single side of adomino.
     * Bitmap of the domino side dots.
     *
     * @param value the side image to return
     * @param context the parent context
     * @return the bitmap image of the domino side value
     */
    //Load image for domino side value
    public static Bitmap getSide(int value, Context context)
    {
        Bitmap side;

        switch (value)
        {
            case 1:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_one);
                break;
            case 2:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_two);
                break;
            case 3:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_three);
                break;
            case 4:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_four);
                break;
            case 5:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_five);
                break;
            case 6:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_six);
                break;
            case 7:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_seven);
                break;
            case 8:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_eight);
                break;
            case 9:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_nine);
                break;
            case 10:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_ten);
                break;
            case 11:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_eleven);
                break;
            case 12:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_twelve);
                break;
            case 13:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_thirteen);
                break;
            case 14:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_fourteen);
                break;
            case 15:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_fifteen);
                break;
            case 16:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_sixteen);
                break;
            case 17:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_seventeen);
                break;
            case 18:
                side = BitmapFactory.decodeResource(context.getResources(), R.drawable.dom_eighteen);
                break;
            case 0:
            default:
                side = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
                break;
        }
        return side;
    }

    /**
     * Parcel CREATOR for the Domino class.
     *
     * @see android.os.Parcelable.Creator
     */
    public static Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        @Override
        public Domino createFromParcel(Parcel source)
        {
            return new Domino(source);
        }

        @Override
        public Domino[] newArray(int size)
        {
            return new Domino[size];
        }
    };
}
