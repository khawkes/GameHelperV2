package game.gamehelper.DominoMT;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import game.gamehelper.R;

/**
 * Created by Mark Andrews on 3/20/2015.
 */
public class NewGameMT extends DialogFragment {
    NewGameListener mListener;
    View newGameView;
    NumberPicker dominoSet;
    NumberPicker player;
    NumberPicker rules;

    public interface NewGameListener{
        public void onNewGameCreate(int set, int player, int rules);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //retrieve draw_layout view
        newGameView = View.inflate(getActivity(), R.layout.new_game_layout, null);

        //retrieve pickers
        dominoSet = (NumberPicker)newGameView.findViewById(R.id.max_double_picker);
        player = (NumberPicker)newGameView.findViewById(R.id.player_picker);
        rules = (NumberPicker)newGameView.findViewById(R.id.rules_picker);

        dominoSet.setMinValue(0);
        dominoSet.setMaxValue(3);
        dominoSet.setDisplayedValues( new String[] {"Double Nine", "Double Twelve", "Double Fifteen", "Double Eighteen"});

        player.setMinValue(1);
        player.setMaxValue(8);

        rules.setMinValue(0);
        rules.setMaxValue(1);
        rules.setDisplayedValues( new String[] {"Traditional", "Custom"});


        //create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(newGameView);

        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Add domino to hand
                mListener.onNewGameCreate(dominoSet.getValue(), player.getValue(), rules.getValue());

            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close window

                    }
                });


        return builder.create();


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NewGameListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement interface NewGameListener");
        }
    }
}
