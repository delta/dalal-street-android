package org.pragyan.dalal18.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.pragyan.dalal18.R;
import org.pragyan.dalal18.dagger.ContextModule;
import org.pragyan.dalal18.dagger.DaggerDalalStreetApplicationComponent;
import org.pragyan.dalal18.ui.OtpEditText;

import javax.inject.Inject;

import dalalstreet.api.DalalActionServiceGrpc;
import dalalstreet.api.actions.VerifyOTPRequest;
import dalalstreet.api.actions.VerifyOTPResponse;
import io.grpc.ManagedChannel;

public class OTPVerficationDialogFragment extends DialogFragment {

    @Inject
    ManagedChannel channel;

    String mobNumber;
    EditText phoneNumberEditText;
    Button resendOtp;
    Button verifyOtp;
    OtpEditText otpEditText;

    public static OTPVerficationDialogFragment newInstance(String phNum) {

        OTPVerficationDialogFragment f = new OTPVerficationDialogFragment();

        f.mobNumber = phNum;
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_otp_verification_dialog, container, false);
        phoneNumberEditText = v.findViewById(R.id.enter_otp_mobno_edit_text);
        resendOtp = v.findViewById(R.id.btnResendOtp);
        verifyOtp = v.findViewById(R.id.btnVerifyOtp);
        otpEditText = v.findViewById(R.id.et_otp);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        phoneNumberEditText.setText(mobNumber);

        resendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOtpAgain();
            }
        });
        verifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(otpEditText.getText().toString().equals(""))
                    Toast.makeText(getContext(),"Enter OTP.",Toast.LENGTH_SHORT).show();
                else
                    checkIfOtpIsCorrect(otpEditText.getText().toString());
            }
        });
    }

    private void checkIfOtpIsCorrect(String OTP) {
        VerifyOTPRequest request = VerifyOTPRequest
                .newBuilder()
                .setOtp(Integer.parseInt(OTP))
                .build();

        DalalActionServiceGrpc.DalalActionServiceBlockingStub stub = DalalActionServiceGrpc.newBlockingStub(channel);
        VerifyOTPResponse response = stub.verifyOTP(request);

        if(response.getStatusCode() == VerifyOTPResponse.StatusCode.OK) {
            // go to main
        }
        else {
            Toast.makeText(getContext(),"Wrong OTP.",Toast.LENGTH_SHORT).show();
        }

    }

    private void sendOtpAgain() {
        dismiss();
    }
}