package com.rushlimit.doodlz;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by aminm on 5/4/16.
 */
public class EraseImageDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.message_erase);
        builder.setPositiveButton(R.string.button_erase, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getDoodleFragment().getDoodleView().clear();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MainActivityFragment fragment = getDoodleFragment();

        if (fragment != null) {
            fragment.setDialogOnScreen(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivityFragment fragment = getDoodleFragment();

        if (fragment != null) {
            fragment.setDialogOnScreen(false);
        }
    }

    private MainActivityFragment getDoodleFragment() {
        return (MainActivityFragment) getFragmentManager().findFragmentById(R.id.doodleFragment);
    }
}
