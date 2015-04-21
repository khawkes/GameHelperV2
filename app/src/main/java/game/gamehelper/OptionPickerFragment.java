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

package game.gamehelper;

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

import game.gamehelper.DominoMT.NewGameMT;

/**
 * Android dialog fragment for displaying a spinner selector with the
 * provided values.
 *
 * Created by Mark Andrews on 3/24/2015.
 */
public class OptionPickerFragment extends DialogFragment
{

    /**
     * Listener callback for options selected from this dialog fragment.
     */
    public interface OptionPickerListener
    {
        /**
         * Callback for the options fragment. Used to set dialog choices from the picker.
         *
         * @param option the value from the picker
         * @param caller the option the picker is updating
         */
        public void setOption(int option, int caller);
    }

    private OptionPickerListener mListener;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (OptionPickerListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getActivity().toString()
                    + " must implement interface OptionPickerListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle b = getArguments();
        final int option;

        if (b == null)
        {
            Log.w("OptionPickerFragment", "no arguments found, integer required for picker type");
            dismiss();
            throw new IllegalArgumentException("No arguments found for the OptionPickerFragment, ensure bundle is passed.");
        }
        option = b.getInt("option");

        View optionPickerView = View.inflate(getActivity(), R.layout.picker_layout, null);

        //retrieve pickers
        final NumberPicker optionPicker = (NumberPicker) optionPickerView.findViewById(R.id.new_game_picker);
        optionPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        switch (option)
        {
            case NewGameMT.SET_SELECT_OPTION:
                //Picker for domino set
                optionPicker.setMinValue(0);
                optionPicker.setMaxValue(3);
                optionPicker.setDisplayedValues(new String[] {"Double Nine", "Double Twelve", "Double Fifteen", "Double Eighteen"});
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
                optionPicker.setDisplayedValues(new String[] {"Traditional", "Custom"});
                break;
            default:
                Log.w("OptionPickerFragment", "Unknown option type found: " + option);
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(optionPickerView);

        builder.setPositiveButton(getString(R.string.txtDlgDone), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //return option selected and caller
                mListener.setOption(optionPicker.getValue(), option);
            }
        }).setNegativeButton(getString(R.string.txtDlgCancel), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //do nothing
            }
        });
        return builder.create();
    }
}
