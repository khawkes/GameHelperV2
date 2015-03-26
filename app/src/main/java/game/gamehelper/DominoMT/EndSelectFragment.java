package game.gamehelper.DominoMT;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
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
public class EndSelectFragment extends DialogFragment {

    public interface EndListener {
        public void onClose(int var1);
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
    EndListener mListener;
    GridView gridView;
    View drawView;
    Display display;
    Point size = new Point();
    BitmapAdapter bitmapAdapter;


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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (EndListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement interface EndListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int marginSize;
        Bundle b = getArguments();

        if (b != null)
            deckMax = b.getInt("maxDouble");

        //retrieve draw_layout view
        drawView = View.inflate(getActivity(), R.layout.end_select_layout, null);

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

        final int[] mList = new int[]{
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

        //retrieve gridview from layout, set adapter
        gridView = (GridView) drawView.findViewById(R.id.gridView);
        bitmapAdapter = new BitmapAdapter(getActivity(), mList, deckMax + 1);
        bitmapAdapter.setImageSize(bitmapSize);
        gridView.setAdapter(bitmapAdapter);
        gridView.setNumColumns(numColumns);

        //return the selected tile
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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