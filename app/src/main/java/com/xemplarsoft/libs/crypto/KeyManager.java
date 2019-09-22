package com.xemplarsoft.libs.crypto;

import com.xemplarsoft.libs.util.Base58;
import com.xemplarsoft.libs.util.Ripemd160;
import com.xemplarsoft.libs.util.Sha256;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;

import static com.xemplarsoft.libs.util.DataUtil.bytes2hex;
import static com.xemplarsoft.libs.util.DataUtil.hex2bytes;


public final class KeyManager {
    public static final String CURVE = "secp256k1";

    public static String convertPubToAddress(byte[] pub){
        byte[] data = Ripemd160.getHash(Sha256.getHash(pub));
        byte[] dat = new byte[data.length + 1];
        System.arraycopy(data, 0, dat, 1, data.length);
        dat[0] = (byte)0x1E;

        byte[] checksum = Sha256.getHash(Sha256.getHash(dat));
        checksum = slice(checksum, 4);

        byte[] fin = new byte[dat.length + 4];
        System.arraycopy(dat, 0, fin, 0, dat.length);
        System.arraycopy(checksum, 0, fin, dat.length, 4);

        return Base58.encode(fin);
    }

    public static String convertPubToAddress(String pub){
        return convertPubToAddress(hex2bytes(pub));
    }

    public static String convertPrivToWIF(byte[] priv){
        byte[] key = new byte[priv.length + 2];
        System.arraycopy(priv, 0, key, 1, priv.length);
        key[0] = (byte)0x9E;
        key[key.length - 1] = (byte)0x01;

        byte[] checksum = Sha256.getHash(Sha256.getHash(key));
        checksum = slice(checksum, 4);

        byte[] fin = new byte[key.length + 4];
        System.arraycopy(key, 0, fin, 0, key.length);
        System.arraycopy(checksum, 0, fin, key.length, 4);

        return Base58.encode(fin);
    }

    public static String convertWIFtoPrivHex(String wif){
        byte[] raw = Base58.decode(wif);
        byte[] dat = new byte[raw.length - 6];
        System.arraycopy(raw, 1, dat, 0, dat.length);
        return bytes2hex(dat);
    }

    public static byte[] convertWIFtoPrivBytes(String wif){
        byte[] raw = Base58.decode(wif);
        byte[] dat = new byte[raw.length - 6];
        System.arraycopy(raw, 1, dat, 0, dat.length);
        return dat;
    }

    public static ECPrivateKey convertWIFtoECPrivateKey(String wif){
        byte[] dat = convertWIFtoPrivBytes(wif);
        System.out.println("PRIVKEY BYTES: " + bytes2hex(dat));

        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(CURVE);
        ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(new BigInteger(1, dat), spec);

        ECPrivateKey key = null;

        try{
            KeyFactory kf = KeyFactory.getInstance("EC", "BC"); // or "EC" or whatever
            key = (ECPrivateKey)kf.generatePrivate(ecPrivateKeySpec);
        } catch (Exception e){
            e.printStackTrace();
        }

        return key;
    }

    public static String pubKeyHashToAddress(String hash){
        byte[] data = hex2bytes(hash);

        byte[] dat = new byte[data.length + 1];
        System.arraycopy(data, 0, dat, 1, data.length);
        dat[0] = (byte)0x1E;

        byte[] checksum = Sha256.getHash(Sha256.getHash(dat));
        checksum = slice(checksum, 4);

        byte[] fin = new byte[dat.length + 4];
        System.arraycopy(dat, 0, fin, 0, dat.length);
        System.arraycopy(checksum, 0, fin, dat.length, 4);

        return Base58.encode(fin);
    }

    public static String addressToPubKeyHash(String address){
        byte[] data = Base58.decode(address);
        byte[] dat = new byte[data.length - 5];
        System.arraycopy(data, 1, dat, 0, dat.length);
        return bytes2hex(dat);
    }

    public static byte[] slice(byte[] a, int leng) {
        byte[] ret = new byte[leng];
        for (int i = 0; i < leng; i++) {
            ret[i] = a[i];
        }
        return ret;
    }

    public static String convertPrivToWIF(String priv){
        return convertPrivToWIF(hex2bytes(priv));
    }

    private static final String EVEN = "02";
    private static final String ODD = "03";

    public static String compressPublicKey(String toCompress) {
        if (Integer.parseInt(toCompress.substring(128, 130), 16) % 2 == 0)
            return  EVEN + toCompress.substring(2, 66);
        return ODD + toCompress.substring(2, 66);
    }

    public static String compressPublicKey(String toCompress, boolean none) {
        if (Integer.parseInt(toCompress.substring(126, 128), 16) % 2 == 0)
            return  EVEN + toCompress.substring(2, 66);
        return ODD + toCompress.substring(2, 66);
    }

    public static ECPublicKey getPubFromPriv(ECPrivateKey key) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
//        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
//        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(CURVE);
//
//        ECPoint Q = ecSpec.getG().multiply(((org.bouncycastle.jce.interfaces.ECPrivateKey) key).getD());
//        byte[] publicDerBytes = Q.getEncoded();
//
//        ECPoint point = ecSpec.getCurve().decodePoint(publicDerBytes);
//        ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);
//        return (ECPublicKey) keyFactory.generatePublic(pubSpec);

        ECParameterSpec params = key.getParams();
        org.bouncycastle.math.ec.ECPoint q = ECNamedCurveTable.getParameterSpec(CURVE).getG().multiply(key.getS());
        org.bouncycastle.math.ec.ECPoint bcW = ECNamedCurveTable.getParameterSpec(CURVE).getCurve().decodePoint(q.getEncoded());
        ECPoint w = new ECPoint(
                bcW.getX().toBigInteger(),
                bcW.getY().toBigInteger());
        ECPublicKeySpec keySpec = new ECPublicKeySpec(w, params);
        return (ECPublicKey) KeyFactory
                .getInstance("EC", org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME)
                .generatePublic(keySpec);
    }
}
