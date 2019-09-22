package com.xemplarsoft.dv.tx;

import com.xemplarsoft.libs.crypto.KeyManager;

import java.math.BigDecimal;

import static com.xemplarsoft.libs.util.DataUtil.hex2smallEndian;


public class RawOutput {
    public final BigDecimal value;
    public final String address;

    public RawOutput(String address, BigDecimal value){
        this.address = address;
        this.value = value;
    }

    public String getScriptPubKey(){
        return "76a914" + hex2smallEndian(KeyManager.addressToPubKeyHash(address)) + "88ac";
    }

    public String getScriptLengthHexLE(){
        return String.format("%1$02x", getScriptPubKey().length() / 2);
    }

    public String getValueHexLE(){
        int width = 16;
        char fill = '0';

        String toPad = value.multiply(new BigDecimal("100000000")).toBigInteger().toString(16);
        String padded = new String(new char[width - toPad.length()]).replace('\0', fill) + toPad;

        return hex2smallEndian(padded);
    }
}
