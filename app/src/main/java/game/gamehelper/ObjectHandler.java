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

package game.gamehelper;

import android.graphics.Bitmap;
import android.graphics.Point;

import java.util.ArrayList;

/**
 * Created by Mark Andrews on 4/30/2015.
 */
public interface ObjectHandler
{

    public void readObjectList(ArrayList<ArrayList<Point>> objectList, ArrayList<ArrayList<Point>> cornerList);
    //use the objectList and cornerList to classify objects

    public Object getObject();
    //return one classified object

    public Bitmap getFinalShapesImage();
    //return bitmap showing objects that will be returned by getObject()

    public Bitmap getShapesImage();
    //return bitmap showing all objects that have classified as something

    public boolean isEmpty();
    //return whether or not nothing was found
}
