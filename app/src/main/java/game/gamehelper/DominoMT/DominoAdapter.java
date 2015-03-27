package game.gamehelper.DominoMT;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import game.gamehelper.R;

/**
 * Created by Mark Andrews on 2/14/2015.
 * Adapter for image lists
 */
public class DominoAdapter extends ArrayAdapter<Domino> {

    private Context context;
    private Domino[] data;
    int layoutResourceId;

    public DominoAdapter(Context context, int layoutResourceId, Domino[] data){

        super(context, layoutResourceId, data);
        this.context = context;
        this.data = data;
        this.layoutResourceId = layoutResourceId;
    }

    //Updates view for list
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DominoHolder holder = null;
        Domino piece;

        if (row == null) {

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DominoHolder();
            holder.domino = (ImageView) row.findViewById(R.id.domList);
            holder.left = (ImageView) row.findViewById(R.id.dom_list_left);
            holder.right = (ImageView) row.findViewById(R.id.dom_list_right);
            row.setTag(holder);
        }
        else {
            holder = (DominoHolder) row.getTag();
        }

        if(data == null)
            return row;

        holder.domino.setImageResource(R.drawable.dom_bg);

        if(data[position].getVal1() != 0)
            holder.left.setImageResource(Domino.domIdList[data[position].getVal1()]);

        if(data[position].getVal2() != 0)
            holder.right.setImageResource(Domino.domIdList[data[position].getVal2()]);
        return row;
    }

    public void clear(){
        for(int i = 0 ; i < data.length ; i++ )
            data[i] = null;
    }

    private class DominoHolder{
        ImageView domino;
        ImageView left;
        ImageView right;
    }
}
