package com.xemplarsoft.dv.adapters;

import android.view.View;

public interface AddressClickListener {
    public void onAddressClicked(View v, String address);
    public boolean onAddressLongClicked(View v, String address);
}
