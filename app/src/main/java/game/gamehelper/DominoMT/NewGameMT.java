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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import game.gamehelper.OptionPickerFragment;
import game.gamehelper.R;

/**
 * Dialog fragment for prompting the user for the new game parameters.
 *
 * Created by Mark Andrews on 3/20/2015.
 */
public class NewGameMT extends DialogFragment
{
    public static final int SET_SELECT_OPTION = 0;
    public static final int PLAYER_SELECT_OPTION = 1;
    public static final int RULES_SELECT_OPTION = 2;

    private TextView set;
    private TextView player;
    private TextView rules;

    private int setOption = 0;
    private int playerOption = 1;
    private int rulesOption = 0;
    private final Bundle bundle = new Bundle();

    private NewGameListener mListener;

    public interface NewGameListener
    {
        public void onNewGameCreate(int set, int player, int rules);

        public void onNewGameCancel();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        //retrieve draw_layout view
        View newGameView = View.inflate(getActivity(), R.layout.new_game_layout, null);

        //get option text
        set = (TextView) newGameView.findViewById(R.id.max_double_picker);
        player = (TextView) newGameView.findViewById(R.id.player_picker);
        rules = (TextView) newGameView.findViewById(R.id.rules_picker);

        set.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bundle.putInt("option", NewGameMT.SET_SELECT_OPTION);
                DialogFragment fragment = new OptionPickerFragment();
                fragment.setArguments(bundle);
                fragment.show(getActivity().getSupportFragmentManager(), "optionPicker");
            }
        });

        player.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bundle.putInt("option", NewGameMT.PLAYER_SELECT_OPTION);
                DialogFragment fragment = new OptionPickerFragment();
                fragment.setArguments(bundle);
                fragment.show(getActivity().getSupportFragmentManager(), "optionPicker");
            }
        });

        rules.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bundle.putInt("option", NewGameMT.RULES_SELECT_OPTION);
                DialogFragment fragment = new OptionPickerFragment();
                fragment.setArguments(bundle);
                fragment.show(getActivity().getSupportFragmentManager(), "optionPicker");
            }
        });

        //create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(newGameView);

        builder.setPositiveButton(R.string.txtDlgYes, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //Add domino to hand
                mListener.onNewGameCreate(setOption, playerOption, rulesOption);

            }
        })
        .setNegativeButton(R.string.txtDlgCancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                mListener.onNewGameCancel();

            }
        });
        return builder.create();

    }

    /**
     * Callback for the options fragment. Used to set dialog choices from the picker.
     *
     * @param option the value from the picker
     * @param caller the option the picker is updating
     */
    public void setOption(int option, int caller)
    {
        String[] set = new String[] {"Double Nine", "Double Twelve", "Double Fifteen", "Double Eighteen"};
        String[] rules = new String[] {"Traditional", "Custom"};

        //change option selected
        switch (caller)
        {
            case SET_SELECT_OPTION:
                this.set.setText(set[option]);
                setOption = option;
                break;
            case PLAYER_SELECT_OPTION:
                this.player.setText("" + option);
                playerOption = option;
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
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (NewGameListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(getActivity().toString()
                    + " must implement interface NewGameListener");
        }
    }
}