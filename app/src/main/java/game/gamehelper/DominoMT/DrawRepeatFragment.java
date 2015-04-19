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

import android.app.AlertDialog;
import android.content.DialogInterface;

import game.gamehelper.R;

/**
 * Fragment class for handling drawing multiple domino operations.
 * Extends DrawFragment to add multiple dominoes.
 *
 * Copied by Jacob Cassagnol on 4/7/2015.
 */
public class DrawRepeatFragment extends DrawFragment
{
    /**
     * Customizes the buttons on the bottom of the dialog screen.  For the DrawRepeatFragment
     * add buttons to and and continue, add and close, and close.  Overwrite not used.
     *
     * @param builder the dialog builder to update the buttons for
     * @param overwrite NOT USED
     */
    protected void customizeDialog(AlertDialog.Builder builder, final Domino overwrite)
    {
        builder.setPositiveButton(R.string.txtDlgAddContinue, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                mListener.onDrawRepeatClose(new Domino(var1, var2));
            }
        })
        .setNegativeButton(R.string.txtDlgAddClose, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                mListener.onDrawClose(null, new Domino(var1, var2));
            }
        })
        .setNeutralButton(R.string.txtDlgCancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //nothing, exit out!
            }
        });
    }
}
