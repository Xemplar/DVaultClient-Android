package com.xemplarsoft.dv.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xemplarsoft.Vars;
import com.xemplarsoft.dv.MainActivity;
import com.xemplarsoft.dv.R;
import com.xemplarsoft.dv.adapters.TransactionAdapter;
import com.xemplarsoft.dv.anim.ResizeAnimation;

import net.glxn.qrgen.android.QRCode;

import java.math.BigDecimal;

public class BalanceFragment extends Fragment implements View.OnClickListener {
    protected String address, label;
    protected String[] txs;
    protected BigDecimal balance;
    private TransactionAdapter adapter;
    private ImageView qr;
    private TextView bal;
    private TextView addr, name;
    private ViewGroup root;
    private Button showqr, copy_addr, send;

    private MainActivity main;
    private int showQRWidth;

    public void setArguments(Bundle args) {
        super.setArguments(args);
        this.address = args.getString("address");
        this.label = args.getString("label");
        this.balance = new BigDecimal(args.getString("balance"));
        this.txs = args.getString("txs", "").split(" ");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new TransactionAdapter(this, txs);
        if(getActivity() instanceof MainActivity){
            this.main = (MainActivity) getActivity();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.content_balance, container, false);
        this.qr = layout.findViewById(R.id.qr_code);
        this.addr = layout.findViewById(R.id.address);
        this.name = layout.findViewById(R.id.label);
        this.bal = layout.findViewById(R.id.balance);
        this.showqr = layout.findViewById(R.id.showqr);
        this.copy_addr = layout.findViewById(R.id.copy_addr);
        this.send = layout.findViewById(R.id.button_send);
        this.root = container;

        this.copy_addr.setOnClickListener(this);
        this.showqr.setOnClickListener(this);
        this.send.setOnClickListener(this);
        this.qr.setOnClickListener(this);

        name.setText(label);

        RecyclerView recyclerView = layout.findViewById(R.id.transactions);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new Vars.VerticalSpaceItemDecoration(getContext(), 5));
        registerForContextMenu(recyclerView);
        adapter.notifyDataSetChanged();

        return layout;
    }

    public void onClick(View v) {
        if(v.equals(showqr)){
            showQRWidth = showqr.getWidth();

            final Animation aniQR = new ResizeAnimation(qr, ResizeAnimation.DIRECTION_VT, qr.getWidth() - (int)Vars.dpToPx(v.getContext(), 80), (int)Vars.dpToPx(v.getContext(), 48));
            aniQR.setFillAfter(true);
            aniQR.setDuration(500);

            final Animation aniBU = new TranslateAnimation(0F, 0F, 0F, -(int)Vars.dpToPx(v.getContext(), 80));
            aniBU.setFillAfter(true);
            aniBU.setDuration(500);
            aniBU.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) { }
                public void onAnimationRepeat(Animation animation) { }
                public void onAnimationEnd(Animation animation) {
                    showqr.setVisibility(View.INVISIBLE);
                    qr.startAnimation(aniQR);
                    qr.setVisibility(View.VISIBLE);
                }
            });
            showqr.startAnimation(aniBU);

            System.out.println("DEBUG: showqr clicked");
        } else if(v.equals(qr)){
            final Animation aniQR = new ResizeAnimation(qr, ResizeAnimation.DIRECTION_VT, (int)Vars.dpToPx(v.getContext(), 48), qr.getHeight());
            aniQR.setFillAfter(false);
            aniQR.setDuration(500);

            final Animation aniBU = new TranslateAnimation(0F, 0F, -(int)Vars.dpToPx(v.getContext(), 80), 0F);
            aniBU.setFillAfter(true);
            aniBU.setDuration(500);

            aniQR.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) { }
                public void onAnimationRepeat(Animation animation) { }
                public void onAnimationEnd(Animation animation) {
                    qr.setVisibility(View.INVISIBLE);
                    showqr.setVisibility(View.VISIBLE);
                    showqr.startAnimation(aniBU);
                }
            });
            qr.startAnimation(aniQR);

            System.out.println("DEBUG: showqr clicked");
        } else if(v.equals(copy_addr)){
            ClipboardManager manager = (ClipboardManager) main.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("address", addr.getText());
            manager.setPrimaryClip(clip);

            Toast.makeText(main, R.string.addr_copied, Toast.LENGTH_SHORT).show();
        } else if(v.equals(send)){
            FragmentManager fm = getFragmentManager();
            PayFragment pay = PayFragment.newInstance();
            pay.setTargetFragment(this, 300);

            FragmentTransaction tx = fm.beginTransaction();

            Bundle b = new Bundle();
            b.putString("address", address);
            b.putString("balance", balance.toPlainString());
            b.putString("label", label);

            tx.replace(R.id.root, pay);
            tx.addToBackStack(null);
            pay.setArguments(b);

            tx.commit();
        }
    }

    public void onStart() {
        super.onStart();
        update(address, balance);
    }

    public void onResume() {
        super.onResume();
        if(main != null){
            this.main.setTitle(R.string.screen_balance);
        }
    }

    public void update(String address, BigDecimal bal){
        if(address != null) {
            putAddress(address);
            addr.setText(address);
        }
        this.bal.setText(bal.toPlainString() + " D");
    }

    public void addTX(String tx){
        adapter.add(tx);
    }

    public String getAddress(){
        return address;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private Bitmap cropBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() - borderSize * 2, bmp.getHeight() - borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawBitmap(bmp, -borderSize, -borderSize, null);
        return bmpWithBorder;
    }

    public void putAddress(String address){
        try {
            Bitmap bit = QRCode.from(address).withSize(1024,1024).withColor(0xFF313131,0xFFFFFFFF).bitmap();
            qr.setImageBitmap(cropBorder(bit, 100));
        } catch (Exception e){
            e.printStackTrace();
        }

        qr.post(new Runnable() {
            public void run() {
                int margin = (root.getWidth() / 2 - qr.getWidth() / 2);
                System.out.println("DEBUG: root " + root.getWidth());
                System.out.println("DEBUG: qr " + qr.getWidth());

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)qr.getLayoutParams();
                params.setMargins(0, margin, 0, 0);
                qr.setLayoutParams(params);
            }
        });
    }
}
