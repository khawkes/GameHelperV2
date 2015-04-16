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
 * Created by Mark Andrews on 2/23/2015.
 * Fragment for selecting the end piece to calculate runs
 */
public class LeadOffLetterSelect extends DialogFragment
{

    public interface LeadOffListener
    {
        public void callbackLeadOff(ScrabbleLetter l);
    }

    /**
     * @param DIALOG_SIZE_COMPENSATION adjust for dialog window being smaller than the specified size
     * @param PAGE_MARGIN_PERCENT percent of the screen width to be used for side margins
     * @param PORTRAIT_COLUMNS columns for portrait mode
     * @param LANDSCAPE_COLUMNS = columns for landscape mode
     */
    private static final float DIALOG_SIZE_COMPENSATION = 1.1f;
    private final float PAGE_MARGIN_PERCENT = 0.1f;
    private final int PORTRAIT_COLUMNS = 4;
    private final int LANDSCAPE_COLUMNS = 7;
    int dialogWidth;
    int bitmapSize;
    int numColumns;
    int deckMax;
    LeadOffListener mListener;
    GridView gridView;
    View drawView;
    Display display;
    Point size = new Point();
    BitmapAdapter bitmapAdapter;

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
            mListener = (LeadOffListener) activity;
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
        int marginSize;
        Bundle b = getArguments();

        //retrieve draw_layout view
        drawView = View.inflate(getActivity(), R.layout.end_select_layout, null);

        //get the size of the display and calculate dialog size
        display = getActivity().getWindowManager().getDefaultDisplay();
        display.getSize(size);
        marginSize = (int) (PAGE_MARGIN_PERCENT * size.x * 2);
        dialogWidth = size.x - (marginSize);

        //get columns based on screen orientation
        numColumns = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                PORTRAIT_COLUMNS : LANDSCAPE_COLUMNS);

        //set bitmap size
        bitmapSize = dialogWidth / numColumns;

        //retrieve gridview from layout, set adapter
        gridView = (GridView) drawView.findViewById(R.id.gridView);
        bitmapAdapter = new BitmapAdapter(getActivity(), ScrabbleLetter.scrabIdList, ScrabbleLetter.scrabIdList.length);
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
                mListener.callbackLeadOff(new ScrabbleLetter((char) (position + 'a')));
                dismiss();
            }
        });

        //create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(drawView);
        builder.setTitle(R.string.leadOffLetterSelectTitle);

        return builder.create();
    }
}