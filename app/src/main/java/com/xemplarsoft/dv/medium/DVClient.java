package com.xemplarsoft.dv.medium;

import com.xemplarsoft.Vars;
import com.xemplarsoft.libs.crypto.server.domain.Entity;
import com.xemplarsoft.libs.crypto.server.domain.UTXOverview;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.xemplarsoft.libs.util.Base64;

import static com.xemplarsoft.Vars.deserialize;
import static com.xemplarsoft.Vars.serialize;

public class DVClient implements Runnable{
    private static final int CONNECT_REGISTER = 0x03;
    private static final int CONNECT_LOGIN = 0x01;

    private static String pubkey = Vars.pubkey;
    protected volatile PublicKey async;
    protected volatile SecretKey sync;
    protected volatile String chall;
    protected long UID;

    protected volatile Socket socket;
    protected volatile BufferedReader reader;
    protected volatile BufferedWriter writer;

    protected volatile boolean isReady = false, running = false, connected = false;
    protected volatile DVClientListener listener;
    protected volatile String action;

    protected volatile int connectType;

    private Queue<String> data = new ArrayBlockingQueue<>(20);

    protected long millsAWK;

    private Thread t, t2, t3;

    public DVClient(DVClientListener listener){
        this.listener = listener;
        t = new Thread(this);
        t2 = new Thread(this.actionListener);
        t3 = new Thread(this.reconnect);
    }

    public void replaceListener(DVClientListener l){
        this.listener = l;
    }

    public synchronized boolean isConnected(){
        return connected;
    }

    public synchronized void start(){
        running = true;
        t.start();
        t2.start();
        t3.start();
    }

    public synchronized void stop(){
        isReady = false;
        running = false;
    }

    //TX Method Group

    public void createRawTX(ArrayList<UTXOverview> outs, String to, String from, BigDecimal amt, BigDecimal change){
        String outSerialized = "";
        for(UTXOverview over : outs){
            outSerialized += Base64.encode(serialize(over, false)) + " ";
        }
        outSerialized = outSerialized.trim().replace(' ', ':');
        data.add("createrawtx " + to + " " + from + " " + amt.toPlainString() + " " + change.toPlainString() + " " + outSerialized);
    }

    public void createRawTXList(String from){
        data.add("listunspentfortx " + from);
    }


    //Local Methods
    public void dumpprivkey(String address){
        data.add("dumpprivkey " + address);
    }

    public void getBalance(){
        data.add("getbalance");
    }

    public void newRemoteKey(){
        data.add("newkey remote");
    }

    public void getMarketUSD(){
        data.add("getmarket usd");
    }

    public void getMarketBTC(){
        data.add("getmarket btc");
    }

    public void getBalance(String address){
        data.add("getbalance " + address);
    }

    public void getLabel(String address){
        data.add("getlabel " + address);
    }

    public void setLabel(String address, String label){
        label = label.replaceAll("[^A-Za-z0-9 .]", "");
        label = label.replace(' ', '_');
        label = label.substring(0, Math.min(label.length(), 32));

        data.add("setlabel " + address + " " + label);
    }

    public void getAddresses(){
        data.add("getaddresses");
    }

    public void getAddyData(String address) {
        data.add("getaddydata " + address);
    }

    public void getBlockHeight(){
        data.add("getbh");
    }

    public void getUTXO(){
        data.add("listunspent");
    }

    public void getUTXO(String address){
        data.add("listunspent " + address);
    }

    public void getTXs(String address){
        data.add("gettxs " + address);
    }

    public void payTo(String address, BigDecimal amount){
        data.add("pay " + address + " " + amount.toPlainString());
    }

    public void payTo(String address, BigDecimal amount, String narration, String from){
        narration = narration.replace(' ', '_');
        data.add("pay " + address + " " + amount.toPlainString() + " " + narration + " " + from);
    }

    // Network Methods
    public void registerOnNetwork(){
        connectType = CONNECT_REGISTER;
    }

