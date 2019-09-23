package com.xemplarsoft.dv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.hf.leveldb.LevelDB;
import com.xemplarsoft.Vars;
import com.xemplarsoft.dv.medium.DVClient;
import com.xemplarsoft.dv.medium.DVClientListener;

import java.math.BigDecimal;
import java.util.Set;

public class StartActivity extends AppCompatActivity implements DVClientListener {
    private final AddressManager addressManager = AddressManager.getInstance();
    public static DVClient client;
    public static DBWrapper db;

    private TextView status;

    private boolean ready = false;
    private int addyCount = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        status = findViewById(R.id.status);

        String internal = getFilesDir().getAbsolutePath();
        if(db == null) db = new DBWrapper(internal, LevelDB.configure().createIfMissing(true));

        client = new DVClient(this);
        client.start();

        String aes = db.getString("aes");
        if(aes == null){
            client.registerOnNetwork();
        } else {
            long uid = db.getLong("uid");
            client.loginToNetwork(aes, uid);
        }
    }


    @Override
    public void dvEventHappened(String data) {
        String[] dat = data.split(" ");
        String mess = "";
        boolean updateUI = false;

        switch (dat[0]){
            case "login":{
                if(dat[1].equals("attempt")){
                    mess = getResources().getString(R.string.start_login_0);
                    updateUI = true;
                } else if(dat[1].equals("chall")){
                    mess = getResources().getString(R.string.start_login_1);
                    updateUI = true;
                } else if(dat[1].equals("successful")){
                    mess = getResources().getString(R.string.start_login_2);
                    updateUI = true;
                    client.getAddresses();
                    client.getBlockHeight();
                }
                break;
            }

            case "register":{
                if(dat[1].equals("attempt")){
                    mess = getResources().getString(R.string.start_register_0);
                    updateUI = true;
                } else if(dat[1].equals("chall")){
                    mess = getResources().getString(R.string.start_register_1);
                    updateUI = true;
                } else if(dat[1].equals("successful")){
                    mess = getResources().getString(R.string.start_register_2);
                    updateUI = true;
                    client.getAddresses();
                    client.getBlockHeight();
                }
                break;
            }

            case "addresses":{
                updateUI = true;
                String[] addresses = dat[1].split(":");
                for(String s : addresses){
                    if(!addressManager.hasAddress(s)){
                        addressManager.addAddress(s, new BigDecimal("-1.0"));
                    }
                }
                mess = getResources().getString(R.string.start_init_0);
                ready = true;
                break;
            }

            case "addydata": {
                updateUI = true;
                String address = dat[1];
                BigDecimal balance = new BigDecimal(dat[2]);
                String label = dat[3];
                String[] txs = dat[4].split(":");

                if(!addressManager.hasAddress(address)){
                    addressManager.addAddress(new Address(address, label, balance, txs));
                } else {
                    addressManager.setLabel(address, label);
                    addressManager.setBalance(address, balance);
                    addressManager.setTXs(address, txs);
                    addressManager.clean(address);
                }
                mess = getResources().getString(R.string.start_init_1);
                addyCount--;
                break;
            }
        }

        final String message = mess;
        if(updateUI){
            runOnUiThread(new Runnable() {
                public void run() {
                    update(message);
                }
            });
        }

        Set<String> addresses = addressManager.getAddressSet();
        addyCount = addresses.size();
        for(String s : addresses){
            if(addressManager.isDirty(s)){
                addressManager.clean(s);
                client.getAddyData(s);
                break;
            }
        }
    }

    private void loadAddresses(){
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
    }

    private void update(String text){
        status.setText(text);

        String[] addresses = addressManager.getAddressArray();
        String dat = "";
        for(String s : addresses){
            dat += s + " ";
        }
        dat = dat.trim().replace(' ', ':');
        db.putString(Vars.KEY_ADDRESSES, dat);
        for(Address a : addressManager.getAddresses()){
            String txs = "";
            for(String s : a.getTXs()) txs += s + " ";
            db.putString(a.getAddress(), a.getLabel() + ":" + a.getBalance() + ":" + txs.trim());
        }

        if(addyCount == 0 && ready){
            ready = false;
            loadAddresses();

            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }
}
