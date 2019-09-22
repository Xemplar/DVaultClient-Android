package com.xemplarsoft.dv.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blikoon.qrcodescanner.QrCodeActivity;
import com.xemplarsoft.dv.MainActivity;
import com.xemplarsoft.dv.R;
import com.xemplarsoft.dv.views.DecimalPicker;

import java.math.BigDecimal;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static com.xemplarsoft.Vars.REQUEST_CODE_QR_SCAN;

public class PayFragment extends Fragment implements View.OnClickListener {
    protected String address, label, val_address;
    protected BigDecimal balance;

    protected TextView addr, name, bal;
    protected Button pay, scanqr;

    protected EditText payTo, narration;
    protected DecimalPicker value;
    private MainActivity main;

    public static PayFragment newInstance(){
        PayFragment pay = new PayFragment();
        return pay;
    }

    public void setArguments(Bundle args) {
        super.setArguments(args);
        this.address = args.getString("address");
        this.label = args.getString("label");
        this.balance = new BigDecimal(args.getString("balance"));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActivity() instanceof MainActivity){
            this.main = (MainActivity) getActivity();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.content_pay, container, false);

        this.addr = layout.findViewById(R.id.address);
        this.name = layout.findViewById(R.id.label);
        this.bal = layout.findViewById(R.id.balance);

        this.payTo = layout.findViewById(R.id.pay_to);
        this.narration = layout.findViewById(R.id.input_narration);
        this.value = layout.findViewById(R.id.amount);

        this.scanqr = layout.findViewById(R.id.scanqr);
        this.pay = layout.findViewById(R.id.btn_pay);

        this.scanqr.setOnClickListener(this);
        this.pay.setOnClickListener(this);

        this.value.init(balance, new BigDecimal(0));

        return layout;
    }

    public void onClick(View v) {
        if(v.equals(scanqr)){
            scanQR();
        } else if(v.equals(pay)){
            main.makePayment(payTo.getText().toString(), value.getNumber(), narration.getText().toString(), address);
            getFragmentManager().popBackStackImmediate();
        }
    }

    public void scanQR(){
        if (ContextCompat.checkSelfPermission(main, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 0x01);
        } else {
            Intent i = new Intent(main, QrCodeActivity.class);
            startActivityForResult(i, REQUEST_CODE_QR_SCAN);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0x01: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(main, QrCodeActivity.class);
                    startActivityForResult(i, REQUEST_CODE_QR_SCAN);
                } else {
                    Toast.makeText(main, R.string.perm_camera, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                Log.d(TAG, String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
        }
        System.out.println("Request code: " + (requestCode == REQUEST_CODE_QR_SCAN));
        System.out.println("Result code: " + (resultCode == RESULT_OK));
        if(requestCode == REQUEST_CODE_QR_SCAN){
            if(resultCode == RESULT_OK) {
                String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                populate(result, "Not a proper QR Code");
                payTo.setText(val_address);
            }
        }
    }

    private boolean populate(String scannedData, String failToast){
        boolean success = false;
        String[] dat = scannedData.split(":");
        if(dat.length == 1 && !dat[0].equals("")){
            val_address = dat[0];
            success = true;
        } else if(dat.length < 2){
            toast(failToast);
            success = false;
        }
        return success;
    }

    public void onStart() {
        super.onStart();
        this.addr.setText(address);
        this.name.setText(label);
        this.bal.setText(balance.toPlainString() + " D");
    }

    public void onResume() {
        super.onResume();
        if(main != null){
            this.main.setTitle(R.string.screen_pay);
        }
    }

    public void toast(String message){
        Toast.makeText(main, message, Toast.LENGTH_SHORT).show();
    }

    public void update(String address, BigDecimal bal){}

    public void onPause() {
        super.onPause();
    }
}
