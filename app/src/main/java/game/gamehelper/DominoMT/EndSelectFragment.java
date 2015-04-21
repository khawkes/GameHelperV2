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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;

import game.gamehelper.BitmapAdapter;
import game.gamehelper.R;

/**
 * Fragment for selecting the end piece to calculate runs.

 * Created by Mark Andrews on 2/23/2015.
 */
public class EndSelectFragment extends DialogFragment
{

    public interface EndListener
    {
        public void onClose(int var1);
    }

    /** DIALOG_SIZE_COMPENSATION adjust for dialog window being smaller than the specified size */
    private static final float DIALOG_SIZE_COMPENSATION = 1.1f;

    /** PAGE_MARGIN_PERCENT percent of the screen width to be used for side margins */
    private static final float PAGE_MARGIN_PERCENT = 0.1f;

    /** PORTRAIT_COLUMNS columns for portrait mode */
    private static final int PORTRAIT_COLUMNS = 4;

    /** LANDSCAPE_COLUMNS = columns for landscape mode */
    private static final int LANDSCAPE_COLUMNS = 7;

    int dialogWidth;
    EndListener mListener;

    @Override
    public void onStart()
    {
        super.onStart();

        if (getDialog() == null)
            return;

        //set dialog window width
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (dialogWidth * DIALOG_SIZE_COMPENSATION);
        getDialog().getWindow().setAttributes(params);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (EndListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getActivity().toString()
                    + " must implement interface EndListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle b = getArguments();
        int deckMax = 0;

        if (b != null)
            deckMax = b.getInt("maxDouble");

        //retrieve draw_layout view
        View drawView = View.inflate(getActivity(), R.layout.end_select_layout, null);

        //get the size of the display and calculate dialog size
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int marginSize = (int) (PAGE_MARGIN_PERCENT * size.x * 2);
        dialogWidth = size.x - (marginSize);

        //get columns based on screen orientation
        int numColumns = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                PORTRAIT_COLUMNS : LANDSCAPE_COLUMNS);

        //set bitmap size
        int bitmapSize = dialogWidth / numColumns;


        //retrieve gridview from layout, set adapter
        GridView gridView = (GridView) drawView.findViewById(R.id.gridView);
        BitmapAdapter bitmapAdapter = new BitmapAdapter(getActivity(), Domino.domIdList, deckMax + 1);
        bitmapAdapter.setImageSize(bitmapSize);
        gridView.setAdapter(bitmapAdapter);
        gridView.setNumColumns(numColumns);

        //return the selected tile
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //mark value
                mListener.onClose(position);
                dismiss();
            }
        });

        //create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(drawView);
        builder.setTitle(R.string.endSelectTitle);

        return builder.create();
    }
}