package org.pragyan.dalal18.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import org.pragyan.dalal18.R;

public class OTPVerficationDialogFragment extends DialogFragment {

    static OTPVerficationDialogFragment newInstance() {

        OTPVerficationDialogFragment f = new OTPVerficationDialogFragment();
/*
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
*/
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_otp_verification_dialog, container, false);
        return v;
    }
}