    public void loginToNetwork(String aes, long uid){
        try {
            byte[] decoded = Base64.decode(aes);
            this.sync = new SecretKeySpec(decoded, 0, decoded.length, "AES");
            this.UID = uid;
            connectType = CONNECT_LOGIN;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void registerOnNetwork(int step, String response){
        if(step == 0){
            System.out.println("INFO: Attempting to register on network.");
            chall = CryptoHandler.generateRandomString(20);
            listener.dvEventHappened("register attempt " + chall);
            String message = "loadmii:";
            sync = CryptoHandler.generateAES();
            String syncDat = "";
            try {
                syncDat = CryptoHandler.encryptMessage(new String(Base64.encode(sync.getEncoded())) + ":chall" + chall, async);
                write(message + syncDat);
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if(step == 1){
            try {
                System.out.println("REGISTER: Checking Challenge.");
                String dat = CryptoHandler.decryptMessage(response, sync);
                if(dat.equals("chall" + chall)){
                    System.out.println("REGISTER: Challenge Correct");
                    listener.dvEventHappened("register correct");
                    write(CryptoHandler.encryptMessage("finishmii", sync));
                } else {
                    System.out.println("REGISTER: Challenge Incorrect");
                    listener.dvEventHappened("register failed");
                    registerOnNetwork(0, "");
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if(step == 2){
            try {
                String dat = CryptoHandler.decryptMessage(response, sync);
                if(dat.startsWith("ACKR:")){
                    UID = Long.parseLong(dat.split(":")[1]);
                    isReady = true;
                    System.out.println("REGISTER: UID " + UID + " Received");
                    listener.dvEventHappened("register finished " + UID);
                    connectType = CONNECT_LOGIN;
                } else {
                    System.out.println("INFO: Server Error -1");
                    registerOnNetwork(0, "");
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public synchronized void write(String message){
        try {
            writer.write(message + "\n");
            writer.flush();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public synchronized void encryptAndWrite(String message){
        try {
            String str = CryptoHandler.encryptMessage(message, sync);
            System.out.println(message);
            System.out.println(str);
            writer.write(str + "\n");
            writer.flush();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void loginToNetwork(int step, String response){
        if(step == 0){
            System.out.println("INFO: Attempting to login to network.");
            listener.dvEventHappened("login attempt");
            try {
                write("logmiiin:" + UID);
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if(step == 1){
            try {
                String[] dat = response.split(":");
                String random = CryptoHandler.decryptMessage(dat[1], sync);
                System.out.println("INFO: challenge " + random + " received");
                listener.dvEventHappened("login chall" + random);
                String modnar = "";
                for(int i = random.length() - 1; i >= 0; i--) {
                    modnar = modnar + random.charAt(i);
                }
                write("verify:" + CryptoHandler.encryptMessage(modnar, sync));
                System.out.println("INFO: sending verification " + modnar);
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if(step == 2){
            try{
                String dat = CryptoHandler.decryptMessage(response, sync);
                if(dat.startsWith("ACKL:")){
                    if(UID == Long.parseLong(dat.split(":")[1])){
                        isReady = true;
                        System.out.println("INFO: Successfully logged in");
                        listener.dvEventHappened("login successful " + UID);
                    } else {
                        write("loginerr");
                        System.out.println("INFO: Error logging in");
                        listener.dvEventHappened("login failed");
                    }
                } else {
                    loginToNetwork(0, "");
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public String getAES(){
        return Base64.encode(sync.getEncoded());
    }

    public long getUID(){
        return UID;
    }

    public boolean isReady() {
        return isReady;
    }
    public void processMessage(String message){
        String[] dat = message.split(":");
        if(dat.length == 1){
            try{
                String data = CryptoHandler.decryptMessage(message, sync);
                if(data.startsWith("chall")){
                    registerOnNetwork(1, message);
                } else if(data.startsWith("ACKR")){
                    registerOnNetwork(2, message);
                } else if(data.startsWith("ACKL")){
                    loginToNetwork(2, message);
                } else {
                    processCommand(data);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if(dat[0].equals("check")){
            loginToNetwork(1, message);
        }
    }
    public void processCommand(String command){
        System.out.println("COMMAND: " + command);
        String[] dat = command.split("#");
        String[] tad = command.split(" ");
        if(dat.length > 1){
            try {
                Class c = Class.forName("com.xemplarsoft.libs.crypto.server.domain." + dat[0]);
                String data = new String(Base64.decode(dat[1]));
                Entity info = (Entity) deserialize(data, c);
                System.out.println(serialize(info, true));
                listener.dvEventHappened("data " + dat[0] + " " + dat[1]);
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if(tad.length > 1){
            listener.dvEventHappened(command);
        }
    }
    public void run() {
        while (running) {
            try {
                if(connected) {
                    String line = reader.readLine();
                    if(line != null) {
                        if(line.equals("AWK")) {
                            millsAWK = System.currentTimeMillis();
                        } else {
                            processMessage(line);
                        }
                    }
                }
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            t.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable actionListener = new Runnable() {
        public void run() {
            while(running){
                if(connected) {
                    action = data.poll();
                    if (action != null) {
                        encryptAndWrite(action);
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            try {
                t2.join();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private Runnable reconnect = new Runnable() {
        public void run() {
            millsAWK = System.currentTimeMillis();
            while(running){
                //AWK

                if(System.currentTimeMillis() - millsAWK > 10000){
                    if(connected){
                        System.err.println("ERROR: Lost connection to server");
                    }
                    connected = false;
                    isReady = false;
                }

                if(!connected){
                    try {
                        async = CryptoHandler.loadPublicKey(pubkey);
                        socket = new Socket(Vars.HOST, Vars.PORT);
                        millsAWK = System.currentTimeMillis();
                        connected = true;
                    } catch (NoRouteToHostException e){
                        System.out.println("ERROR: Cannot connect to host " + Vars.HOST + ":" + Vars.PORT);
                        connected = false;
                    } catch (Exception e){
                        connected = false;
                    }

                    if(connected) {
                        try {
                            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                Thread.sleep(100);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                return;
                            }
                        }

                        switch (connectType){
                            case CONNECT_REGISTER:
                                registerOnNetwork(0, "");
                                break;
                            case CONNECT_LOGIN:
                                loginToNetwork(0, "");
                                break;
                        }
                    }
                }
                try{
                    Thread.sleep(100);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            try {
                t3.join();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    };
}
