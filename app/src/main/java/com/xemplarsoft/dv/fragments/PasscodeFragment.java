package com.xemplarsoft.dv.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.xemplarsoft.dv.R;

import java.math.BigDecimal;

public class PasscodeFragment extends Fragment implements View.OnClickListener {

    public void setArguments(Bundle args) {
        super.setArguments(args);

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.content_balance, container, false);

        return layout;
    }

    public void onClick(View v) {

    }

    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
    }

    public void update(String address, BigDecimal bal){}

    public void onPause() {
        super.onPause();
    }
}
