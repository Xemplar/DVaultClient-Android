package com.xemplarsoft.dv.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xemplarsoft.Vars;
import com.xemplarsoft.dv.Address;
import com.xemplarsoft.dv.MainActivity;
import com.xemplarsoft.dv.R;
import com.xemplarsoft.dv.adapters.AddressAdapter;
import com.xemplarsoft.dv.adapters.AddressClickListener;

import java.util.List;

public class AddressFragment extends Fragment implements AddressClickListener, View.OnClickListener {
    private AddressAdapter adapter;
    private MainActivity main;
    private String lcAddress;

    public TextView total, btc, usd;
    public Button remoteKey;

    public static AddressFragment newInstance(){
        AddressFragment frag = new AddressFragment();

        return frag;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new AddressAdapter(this);
        if(getActivity() instanceof MainActivity){
            this.main = (MainActivity) getActivity();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.content_addresses, container, false);
        RecyclerView recyclerView = layout.findViewById(R.id.addresses);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new Vars.VerticalSpaceItemDecoration(getContext(), 5));
        registerForContextMenu(recyclerView);
        adapter.notifyDataSetChanged();

        this.total = layout.findViewById(R.id.total);
        this.btc = layout.findViewById(R.id.btc);
        this.usd = layout.findViewById(R.id.usd);
        this.remoteKey = layout.findViewById(R.id.remotekey);

        this.remoteKey.setOnClickListener(this);

        return layout;
    }

    public void onClick(View v) {
        if(v.equals(remoteKey)){
            main.newRemoteKey();
        }
    }

    @SuppressLint("SetTextI18n")
    public void onResume() {
        super.onResume();
        if(main != null){
            this.main.setTitle(R.string.screen_addresses);
        }

        this.total.setText(MainActivity.addressManager.getTotalBalance().toPlainString() + " D");
        this.btc.setText(MainActivity.btc.toPlainString() + " \u20BF");
        this.usd.setText("$" + MainActivity.usd.toPlainString());
    }

    public void onAddressClicked(View v, String address) {
        if(main != null) main.onAddressClicked(v, address);
    }

    public boolean onAddressLongClicked(View v, String address) {
        lcAddress = address;
        if(main != null) {
            main.openContextMenu(v);
            return true;
        }
        return false;
    }

    public boolean onContextItemSelected(MenuItem item) {
        AddressAdapter.AddressAdapterMenuInfo info = (AddressAdapter.AddressAdapterMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.mi_rename:
                rename(lcAddress);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_addy, menu);
    }

    public void setAddresses(List<Address> addresses){
        adapter.clear();
        adapter.addAll(addresses);
        adapter.notifyDataSetChanged();
    }

    public void rename(String address){
        Address a = MainActivity.addressManager.getAddress(address);
        if(a == null) return;
        FragmentManager fm = getFragmentManager();
        RenameFragment rename = RenameFragment.newInstance(a.getAddress(), a.getLabel());
        rename.setTargetFragment(this, 300);
        rename.show(fm, "fragment_edit_name");
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
