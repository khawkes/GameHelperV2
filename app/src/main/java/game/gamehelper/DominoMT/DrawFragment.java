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
import android.content.DialogInterface;
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
import android.widget.ImageView;

import game.gamehelper.BitmapAdapter;
import game.gamehelper.R;

/**
 * Fragment class for handling drawing domino operations
 *
 * Created by Mark Andrews on 2/23/2015.
 */
// TODO: make parent class to minimize duplicate code in DrawFragment and EndSelectFragment
public class DrawFragment extends DialogFragment
{
    /** DIALOG_SIZE_COMPENSATION adjust for dialog window being smaller than the specified size */
    private static final float DIALOG_SIZE_COMPENSATION = 1.1f;

    /** PAGE_MARGIN_PERCENT percent of the screen width to be used for side margins */
    private static final float PAGE_MARGIN_PERCENT = 0.1f;

    /** PORTRAIT_COLUMNS columns for portrait mode */
    private static final int PORTRAIT_COLUMNS = 4;

    /** LANDSCAPE_COLUMNS = columns for landscape mode */
    private static final int LANDSCAPE_COLUMNS = 7;

    private int dialogWidth;
    private int deckMax;

    private ImageView leftSide;
    private ImageView leftSidePtr;
    private ImageView rightSide;
    private ImageView rightSidePtr;
    protected DrawDominoListener mListener;

    protected int var1 = 0;
    protected int var2 = 0;
    private int currentSide = 0;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (DrawDominoListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getActivity().toString()
                    + " must implement interface DrawDominoListener");
        }
    }

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle arguments = getArguments();
        Domino overwrite = null;
        if (arguments != null)
        {
            deckMax = arguments.getInt("maxDouble");
            overwrite = arguments.getParcelable("overwrite");
        }

        //retrieve draw_layout view
        View drawView = View.inflate(getActivity(), R.layout.draw_layout, null);

        //get the size of the display and calculate dialog size
        Point size = new Point();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        display.getSize(size);
        int marginSize = (int) (PAGE_MARGIN_PERCENT * size.x * 2);
        dialogWidth = size.x - (marginSize);

        //get columns based on screen orientation
        Configuration configuration = getResources().getConfiguration();
        int numColumns =
                (configuration.orientation == Configuration.ORIENTATION_PORTRAIT ?
                    PORTRAIT_COLUMNS : LANDSCAPE_COLUMNS);

        //set bitmap size
        int bitmapSize = dialogWidth / numColumns;

        //get imageview from top left of layout and place the domino background
        ImageView imgDominoBackground = (ImageView) drawView.findViewById(R.id.imageViewBG);
        imgDominoBackground.setImageResource(R.drawable.dom_bg);

        //get sides
        leftSide = (ImageView) drawView.findViewById(R.id.leftSide);
        rightSide = (ImageView) drawView.findViewById(R.id.rightSide);

        leftSidePtr = (ImageView) drawView.findViewById(R.id.leftSidePtr);
        leftSidePtr.setImageResource(android.R.drawable.arrow_down_float);
        rightSidePtr = (ImageView) drawView.findViewById(R.id.rightSidePtr);
        rightSidePtr.setImageResource(android.R.drawable.arrow_down_float);
        rightSidePtr.setVisibility(View.INVISIBLE);

        Clicker clickListener = new Clicker();
        leftSide.setOnClickListener(clickListener);
        rightSide.setOnClickListener(clickListener);

        //retrieve gridview from layout, set adapter
        GridView gridView = (GridView) drawView.findViewById(R.id.gridView);
        BitmapAdapter bitmapAdapter = new BitmapAdapter(getActivity(), Domino.domIdList, deckMax + 1);
        bitmapAdapter.setImageSize(bitmapSize);
        gridView.setAdapter(bitmapAdapter);
        gridView.setNumColumns(numColumns);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //mark piece, toggle side of preview domino
                switch (currentSide)
                {
                    default:
                    case 0:
                        var1 = position;
                        leftSide.setImageBitmap(Domino.getSide(position, getActivity().getApplicationContext()));
                        setSidePointerImage(false);
                        break;
                    case 1:
                        var2 = position;
                        rightSide.setImageBitmap(Domino.getSide(position, getActivity().getApplicationContext()));
                        setSidePointerImage(true);
                }

                currentSide ^= 1;
            }
        });

        //create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(drawView);
        customizeDialog(builder, overwrite);

        return builder.create();
    }

    /**
     * Customizes the buttons on the bottom of the dialog screen.  Overwrite, if present,
     * is the existing domino to update.
     *
     * @param builder the dialog builder to update the buttons for
     * @param overwrite if set, the domino that will be replaced by this draw
     */
    protected void customizeDialog(AlertDialog.Builder builder, final Domino overwrite)
    {
        int posResourceString = R.string.txtDlgAdd;
        if (overwrite != null) posResourceString = R.string.txtDlgUpdate;

        int negResourceString = R.string.txtDlgCancel;
        builder.setPositiveButton(posResourceString, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                mListener.onDrawClose(overwrite, new Domino(var1, var2));
            }
        })
        .setNegativeButton(negResourceString, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //nothing, exit out!
            }
        });

        if (overwrite != null)
        {
            var1 = overwrite.getVal1();
            leftSide.setImageBitmap(Domino.getSide(var1, getActivity().getApplicationContext()));

            var2 = overwrite.getVal2();
            rightSide.setImageBitmap(Domino.getSide(var2, getActivity().getApplicationContext()));
        }
    }

    /**
     * Updates the pointer triangle over the top the domino side that will
     * be updated if a value is selected from the top of the dialog.
     *
     * @param left if true, the pointer indicates the left hand side of the
     *             domino, if false, the right hand side.
     */
    private void setSidePointerImage(boolean left)
    {
        int visibleLeft = left ? View.VISIBLE : View.INVISIBLE;
        int visibleRight = left ? View.INVISIBLE : View.VISIBLE;

        leftSidePtr.setVisibility(visibleLeft);
        rightSidePtr.setVisibility(visibleRight);
    }

    /**
     * Setup a click listener to allow the user to override the domino side
     * that will be updated.
     */
    public class Clicker implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            if (v == leftSide)
            {
                var1 = 0;
                leftSide.setImageDrawable(null);
                currentSide = 0;
                setSidePointerImage(true);
            }
            else
            {
                var2 = 0;
                rightSide.setImageDrawable(null);
                currentSide = 1;
                setSidePointerImage(false);
            }
        }
    }
}
