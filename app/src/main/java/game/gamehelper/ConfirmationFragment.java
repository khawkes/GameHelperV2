package game.gamehelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import game.gamehelper.R;

/**
 * Created by Mark Andrews on 2/23/2015.
 * Fragment for handling confirmation dialog
 */

public class ConfirmationFragment extends DialogFragment {

    public interface ConfirmationListener {
        public void onDialogPositiveClick(String tag);
    }

    String[] dialogText = new String[4];
    ConfirmationListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b = getArguments();

        /**
         * "positive" = confirm button text
         * "negative" = cancel button text
         * "mainText" = action description text
         * "callName" = fragment tag
         */

        //read dialog text
        if(b != null){
            dialogText[0] = b.getString("positive");
            dialogText[1] = b.getString("negative");
            dialogText[2] = b.getString("mainText");
            dialogText[3] = b.getString("callName");
        }

        //setup window and handle click
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(dialogText[2])
                .setPositiveButton(dialogText[0], new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //return dialog name on positive click
                        mListener.onDialogPositiveClick(dialogText[3]);
                    }
                })
                .setNegativeButton(dialogText[1], new DialogInterface.OnClickListener() {
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
            mListener = (ConfirmationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
            + " must implement ConfirmationListener");
        }
    }
}
