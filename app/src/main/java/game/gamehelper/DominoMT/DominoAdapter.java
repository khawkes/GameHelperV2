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

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import game.gamehelper.R;

/**
 * Adapter for displaying the list of domino images.
 *
 * Created by Mark Andrews on 2/14/2015.
 */
public class DominoAdapter extends ArrayAdapter<Domino>
{
    private Context context;
    private Domino[] data;
    private int layoutResourceId;

    public DominoAdapter(Context context, int layoutResourceId, Domino[] data)
    {
        super(context, layoutResourceId, data);
        this.context = context;
        this.data = data;
        this.layoutResourceId = layoutResourceId;
    }

    /**
     * Returns the number of dominoes in the array adapter.
     *
     * @return the number of dominoes in the adapter
     */
    @Override
    public int getCount()
    {
        return data.length;
    }

    /**
     * Update the adapter with a new domino set.
     *
     * @param data the new domino array to display
     */
    public void changeData(Domino[] data)
    {
        clear();
        this.data = data;
        notifyDataSetChanged();
        Log.w("dominoadapter", "DominoAdapter dominoes = " + getCount());
    }

    //Updates view for list

    /**
     * Return an view for an item from the adapter.
     *
     * @param position the item to return
     * @param convertView the inflated view if already constructed
     * @param parent parent view
     * @return return the inflated view for the domino in the adapter
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        DominoHolder holder;

        View row = convertView;
        if (row == null)
        {

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DominoHolder();
            holder.domino = (ImageView) row.findViewById(R.id.domList);
            holder.left = (ImageView) row.findViewById(R.id.dom_list_left);
            holder.right = (ImageView) row.findViewById(R.id.dom_list_right);
            row.setTag(holder);
        }
        else
        {
            holder = (DominoHolder) row.getTag();
        }

        if (data == null)
            return row;

        holder.domino.setImageResource(R.drawable.dom_bg);

        if (data[position].getVal1() != 0)
            holder.left.setImageResource(Domino.domIdList[data[position].getVal1()]);

        if (data[position].getVal2() != 0)
            holder.right.setImageResource(Domino.domIdList[data[position].getVal2()]);
        return row;
    }

    /**
     * Clear all the dominoes from the adapter.
     */
    @Override
    public void clear()
    {
        data = null;
    }

    /**
     * Inner class for holding the domino images.
     */
    private class DominoHolder
    {
        ImageView domino;
        ImageView left;
        ImageView right;
    }
}
