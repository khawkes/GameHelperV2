package game.gamehelper.DominoMT;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import game.gamehelper.R;

/**
 * Created by Mark Andrews on 3/20/2015.
 */
public class NewGameMT extends DialogFragment{
    public static final int SET_SELECT_OPTION = 0;
    public static final int PLAYER_SELECT_OPTION = 1;
    public static final int RULES_SELECT_OPTION = 2;

    TextView set;
    TextView player;
    TextView rules;

    int setOption = 0;
    int playerOption = 0;
    int rulesOption = 0;
    final Bundle bundle = new Bundle();

    NewGameListener mListener;
    View newGameView;

    public interface NewGameListener{
        public void onNewGameCreate(int set, int player, int rules);
        public void onNewGameCancel();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //retrieve draw_layout view
        newGameView = View.inflate(getActivity(), R.layout.new_game_layout, null);

        //get option text
        set = (TextView)newGameView.findViewById(R.id.max_double_picker);
        player = (TextView)newGameView.findViewById(R.id.player_picker);
        rules = (TextView)newGameView.findViewById(R.id.rules_picker);

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putInt("option", NewGameMT.SET_SELECT_OPTION);
                DialogFragment fragment = new OptionPickerFragment();
                fragment.setArguments(bundle);
                fragment.show(getActivity().getSupportFragmentManager(), "optionPicker");
            }
        });


        player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putInt("option", NewGameMT.PLAYER_SELECT_OPTION);
                DialogFragment fragment = new OptionPickerFragment();
                fragment.setArguments(bundle);
                fragment.show(getActivity().getSupportFragmentManager(), "optionPicker");
            }
        });


        rules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putInt("option", NewGameMT.RULES_SELECT_OPTION);
                DialogFragment fragment = new OptionPickerFragment();
                fragment.setArguments(bundle);
                fragment.show(getActivity().getSupportFragmentManager(), "optionPicker");
            }
        });

        //create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(newGameView);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Add domino to hand
                mListener.onNewGameCreate(setOption, playerOption, rulesOption);

            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onNewGameCancel();

                    }
                });
        return builder.create();

    }

    public void setOption(int option, int caller) {
        String[] set = new String[]{"Double Nine", "Double Twelve", "Double Fifteen", "Double Eighteen"};
        String[] rules = new String[]{"Traditional", "Custom"};

        //change option selected
        switch (caller){
            case SET_SELECT_OPTION:
                this.set.setText(set[option]);
                setOption = option;
                break;
            case PLAYER_SELECT_OPTION:
                this.player.setText("" + option);
                playerOption = option+1;
                break;
            case RULES_SELECT_OPTION:
                this.rules.setText(rules[option]);
                rulesOption = option;
                break;
            default:
                break;
        }
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