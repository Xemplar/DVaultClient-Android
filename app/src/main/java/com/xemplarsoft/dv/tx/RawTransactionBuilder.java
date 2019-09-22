package com.xemplarsoft.dv.tx;

import com.xemplarsoft.libs.crypto.server.domain.UTXO;
import com.xemplarsoft.libs.util.Sha256;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.DSADigestSigner;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

import java.math.BigDecimal;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;

import static com.xemplarsoft.Vars.TX_FEE;
import static com.xemplarsoft.libs.crypto.KeyManager.CURVE;
import static com.xemplarsoft.libs.crypto.KeyManager.getPubFromPriv;
import static com.xemplarsoft.libs.util.DataUtil.hex2bytes;
import static com.xemplarsoft.libs.util.DataUtil.hex2smallEndian;
import static com.xemplarsoft.libs.util.DataUtil.reverse;

public class RawTransactionBuilder {
    public static final String VERSION = "01000000";
    public static final String SEQUENCE_NUMBER = hex2smallEndian(Integer.toHexString(0xFFFFFFFF));
    public static final String SEQUENCE_NUMBER_SIGN = hex2smallEndian(Integer.toHexString(0xFFFFFFFF - 1));
    public static final String LOCKTIME = "00000000";

    public final int inputCount, outputCount;
    public final RawOutput[] outputs;
    public final RawInput[] inputs;
    public RawTransactionBuilder(ArrayList<UTXO> utxos, String from, String to, BigDecimal amount){
        inputCount = utxos.size();
        outputCount = 2;
        outputs = new RawOutput[outputCount];
        inputs = new RawInput[inputCount];

        BigDecimal total = new BigDecimal(0);
        for(int i = 0; i < utxos.size(); i++){
            UTXO u = utxos.get(i);

            total = total.add(u.getAmount());
            inputs[i] = new RawInput(u.getTxId(), u.getScriptPubKey(), u.getVout());
        }

        BigDecimal change = total.subtract(amount).subtract(TX_FEE);
        outputs[1] = new RawOutput(from, change);
        outputs[0] = new RawOutput(to, amount);
    }

    public String buildHexUnsignedRawTX(){
        long mills = System.currentTimeMillis();
        String ret = VERSION; // VERSION

        ret += hex2smallEndian(String.format("%1$08x", mills / 1000L)); // TIME
        ret += hex2smallEndian(String.format("%1$02x", inputCount)); // INPUT COUNT

        for(int i = 0; i < inputCount; i++){
            RawInput in = inputs[i];
            ret += in.getPrevTXHexLE(); // INPUT[i] PREV TXID
            ret += in.getPrevOutIndexLE(); //INPUT[i] PREV OUTPUT INDEX
            ret += "00"; //INPUT[i] SCRIPT LENGTH
            ret += SEQUENCE_NUMBER; //INPUT[i] SEQUENCE NUMBER
        }

        ret += hex2smallEndian(String.format("%1$02x", outputCount)); // OUTPUT COUNT

        for(int i = 0; i < outputCount; i++){
            RawOutput out = outputs[i];
            ret += out.getValueHexLE(); // OUTPUT[i] VALUE
            ret += out.getScriptLengthHexLE(); //OUTPUT[i] PUBLIC KEY SCRIPT LENGTH
            ret += out.getScriptPubKey(); //OUTPUT[i] PUBLIC KEY SCRIPT
        }

        ret += LOCKTIME;
        return ret;
    }

    public byte[] buildUnsignedRawTX(){
        long mills = System.currentTimeMillis();
        String ret = VERSION; // VERSION

        ret += hex2smallEndian(String.format("%1$08x", mills / 1000L)); // TIME
        ret += hex2smallEndian(String.format("%1$02x", inputCount)); // INPUT COUNT

        for(int i = 0; i < inputCount; i++){
            RawInput in = inputs[i];
            ret += in.getPrevTXHexLE(); // INPUT[i] PREV TXID
            ret += in.getPrevOutIndexLE(); //INPUT[i] PREV OUTPUT INDEX
            ret += "00"; //INPUT[i] SCRIPT LENGTH
            ret += SEQUENCE_NUMBER; //INPUT[i] SEQUENCE NUMBER
        }

        ret += hex2smallEndian(String.format("%1$02x", outputCount)); // OUTPUT COUNT

        for(int i = 0; i < outputCount; i++){
            RawOutput out = outputs[i];
            ret += out.getValueHexLE(); // OUTPUT[i] VALUE
            ret += out.getScriptLengthHexLE(); //OUTPUT[i] PUBLIC KEY SCRIPT LENGTH
            ret += out.getScriptPubKey(); //OUTPUT[i] PUBLIC KEY SCRIPT
        }

        ret += LOCKTIME;
        return hex2bytes(ret);
    }

