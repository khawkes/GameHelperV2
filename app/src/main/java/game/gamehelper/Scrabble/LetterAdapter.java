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

package game.gamehelper.Scrabble;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import game.gamehelper.DominoMT.Domino;
import game.gamehelper.R;

/**
 * Created by Mark Andrews on 2/14/2015.
 * Adapter for image lists
 */
public class LetterAdapter extends ArrayAdapter<ScrabbleLetter>
{
    private Context context;
    private ScrabbleLetter[] data;
    int layoutResourceId;

    public LetterAdapter(Context context, int layoutResourceId, ScrabbleLetter[] data)
    {
        super(context, layoutResourceId, data);
        this.context = context;
        this.data = data;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public int getCount()
    {
        return data.length;
    }

    public void changeData(ScrabbleLetter[] data)
    {
        clear();
        this.data = data;
        notifyDataSetChanged();
        Log.w("scrabbleadaptor", "ScrabbleAdaptor letters = " + getCount());
    }

    //Updates view for list
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        LetterHolder holder = null;
        Domino piece;

        if (row == null)
        {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new LetterHolder();
            holder.letter = (ImageView) row.findViewById(R.id.domList);
            row.setTag(holder);
        }
        else
        {
            holder = (LetterHolder) row.getTag();
        }

        if (data == null)
            return row;

        if (data[position].getChar() != ' ')
            holder.letter.setImageResource(ScrabbleLetter.scrabIdList[data[position].getChar()-'a']);
        else
            holder.letter.setImageResource(ScrabbleLetter.scrabIdList[0]);

        return row;
    }

    public void clear()
    {
        for (int i = 0; i < data.length; i++)
        {
            data[i] = null;
        }
    }

    private class LetterHolder
    {
        ImageView letter;
    }
}
