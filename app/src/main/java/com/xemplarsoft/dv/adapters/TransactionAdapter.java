package com.xemplarsoft.dv.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xemplarsoft.dv.R;
import com.xemplarsoft.dv.adapters.TransactionAdapter.TransactionViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionViewHolder>{
    protected List<String> transactions;
    protected TransactionClickListener tcl;
    protected Fragment f;

    public TransactionAdapter(Fragment f){
        this.f = f;
        transactions = new ArrayList<>();
    }

    public TransactionAdapter(Fragment f, String[] objects){
        this.f = f;
        transactions = new ArrayList<>();
        for(String s : objects){
            if(s.equals("")) continue;
            if(s.equals("null")) continue;
            this.transactions.add(s);
        }
    }

    public void setTcl(TransactionClickListener tcl){
        this.tcl = tcl;
    }

    public TransactionViewHolder onCreateViewHolder(ViewGroup vg, int i) {
        Context context = vg.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.item_txid, vg, false);
        return new TransactionViewHolder(root);
    }

    public void onBindViewHolder(TransactionViewHolder vh, int i) {
        TextView txid = vh.txid;
        Button copy = vh.copy;
        Button explorer = vh.explorer;

        if(transactions.size() != 0) {
            String tx = transactions.get(i);

            copy.setVisibility(View.VISIBLE);
            explorer.setVisibility(View.VISIBLE);
            txid.setText(tx);
        } else {
            copy.setVisibility(View.GONE);
            explorer.setVisibility(View.GONE);
            txid.setText(R.string.no_transactions);
        }
    }

    public int getItemCount() {
        if(transactions.size() == 0){
            return 1;
        } else {
            return transactions.size();
        }
    }

    public void add(String tx){
        this.transactions.add(tx);
        this.notifyDataSetChanged();
    }

    public void remove(String tx){
        this.transactions.add(tx);
        this.notifyDataSetChanged();
    }

    public void addAll(Collection<? extends String> list){
        this.transactions.addAll(list);
        this.notifyDataSetChanged();
    }

    public void removeAll(Collection<? extends String> list){
        this.transactions.removeAll(list);
        this.notifyDataSetChanged();
    }

    public void clear(){
        this.transactions.clear();
        this.notifyDataSetChanged();
    }

    public String get(int pos){
        return transactions.get(pos);
    }

    public void setTransaction(String tx, int pos){
        transactions.set(pos, tx);
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView txid;
        public String addy;
        public Button copy, explorer;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            txid = itemView.findViewById(R.id.txid);
            copy = itemView.findViewById(R.id.copy);
            explorer = itemView.findViewById(R.id.explorer);
            copy.setOnClickListener(this);
            explorer.setOnClickListener(this);
        }

        public void onClick(View v) {
            Context con = v.getContext();
            if(v.equals(copy)) {
                ClipboardManager manager = (ClipboardManager) con.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("txid", txid.getText());
                manager.setPrimaryClip(clip);

                Toast.makeText(con, R.string.tx_copied, Toast.LENGTH_SHORT).show();
            } else {
                Uri uri = Uri.parse("https://chainz.cryptoid.info/d/tx.dws?" + txid.getText());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                f.startActivity(intent);
            }

            if (tcl != null) tcl.onTransactionClicked(v, addy);
        }
    }

    public static class TransactionAdapterMenuInfo implements ContextMenu.ContextMenuInfo {
        public TransactionAdapterMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }

        final public int position;
        final public long id;
    }
}