    private String buildSignedRawTX(){
        long mills = System.currentTimeMillis();
        String ret = VERSION; // VERSION

        ret += hex2smallEndian(String.format("%1$08x", (mills / 1000L) - 3L)); // TIME
        ret += hex2smallEndian(String.format("%1$02x", inputCount)); // INPUT COUNT

        for(int i = 0; i < inputCount; i++){
            RawInput in = inputs[i];
            ret += in.getPrevTXHexLE(); // INPUT[i] PREV TXID
            ret += in.getPrevOutIndexLE(); //INPUT[i] PREV OUTPUT INDEX
            ret += in.getScriptSigLength(); //INPUT[i] SCRIPT LENGTH
            ret += in.getScriptSig(); //INPUT[i] SCRIPT SIG
            ret += SEQUENCE_NUMBER; //INPUT[i] SEQUENCE NUMBER
        }

        ret += hex2smallEndian(String.format("%1$02x", outputCount)); // OUTPUT COUNT

        for(int i = 0; i < outputCount; i++){
            RawOutput out = outputs[i];
            ret += out.getValueHexLE(); // OUTPUT[i] VALUE
            ret += out.getScriptLengthHexLE(); //OUTPUT[i] PUBLIC KEY SCRIPT LENGTH
            ret += out.getScriptPubKey(); //OUTPUT[i] PUBLIC KEY SCRIPT
        }

        ret += LOCKTIME;
        return ret;
    }

    public String buildUnsignedRawTXForSigning(int inputIndex){
        String ret = VERSION; // VERSION
        ret += hex2smallEndian(String.format("%1$02x", inputCount)); // INPUT COUNT

        for(int i = 0; i < inputCount; i++){
            RawInput in = inputs[i];
            ret += in.getPrevTXHexLE(); // INPUT[i] PREV TXID
            ret += in.getPrevOutIndexLE(); //INPUT[i] PREV OUTPUT INDEX
            if(i == inputIndex){
                ret += in.getScriptPubKeyLength(); //INPUT[i] SCRIPT LENGTH
                ret += in.getScriptPubKey(); //INPUT[i] TEMP SCRIPT SIG
                System.out.println("SCRIPTPUBKEY: " + in.getScriptPubKey());
                System.out.println("PREVTX: " + in.getPrevTXHex());
            } else {
                ret += "00"; //INPUT[i] SCRIPT LENGTH
            }
            ret += SEQUENCE_NUMBER_SIGN; //INPUT[i] SEQUENCE NUMBER
        }

        ret += hex2smallEndian(String.format("%1$02x", outputCount)); // OUTPUT COUNT

        for(int i = 0; i < outputCount; i++){
            RawOutput out = outputs[i];
            ret += out.getValueHexLE(); // OUTPUT[i] VALUE
            ret += out.getScriptLengthHexLE(); //OUTPUT[i] PUBLIC KEY SCRIPT LENGTH
            ret += out.getScriptPubKey(); //OUTPUT[i] PUBLIC KEY SCRIPT
        }

        ret += LOCKTIME;
        ret += "01000000";

        return ret;
    }

    public String signTX(ECPrivateKey priv){
        ECPublicKey pub = null;
        try{
            pub = getPubFromPriv(priv);
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(CURVE);
            ECDomainParameters domain = new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN());

            DSADigestSigner signer = new DSADigestSigner(new ECDSASigner(), new SHA256Digest());
            signer.init(true, new ECPrivateKeyParameters(priv.getS(), domain));
            for(int i = 0; i < inputs.length; i++) {
                byte[] tx = hex2bytes(buildUnsignedRawTXForSigning(i));
                byte[] hash = Sha256.getHash(Sha256.getHash(tx));
                reverse(hash);
                signer.update(hash, 0, hash.length);
                byte[] sig = signer.generateSignature();
                inputs[i].setScriptSig(sig, pub);
                signer.reset();
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return buildSignedRawTX();
    }
}
