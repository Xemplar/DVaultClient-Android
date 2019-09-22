package com.xemplarsoft.dv.tx;

import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;

import static com.xemplarsoft.libs.util.DataUtil.bytes2hex;
import static com.xemplarsoft.libs.crypto.KeyManager.compressPublicKey;
import static com.xemplarsoft.libs.crypto.KeyManager.convertPubToAddress;
import static com.xemplarsoft.libs.crypto.KeySet.adjustTo64;
import static com.xemplarsoft.libs.util.DataUtil.hex2smallEndian;

public class RawInput {
    private final String prevTXID, scriptPubKey;
    private final long prevOutIndex;

    private String scriptSig;

    public RawInput(String prevTX, String scriptPubKey, long prevOutIndex){
        this.prevTXID = prevTX;
        this.prevOutIndex = prevOutIndex;
        this.scriptPubKey = scriptPubKey;
    }

    public String getPrevTXHexLE(){
        return hex2smallEndian(prevTXID);
    }

    public String getPrevTXHex(){
        return prevTXID;
    }

    public String getPrevOutIndexLE(){
        return hex2smallEndian(String.format("%1$08x", prevOutIndex));
    }

    public String getScriptPubKey(){
        System.out.println("Script Pub Key: " + scriptPubKey);
        return scriptPubKey;
    }

    public String getScriptPubKeyLength(){
        return String.format("%1$02x", scriptPubKey.length() / 2);
    }

    public void setScriptSig(byte[] sig, ECPublicKey pub){
        String signature = bytes2hex(sig) + "01";

        ECPoint pt = pub.getW();
        String sx = adjustTo64(pt.getAffineX().toString(16));
        String sy = adjustTo64(pt.getAffineY().toString(16));

        String pubkey = compressPublicKey(sx + sy, false);

        String sigLength = String.format("%1$02x", signature.length() / 2);
        String pubLength = String.format("%1$02x", pubkey.length() / 2);

        System.out.println("PUBKEY: " + convertPubToAddress(pubkey));

        this.scriptSig = sigLength + signature + pubLength + pubkey;
    }

    public String getScriptSigLength(){
        return String.format("%1$02x", scriptSig.length() / 2);
    }

    public String getScriptSig(){
        return scriptSig;
    }
}
