package com.xemplarsoft.libs.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DataUtil {
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    public static byte[] hex2bytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String bytes2hex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    public static String hex2smallEndian(String hex){
        String[] dat = split(hex, 2);
        String swapped = "";
        for(int i = dat.length - 1; i >= 0; i--){
            swapped += dat[i];
        }

        return swapped;
    }

    public static String[] split(String str, int count){
        String[] dat = new String[str.length() / count];
        for(int i = 0; i < str.length(); i+=count){
            dat[i / count] = str.substring(i, i + count);
        }

        return dat;
    }
}
