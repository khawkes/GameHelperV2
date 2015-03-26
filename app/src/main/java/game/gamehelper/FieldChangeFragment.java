package game.gamehelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.widget.EditText;

import game.gamehelper.R;
import game.gamehelper.ScoreBoard;

/**
 * Created by Mark Andrews on 3/8/2015.
 * Fragment for changing text or scores in the ScoreBoard
 */
public class FieldChangeFragment extends DialogFragment {

    public interface FieldChangeListener {
        public void onDialogPositiveClick(String s, int fieldType);
    }

    private FieldChangeListener mListener;
    private String fieldText;
    private int fieldType;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final EditText name = new EditText(getActivity());
        String title;

        //retrieve field type and data
        Bundle args = getArguments();
        if( args != null ){
            fieldText = args.getString("field");
            fieldType = args.getInt("fieldType");
        }

        name.setText(fieldText);
        name.setSelectAllOnFocus(true);

        //restrict use to numbers for score fields
        if(fieldType == ScoreBoard.DATA_FIELD){
           name.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        //create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle( fieldType == ScoreBoard.PLAYER_FIELD ? R.string.nameChange : R.string.dataChange )
                .setView(name)
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(name.getText().toString(), fieldType);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do nothing

                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (FieldChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
            + " must implement NameChangeListener");
        }
    }
}
