package game.gamehelper.DominoMT;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import game.gamehelper.BitmapAdapter;
import game.gamehelper.R;

/**
 * Created by Mark Andrews on 2/23/2015.
 * Fragment class for handling draw operations
 *
 * TODO make parent class to minimize duplicate code in DrawFragment and EndSelectFragment
 */
public class DrawFragment extends DialogFragment {

    public interface DrawListener {
        public void onClose(int var1, int var2);
    }

    /** @param DIALOG_SIZE_COMPENSATION adjust for dialog window being smaller than the specified size
     *  @param PAGE_MARGIN_PERCENT percent of the screen width to be used for side margins
     *  @param PORTRAIT_COLUMNS columns for portrait mode
     *  @param LANDSCAPE_COLUMNS = columns for landscape mode
     */
    private static final float DIALOG_SIZE_COMPENSATION = 1.1f;
    private final float PAGE_MARGIN_PERCENT = 0.1f;
    private final int PORTRAIT_COLUMNS = 4;
    private final int LANDSCAPE_COLUMNS = 7;
    int dialogWidth;
    int bitmapSize;
    int numColumns;
    int deckMax;
    DrawListener mListener;
    GridView gridView;
    View drawView;
    ImageView imageView;
    ImageView leftSide;
    ImageView rightSide;
    BitmapAdapter bitmapAdapter;
    int width = 0;
    int height = 0;
    int var1 = 0;
    int var2 = 0;
    int currentSide = 0;
    Display display;
    Point size = new Point();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mListener = (DrawListener) activity;
        }catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
            + " must implement interface DrawListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null)
            return;

        //set dialog window width
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (dialogWidth*DIALOG_SIZE_COMPENSATION);
        getDialog().getWindow().setAttributes(params);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int marginSize;
        Bundle b = getArguments();
        Clicker clickListener = new Clicker();

        if (b != null)
            deckMax = b.getInt("maxDouble");

        //retrieve draw_layout view
        drawView = View.inflate(getActivity(), R.layout.draw_layout, null);

        //get the size of the display and calculate dialog size
        display = getActivity().getWindowManager().getDefaultDisplay();
        display.getSize(size);
        marginSize = (int) (PAGE_MARGIN_PERCENT*size.x*2);
        dialogWidth = size.x - (marginSize);

        //get columns based on screen orientation
        numColumns = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                PORTRAIT_COLUMNS : LANDSCAPE_COLUMNS);

        //set bitmap size
        bitmapSize = dialogWidth / numColumns;

        int[] mList = new int[] {
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

        //get imageview from top left of layout and place the domino background
        imageView = (ImageView)drawView.findViewById(R.id.imageViewBG);
        imageView.setImageResource(R.drawable.dom_bg);

        //get sides
        leftSide = (ImageView)drawView.findViewById(R.id.leftSide);
        rightSide = (ImageView)drawView.findViewById(R.id.rightSide);

        leftSide.setOnClickListener(clickListener);
        rightSide.setOnClickListener(clickListener);

        //retrieve gridview from layout, set adapter
        gridView = (GridView)drawView.findViewById(R.id.gridView);
        bitmapAdapter = new BitmapAdapter(getActivity(), mList, deckMax);
        bitmapAdapter.setImageSize(bitmapSize);
        gridView.setAdapter(bitmapAdapter);
        gridView.setNumColumns(numColumns);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //mark piece, toggle side of preview domino
                position++;

                switch(currentSide) {
                    default:
                    case 0:
                        var1 = position;
                        leftSide.setImageBitmap(Domino.getSide(position, getActivity().getApplicationContext()));
                        break;
                    case 1:
                        var2 = position;
                        rightSide.setImageBitmap(Domino.getSide(position, getActivity().getApplicationContext()));
                }

                currentSide ^= 1;
            }
        });

        //create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(drawView);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Add domino to hand
                mListener.onClose(var1,var2);

            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close window

                    }
                });

        return builder.create();
    }

    public class Clicker implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if(v == leftSide ) {
                var1 = 0;
                leftSide.setImageDrawable(null);
                currentSide = 0;
            }
            else {
                var2 = 0;
                rightSide.setImageDrawable(null);
                currentSide = 1;
            }
        }
    }
}
