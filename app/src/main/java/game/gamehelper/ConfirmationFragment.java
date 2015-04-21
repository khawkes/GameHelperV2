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

import java.util.HashMap;

/**
 * Created by Mark Andrews on 2/23/2015.
 * Fragment for handling confirmation dialog
 */

public class ConfirmationFragment extends DialogFragment
{
    /**
     * confirm button text argument name
     */
    public static final String ARG_POSITIVE = "positive";

    /**
     * cancel button text argument name
     */
    public static final String ARG_NEGATIVE = "negative";

    /**
     * neutral button text argument name
     */
    public static final String ARG_NEUTRAL = "neutral";

    /**
     * action description text argument name
     */
    public static final String ARG_MAIN_TEXT = "mainText";

    /**
     * dialog fragment argument name
     */
    public static final String ARG_CALL_NAME = "callName";

    /**
     * Dialog fragment listener response.
     */
    public interface ConfirmationListener
    {
        /**
         * Callback for the confirmation fragment positive (ok) response.
         */
        public void onDialogPositiveClick(String tag);

        /**
         * Callback for the confirmation fragment negative (cancel) response.
         */
        public void onDialogNegativeClick(String tag);

        /**
         * Callback for the confirmation fragment neutral response.  Dialog left
         * hand button.
         */
        public void onDialogNeutralClick(String tag);
    }

    /**
     * Handle back to the activity that called this dialog.
     */
    private ConfirmationListener mListener;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle b = getArguments();
        final HashMap<String, String> dlgArgs = new HashMap<>();

        //read dialog text
        dlgArgs.put(ARG_POSITIVE, getString(R.string.txtDlgOK));
        dlgArgs.put(ARG_NEGATIVE, getString(R.string.txtDlgCancel));
        dlgArgs.put(ARG_MAIN_TEXT, "Dialog");
        dlgArgs.put(ARG_CALL_NAME, "Tag");

        if (b != null)
        {
            String val = b.getString(ARG_POSITIVE);
            if (val != null) dlgArgs.put(ARG_POSITIVE, val);

            val = b.getString(ARG_NEGATIVE);
            if (val != null) dlgArgs.put(ARG_NEGATIVE, val);

            val = b.getString(ARG_NEUTRAL);
            if (val != null) dlgArgs.put(ARG_NEUTRAL, val);

            val = b.getString(ARG_MAIN_TEXT);
            if (val != null) dlgArgs.put(ARG_MAIN_TEXT, val);

            val = b.getString(ARG_CALL_NAME);
            if (val != null) dlgArgs.put(ARG_CALL_NAME, val);
        }

        //setup window and handle click
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(dlgArgs.get(ARG_MAIN_TEXT));
        builder.setPositiveButton(dlgArgs.get(ARG_POSITIVE), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                mListener.onDialogPositiveClick(dlgArgs.get(ARG_CALL_NAME));
            }
        });
        builder.setNegativeButton(dlgArgs.get(ARG_NEGATIVE), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                mListener.onDialogNegativeClick(dlgArgs.get(ARG_CALL_NAME));
            }
        });
        if (dlgArgs.containsKey(ARG_NEUTRAL))
        {
            builder.setNeutralButton(dlgArgs.get(ARG_NEUTRAL), new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    mListener.onDialogNeutralClick(dlgArgs.get(ARG_CALL_NAME));
                }
            });
        }

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        try
        {
            mListener = (ConfirmationListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement ConfirmationListener");
        }
    }
}
