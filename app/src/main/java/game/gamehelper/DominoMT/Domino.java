package game.gamehelper.DominoMT;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import game.gamehelper.R;
/**
 * Author History:
 * Jacob
 * Mark
 * Jacob
 */

/**
 * Created by Jacob on 2/11/2015.
 * A domino: defined as value 1, value 2, and sum of values.
 */
public class Domino implements Parcelable {
    private final int val1;
    private final int val2;
    private final int sum;

    @Override
    //required for Parcelable
    public int describeContents() {
        return 0;
    }

    /**
     * Constructor. Creates the Domino.
     * @param value1 Pair value 1.
     * @param value2 Pair value 2.
     */
    public Domino(int value1, int value2) {
        val1 = value1;
        val2 = value2;
        sum = getVal1() + getVal2();

    }

    public Domino(Parcel p){
        val1 = p.readInt();
        val2 = p.readInt();
        sum = val1 + val2;
    }

    public int getVal1() {
        return val1;
    }

    public int getVal2() {
        return val2;
    }

    //returns the oppositve value than the one given on this domino.
    //Assumes that we give it one of the sides on this domino.
    public int getOtherVal(int val) { return (val == val1) ? val2 : val1; }

    public int getSum() {
        return sum;
    }

    //Allows comparison between other dominoes
    public boolean compareTo(Domino a) {
        if( a == null )
            return false;
        if (val1 == a.getVal1() && val2 == a.getVal2())
            return true;
        if (val1 == a.getVal2() && val2 == a.getVal1())
            return true;
        return false;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(val1);
        dest.writeInt(val2);
    }

    @Override
    //So we can search for this in an arrayList.
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj.getClass() != getClass())
            return false;

        final Domino other = (Domino) obj;

        if (this.compareTo(other))
            return true;
        return false;
    }

    @Override
    //Because you always have to override both equals and hashcode if you overide one.
    public int hashCode() {
        //multiplied by a small prime.
        return getSum() * 137;
    }


    //Load image for domino side value
    public static Bitmap getSide(int value, Context context){

        Bitmap side;

        switch(value){
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
                side = Bitmap.createBitmap(200,200,Bitmap.Config.ARGB_8888);
                break;
        }
        return side;
    }

    public static Parcelable.Creator CREATOR = new Parcelable.Creator(){
        @Override
        public Domino createFromParcel(Parcel source) {
            return new Domino(source);
        }

        @Override
        public Domino[] newArray(int size) {
            return new Domino[size];
        }
    };


}
