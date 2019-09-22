package com.xemplarsoft.dv;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.github.hf.leveldb.LevelDB;
import com.xemplarsoft.Vars;
import com.xemplarsoft.dv.adapters.AddressClickListener;
import com.xemplarsoft.dv.fragments.AddressFragment;
import com.xemplarsoft.dv.medium.DVClient;
import com.xemplarsoft.dv.medium.DVClientListener;
import com.xemplarsoft.dv.fragments.BalanceFragment;
import com.xemplarsoft.dv.tx.RawTransactionBuilder;
import com.xemplarsoft.libs.crypto.server.domain.RawTransactionOverview;
import com.xemplarsoft.libs.crypto.server.domain.UTXO;
import com.xemplarsoft.libs.crypto.server.domain.UTXOverview;
import com.xemplarsoft.libs.util.Base64;
import com.xemplarsoft.libs.util.Sha256;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigDecimal;
import java.security.Provider;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.util.ArrayList;
import java.util.Set;

import static com.xemplarsoft.Vars.TX_FEE;
import static com.xemplarsoft.Vars.deserialize;
import static com.xemplarsoft.libs.util.DataUtil.bytes2hex;
import static com.xemplarsoft.libs.crypto.KeyManager.convertPrivToWIF;
import static com.xemplarsoft.libs.crypto.KeyManager.convertWIFtoECPrivateKey;
import static com.xemplarsoft.libs.crypto.KeySet.adjustTo64;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity implements DVClientListener, AddressClickListener {
    public static AddressManager addressManager = new AddressManager();
    public static BigDecimal usd = new BigDecimal("0.0");
    public static BigDecimal btc = new BigDecimal("0.0");

    private FragmentManager manager;
    private DVClient cli;

    private BalanceFragment fragBalance;
    private AddressFragment fragAddresses;

    private TextView bh;

    private DBWrapper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBouncyCastle();
        setContentView(R.layout.title_layout);

        manager = getSupportFragmentManager();
        fragBalance = new BalanceFragment();
        fragAddresses = AddressFragment.newInstance();

        FragmentTransaction fragTX = manager.beginTransaction();
        fragTX.add(R.id.root, fragAddresses);
        fragTX.commit();

        bh = findViewById(R.id.blockheight);

        cli = new DVClient(this);
        cli.start();
    }

    public void dwEventHappened(String data) {
        String[] tad = data.split(" ");
        boolean updateUI = false;
        switch (tad[0]){
            case "balance":{
                String address = tad[2];
                BigDecimal amount = new BigDecimal(tad[3]);
                if(tad[1].equals("received")){
                    if(!addressManager.hasAddress(address)){
                        cli.getAddresses();
                    } else {
                        BigDecimal bal = addressManager.getBalance(address);
                        addressManager.setBalance(address, bal.add(amount));
                        System.out.println(address + " received " + amount.toPlainString() + " D");
                        addressManager.addTX(address, tad[4]);
                        addTX(address, tad[4]);
                    }
                } else if(tad[1].equals("staked")){
                    System.out.println(address + " staked " + amount.toPlainString() + " D");
                    BigDecimal bal = addressManager.getBalance(address);
                    addressManager.setBalance(address, bal.add(amount));
                    addressManager.addTX(address, tad[4]);
                    addTX(address, tad[4]);
                } else if(tad[1].equals("sent")){
                    System.out.println(address + " sent " + amount.toPlainString() + " D");
                    BigDecimal bal = addressManager.getBalance(address);
                    addressManager.setBalance(address, bal.subtract(amount));
                } else if(tad[1].equals("total")){
                    if(address.equals("wallet")){
                        System.out.println("Your total balance is " + amount.toPlainString() + " D");
                    } else {
                        System.out.println(address + " has a total balance of " + amount.toPlainString() + " D");
                        addressManager.setBalance(address, amount);
                    }
                }
                updateUI = true;
                break;
            }
            case "addresses":{
                updateUI = true;
                String[] addresses = tad[1].split(":");
                for(String s : addresses){
                    if(!addressManager.hasAddress(s)){
                        addressManager.addAddress(s, new BigDecimal("-1.0"));
                    }
                }
                break;
            }

            case "address": {
                updateUI = true;
                String address = tad[1];
                if (!addressManager.hasAddress(address)) {
                    addressManager.addAddress(address, new BigDecimal("-1.0"));
                }
                break;
            }
            case "label": {
                updateUI = true;
                String address = tad[1];
                String label = tad[2];

                addressManager.setLabel(address, label);
                break;
            }

            case "addydata": {
                updateUI = true;
                String address = tad[1];
                BigDecimal balance = new BigDecimal(tad[2]);
                String label = tad[3];
                String[] txs = tad[4].split(":");

                if(!addressManager.hasAddress(address)){
                    addressManager.addAddress(new Address(address, label, balance, txs));
                } else {
                    addressManager.setLabel(address, label);
                    addressManager.setBalance(address, balance);
                    addressManager.setTXs(address, txs);
                    addressManager.clean(address);
                }
                break;
            }

            case "txs":{
                updateUI = true;
                String address = tad[1];
                String[] txs = tad[2].split(":");

                addressManager.setTXs(address, txs);
                break;
            }

            case "bh":{
                setBlockHeight(Integer.parseInt(tad[1]));
                break;
            }

            case "market":{
                if(tad[1].equals("usd")){
                    usd = new BigDecimal(tad[2]);
                } else if(tad[1].equals("btc")){
                    btc = new BigDecimal(tad[2]);
                }

                savePrices();
                break;
            }

            case "key":{
                String address = tad[1];
                cli.getAddyData(address);
                break;
            }

            case "pay":{
                updateUI = true;
                if(tad[1].equals("successful")){
                    addressManager.dirtyAll();
                }
                break;
            }

            case "unspent":{
                ArrayList<UTXO> serialized = new ArrayList<>();
                BigDecimal total = new BigDecimal(0);
                Class c;
                try {
                    c = Class.forName("com.xemplarsoft.libs.crypto.server.domain.UTXO");
                } catch (Exception e){
                    e.printStackTrace();
                    break;
                }

                if(tad[1].equals("for")){
                    String addr = tad[2];
                    for (int i = 3; i < tad.length; i++) {
                        String dat = new String(Base64.decode(tad[i]));

                        UTXO tx = (UTXO) deserialize(dat, c);
                        total = total.add(tx.getAmount());
                        serialized.add(tx);
                    }
                } else {
                    for (int i = 1; i < tad.length; i++) {
                        String dat = new String(Base64.decode(tad[i]));

                        UTXO tx = (UTXO) deserialize(dat, c);
                        total = total.add(tx.getAmount());
                        serialized.add(tx);
                    }
                }

                System.out.println("Total : " + total.toPlainString());
                break;
            }

            case "dumpprivkey":{
                System.out.println("WIF: " + tad[1]);
                break;
            }

            case "unspentfortx":{
                ArrayList<UTXO> serialized = new ArrayList<>();
                BigDecimal total = new BigDecimal(0);
                Class c;
                try {
                    c = Class.forName("com.xemplarsoft.libs.crypto.server.domain.UTXO");
                } catch (Exception e){
                    e.printStackTrace();
                    makePayment(null);
                    break;
                }

                for (int i = 1; i < tad.length; i++) {
                    String dat = new String(Base64.decode(tad[i]));

                    UTXO tx = (UTXO) deserialize(dat, c);
                    total = total.add(tx.getAmount());
                    serialized.add(tx);
                }

                makePayment(serialized);
                break;
            }

            case "rawtx":{
                String address = tad[1];
                String hex = tad[2];

                System.out.println("RAW TX: " + hex);

                String WIF = db.getString(address + ":WIF");
                //ECPrivateKey priv = KeyManager.convertWIFtoECPrivateKey(WIF);

                break;
            }

            case "decoded":{
                String dat = new String(Base64.decode(tad[1]));
                Class c;
                try {
                    c = Class.forName("com.xemplarsoft.libs.crypto.server.domain.RawTransactionOverview");
                } catch (Exception e){
                    e.printStackTrace();
                    break;
                }
                RawTransactionOverview over = (RawTransactionOverview) deserialize(dat, c);
                break;
            }
        }

        if(updateUI){
            runOnUiThread(new Runnable() {
                public void run() {
                    update();
                }
            });
        }

        Set<String> addresses = addressManager.getAddressSet();
        for(String s : addresses){
            if(addressManager.isDirty(s)){
                addressManager.clean(s);
                cli.getAddyData(s);
                break;
            }
        }
    }

    public void updateBalanceFragment(){
        if(fragBalance.isVisible()) {
            String addr = fragBalance.getAddress();
            fragBalance.update(addr, addressManager.getBalance(addr));
        }
    }

    public void setBlockHeight(final int height){
        runOnUiThread(new Runnable() {
            public void run() {
                bh.setText(height + "");
            }
        });
    }

    public void setTitle(int id){
        String r = id != -1 ? getResources().getString(id) : "";
        ((TextView) findViewById(R.id.screen)).setText(r);
    }
    public void setTitle(int id, String s){
        String r = id != -1 ? getResources().getString(id) + s : s;
        ((TextView) findViewById(R.id.screen)).setText(r);
    }
    public void setTitle(String s){
        ((TextView) findViewById(R.id.screen)).setText(s);
    }

    public void addTX(final String address, final String tx){
        runOnUiThread(new Runnable() {
            public void run() {
                if(fragBalance.isVisible() && fragBalance.getAddress().equals(address)){
                    fragBalance.addTX(tx);
                }
            }
        });
    }

    public void onAddressClicked(View v, String address) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        String txs = "";
        for(String s : addressManager.getTXs(address)) txs += s + " ";

        Bundle b = new Bundle();
        b.putString("address", address);
        b.putString("balance", addressManager.getBalance(address).toPlainString());
        b.putString("label", addressManager.getLabel(address));
        b.putString("txs", txs.trim());

        transaction.replace(R.id.root, fragBalance);
        transaction.addToBackStack(null);
        fragBalance.setArguments(b);

        transaction.commit();
    }
    public boolean onAddressLongClicked(View v, String address) {
        return false;
    }

    public void rename(String address, String label) {
        addressManager.setLabel(address, label);
        cli.setLabel(address, label);
    }

    //Payment Method Group
    private String to, from, narration;
    private BigDecimal amt, total;
    public void makePayment(String to, BigDecimal amount, String narration, String from){
        this.to = to;
        this.from = from;
        this.amt = amount;
        this.total = amount.add(TX_FEE);
        this.narration = narration;

        cli.createRawTXList(from);
    }
    private void makePayment(ArrayList<UTXO> utxos){
        if(utxos == null){
            this.to = null;
            this.from = null;
            this.amt = null;
            this.total = null;
            this.narration = null;
            return;
        }

        ArrayList<UTXO> required = new ArrayList<>();
        ArrayList<UTXOverview> requiredOverview = new ArrayList<>();
        BigDecimal soFar = new BigDecimal(0);
        boolean needsMore = true;
        int index = 0;

        while(needsMore){
            UTXO current = utxos.get(index);
            soFar = soFar.add(current.getAmount());
            required.add(current);
            requiredOverview.add(current.asOverview());

            if(soFar.compareTo(total) >= 0){
                needsMore = false;
            }
            index++;

            if(index >= utxos.size()) needsMore = false;
        }

        String WIF = "QP5bV1BrQmy74wxmEEUb1DRMzU2cfUbpopyf1sL9HhSqtbWCaV2B";
        ECPrivateKey priv = convertWIFtoECPrivateKey(WIF);
        System.out.println("WIF: " + convertPrivToWIF(adjustTo64(priv.getS().toString(16)).toUpperCase()));

        RawTransactionBuilder raw = new RawTransactionBuilder(required, from, to, amt);
        System.out.println("RAWTXBUILDER unsigned: " + raw.buildUnsignedRawTX());
        System.out.println("RAWTXBUILDER txid: " + bytes2hex(Sha256.getHash(Sha256.getHash(raw.buildUnsignedRawTX()))));
        try {
            System.out.println("RAWTXBUILDER signed: " + raw.signTX(priv));
        } catch (Exception e){
            e.printStackTrace();
        }
        this.to = null;
        this.amt = null;
        this.total = null;
        this.narration = null;
    }


    //END

    public void newRemoteKey(){
        cli.newRemoteKey();
    }

    public void update(){
        String[] addresses = addressManager.getAddressArray();
        fragAddresses.setAddresses(addressManager.getAddresses());

        String dat = "";
        for(String s : addresses){
            dat += s + " ";
        }
        dat = dat.trim().replace(' ', ':');
        db.putString(Vars.KEY_ADDRESSES, dat);
        BigDecimal total = new BigDecimal("0.0");
        for(Address a : addressManager.getAddresses()){
            total = total.add(a.getBalance());
            String txs = "";
            for(String s : a.getTXs()) txs += s + " ";
            db.putString(a.getAddress(), a.getLabel() + ":" + a.getBalance() + ":" + txs.trim());

            if(fragBalance.isVisible() && a.getAddress().equals(fragBalance.getAddress())){
                updateBalanceFragment();
            }
        }
        if(fragAddresses != null && fragAddresses.total != null) {
            fragAddresses.total.setText(total.toPlainString() + " D");
        }
    }

    protected void savePrices(){
        db.putString("USD", usd.toPlainString());
        db.putString("BTC", btc.toPlainString());

        if(fragAddresses != null && fragAddresses.btc != null){
            fragAddresses.btc.setText(MainActivity.btc.toPlainString() + " \u20BF");
            fragAddresses.usd.setText("$" + MainActivity.usd.toPlainString());
        }
    }

    protected void onStart() {
        super.onStart();
        try {
            String internal = getFilesDir().getAbsolutePath();
            if(db == null) db = new DBWrapper(internal, LevelDB.configure().createIfMissing(true));

            String usd_s = db.getString("USD");
            String btc_s = db.getString("BTC");
            if(usd_s != null){
                usd = new BigDecimal(usd_s);
            }
            if(btc_s != null){
                btc = new BigDecimal(btc_s);
            }

            String raw = db.getString(Vars.KEY_ADDRESSES);
            if(raw != null) {
                String[] read = raw.split(":");
                for (String s : read) {
                    String[] dat = db.getString(s).split(":");
                    if (dat.length > 2) {
                        String[] txs = dat[2].split(" ");
                        addressManager.addAddress(new Address(s, dat[0], new BigDecimal(dat[1]), txs));
                    } else {
                        addressManager.addAddress(new Address(s, dat[0], new BigDecimal(dat[1])));
                    }
                }
            }
            update();
        } catch (Exception e){
            e.printStackTrace();
        }

        cli.getAddresses();
        cli.getBlockHeight();
        cli.getMarketUSD();
        cli.getMarketBTC();
    }

    protected void onStop() {
        super.onStop();
        db.close();
    }

    private void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }
}
