package com.xemplarsoft.dv.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xemplarsoft.dv.Address;
import com.xemplarsoft.dv.R;
import com.xemplarsoft.dv.adapters.AddressAdapter.AddressViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressViewHolder>{
    protected List<Address> addresses;
    protected AddressClickListener acl;

    public AddressAdapter(AddressClickListener acl){
        addresses = new ArrayList<>();
        this.acl = acl;
    }

    public AddressAdapter(List<Address> objects, AddressClickListener acl){
        this.addresses = objects;
        this.acl = acl;
    }

    public AddressViewHolder onCreateViewHolder(ViewGroup vg, int i) {
        Context context = vg.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.item_address, vg, false);
        return new AddressViewHolder(root);
    }

    public void onBindViewHolder(AddressViewHolder vh, int i) {
        TextView address = vh.address;
        TextView balance = vh.balance;
        TextView label = vh.label;

        if(addresses.size() != 0) {
            Address a = addresses.get(i);
            vh.addy = a.getAddress();

            balance.setVisibility(View.VISIBLE);
            label.setVisibility(View.VISIBLE);

            address.setText(a.getAddress());
            label.setText(a.getLabel());
            balance.setText(a.getBalance().toPlainString() + " D");
        } else {
            balance.setVisibility(View.GONE);
            label.setVisibility(View.GONE);
            address.setText(R.string.no_addresses);
        }
    }

    public int getItemCount() {
        if(addresses.size() == 0){
            return 1;
        } else {
            return addresses.size();
        }
    }

    public void add(Address a){
        this.addresses.add(a);
        this.notifyDataSetChanged();
    }

    public void remove(Address a){
        this.addresses.add(a);
        this.notifyDataSetChanged();
    }

    public void addAll(Collection<? extends Address> list){
        this.addresses.addAll(list);
        this.notifyDataSetChanged();
    }

    public void removeAll(Collection<? extends Address> list){
        this.addresses.removeAll(list);
        this.notifyDataSetChanged();
    }

    public void clear(){
        this.addresses.clear();
        this.notifyDataSetChanged();
    }

    public Address get(int pos){
        return addresses.get(pos);
    }

    public void setAddress(Address a, int pos){
        addresses.set(pos, a);
    }

    public class AddressViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView address, balance, label;
        public String addy;

        public AddressViewHolder(View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.address);
            balance = itemView.findViewById(R.id.balance);
            label = itemView.findViewById(R.id.label);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void onClick(View v) {
            acl.onAddressClicked(v, addy);
        }

        public boolean onLongClick(View v) {
            return acl.onAddressLongClicked(v, addy);
        }
    }

    public static class AddressAdapterMenuInfo implements ContextMenu.ContextMenuInfo {
        public AddressAdapterMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }

        final public int position;
        final public long id;
    }
}
