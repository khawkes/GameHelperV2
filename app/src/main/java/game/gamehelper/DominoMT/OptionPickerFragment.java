package game.gamehelper.DominoMT;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import game.gamehelper.R;

/**
 * Created by Mark Andrews on 3/24/2015.
 */
public class OptionPickerFragment extends DialogFragment{

    public interface OptionPickerListener {
        public void setOption(int option, int caller);
    }

    OptionPickerListener mListener;
    View optionPickerView;
    NumberPicker optionPicker;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OptionPickerListener) activity;
        } catch ( ClassCastException e){
            throw new ClassCastException(getActivity().toString()
                    + " must implement interface OptionPickerListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b = getArguments();
        final int option;

        if(b == null){
            Log.w("OptionPickerFragment", "no arguments found, integer required for picker type");
            dismiss();
        }
        option = b.getInt("option");

        optionPickerView = View.inflate(getActivity(), R.layout.picker_layout, null);

        //retrieve pickers
        optionPicker = (NumberPicker)optionPickerView.findViewById(R.id.new_game_picker);
        optionPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        switch (option){
            case NewGameMT.SET_SELECT_OPTION:
                //Picker for domino set
                optionPicker.setMinValue(0);
                optionPicker.setMaxValue(3);
                optionPicker.setDisplayedValues(new String[]{"Double Nine", "Double Twelve", "Double Fifteen", "Double Eighteen"});
                break;
            case NewGameMT.PLAYER_SELECT_OPTION:
                //Picker for player number
                optionPicker.setMinValue(1);
                optionPicker.setMaxValue(8);
                break;
            case NewGameMT.RULES_SELECT_OPTION:
                //picker for rule type
                optionPicker.setMinValue(0);
                optionPicker.setMaxValue(1);
                optionPicker.setDisplayedValues(new String[]{"Traditional", "Custom"});
                break;
            default:
                Log.w("OptionPickerFragment", "Unknown option type found: " + option);
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(optionPickerView);

        builder.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //return option selected and caller
                mListener.setOption(optionPicker.getValue(), option);
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });
        return builder.create();
    }
}
