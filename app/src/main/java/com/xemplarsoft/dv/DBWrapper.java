package com.xemplarsoft.dv;

import com.github.hf.leveldb.LevelDB;
import com.github.hf.leveldb.exception.LevelDBClosedException;

public class DBWrapper {
    private LevelDB.Configuration config;
    private String path;
    private LevelDB db;

    public DBWrapper(String path, LevelDB.Configuration config) {
        this.path = path;
        this.config = config;
        try {
            db = LevelDB.open(path, config);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean reopen(){
        try {
            db = LevelDB.open(path, config);
            System.out.println("INFO: DB was closed, reopening...");
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean isClosed(){
        return db.isClosed();
    }

    public void putString(String key, String value){
        try {
            db.put(key.getBytes(), value.getBytes());
        } catch (LevelDBClosedException e){
            if(reopen()) putString(key, value);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void putDouble(String key, double d){
        long l = Double.doubleToLongBits(d);
        byte[] result = new byte[8];

        result[0] = (byte)((l & 0xFF00000000000000L) >> 56);
        result[1] = (byte)((l & 0x00FF000000000000L) >> 48);
        result[2] = (byte)((l & 0x0000FF0000000000L) >> 40);
        result[3] = (byte)((l & 0x000000FF00000000L) >> 32);
        result[4] = (byte)((l & 0x00000000FF000000L) >> 24);
        result[5] = (byte)((l & 0x0000000000FF0000L) >> 16);
        result[6] = (byte)((l & 0x000000000000FF00L) >> 8);
        result[7] = (byte)((l & 0x00000000000000FFL) >> 0);

        try {
            db.put(key.getBytes(), result);
        } catch (LevelDBClosedException e){
            if(reopen()) putDouble(key, d);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void putFloat(String key, float f){
        int i = Float.floatToIntBits(f);
        byte[] result = new byte[4];

        result[0] = (byte)((i & 0xFF000000) >> 24);
        result[1] = (byte)((i & 0x00FF0000) >> 16);
        result[2] = (byte)((i & 0x0000FF00) >> 8);
        result[3] = (byte)((i & 0x000000FF) >> 0);

        try {
            db.put(key.getBytes(), result);
        } catch (LevelDBClosedException e){
            if(reopen()) putFloat(key, f);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void putLong(String key, long l){
        byte[] result = new byte[8];

        result[0] = (byte)((l & 0xFF00000000000000L) >> 56);
        result[1] = (byte)((l & 0x00FF000000000000L) >> 48);
        result[2] = (byte)((l & 0x0000FF0000000000L) >> 40);
        result[3] = (byte)((l & 0x000000FF00000000L) >> 32);
        result[4] = (byte)((l & 0x00000000FF000000L) >> 24);
        result[5] = (byte)((l & 0x0000000000FF0000L) >> 16);
        result[6] = (byte)((l & 0x000000000000FF00L) >> 8);
        result[7] = (byte)((l & 0x00000000000000FFL) >> 0);

        try {
            db.put(key.getBytes(), result);
        } catch (LevelDBClosedException e){
            if(reopen()) putLong(key, l);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void putInt(String key, int i){
        byte[] result = new byte[4];

        result[0] = (byte)((i & 0xFF000000) >> 24);
        result[1] = (byte)((i & 0x00FF0000) >> 16);
        result[2] = (byte)((i & 0x0000FF00) >> 8);
        result[3] = (byte)((i & 0x000000FF) >> 0);

        try {
            db.put(key.getBytes(), result);
        } catch (LevelDBClosedException e){
            if(reopen()) putInt(key, i);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void putShort(String key, short s){
        byte[] result = new byte[2];

        result[0] = (byte)((s & 0xFF00) >> 8);
        result[1] = (byte)((s & 0x00FF) >> 0);

        try {
            db.put(key.getBytes(), result);
        } catch (LevelDBClosedException e){
            if(reopen()) putShort(key, s);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void putChar(String key, char c){
        byte[] result = new byte[2];

        result[0] = (byte)((c & 0xFF00) >> 8);
        result[1] = (byte)((c & 0x00FF) >> 0);

        try {
            db.put(key.getBytes(), result);
        } catch (LevelDBClosedException e){
            if(reopen()) putChar(key, c);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void putByte(String key, byte b){
        try {
            db.put(key.getBytes(), new byte[]{b});
        } catch (LevelDBClosedException e){
            if(reopen()) putByte(key, b);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void putBoolean(String key, boolean b){
        try {
            db.put(key.getBytes(), new byte[]{b ? (byte)1 : (byte)0});
        } catch (LevelDBClosedException e){
            if(reopen()) putBoolean(key, b);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void putBytes(String key, byte[] b){
        try {
            db.put(key.getBytes(), b);
        } catch (LevelDBClosedException e){
            if(reopen()) putBytes(key, b);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getString(String key){
        String s = null;
        try {
            s = new String(db.get(key.getBytes()));
        } catch (LevelDBClosedException e){
            if(reopen()) return getString(key);
            else e.printStackTrace();
        } catch (NullPointerException e){
            s = null;
        } catch (Exception e){
            e.printStackTrace();
        }

        return s;
    }

    public double getDouble(String key){
        long ret = 0;
        try {
            byte[] bytes = db.get(key.getBytes());

            ret += ((long)bytes[0] << 56);
            ret += ((long)bytes[1] << 48);
            ret += ((long)bytes[2] << 40);
            ret += ((long)bytes[3] << 32);
            ret += ((long)bytes[4] << 24);
            ret += ((long)bytes[5] << 16);
            ret += ((long)bytes[6] << 8);
            ret += ((long)bytes[7] << 0);
        } catch (LevelDBClosedException e){
            if(reopen()) return getDouble(key);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return Double.longBitsToDouble(ret);
    }

    public float getFloat(String key){
        int ret = 0;
        try {
            byte[] bytes = db.get(key.getBytes());

            ret += (bytes[0] << 24);
            ret += (bytes[1] << 16);
            ret += (bytes[2] << 8);
            ret += (bytes[3] << 0);
        } catch (LevelDBClosedException e){
            if(reopen()) return getFloat(key);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return Float.intBitsToFloat(ret);
    }

    public long getLong(String key){
        long ret = 0;
        try {
            byte[] bytes = db.get(key.getBytes());

            ret += ((long)bytes[0] << 56);
            ret += ((long)bytes[1] << 48);
            ret += ((long)bytes[2] << 40);
            ret += ((long)bytes[3] << 32);
            ret += ((long)bytes[4] << 24);
            ret += ((long)bytes[5] << 16);
            ret += ((long)bytes[6] << 8);
            ret += ((long)bytes[7] << 0);
        } catch (LevelDBClosedException e){
            if(reopen()) return getLong(key);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return ret;
    }

    public int getInt(String key){
        int ret = 0;
        try {
            byte[] bytes = db.get(key.getBytes());

            ret += (bytes[0] << 24);
            ret += (bytes[1] << 16);
            ret += (bytes[2] << 8);
            ret += (bytes[3] << 0);
        } catch (LevelDBClosedException e){
            if(reopen()) return getInt(key);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return ret;
    }

    public short getShort(String key){
        short ret = 0;
        try {
            byte[] bytes = db.get(key.getBytes());

            ret += (bytes[0] << 8);
            ret += (bytes[1] << 0);
        } catch (LevelDBClosedException e){
            if(reopen()) return getShort(key);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return ret;
    }

    public char getChar(String key){
        char ret = 0;
        try {
            byte[] bytes = db.get(key.getBytes());

            ret += (bytes[0] << 8);
            ret += (bytes[1] << 0);
        } catch (LevelDBClosedException e){
            if(reopen()) return getChar(key);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return ret;
    }

    public byte getByte(String key){
        byte ret = 0;
        try {
            byte[] bytes = db.get(key.getBytes());
            ret = bytes[0];
        } catch (LevelDBClosedException e){
            if(reopen()) return getByte(key);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return ret;
    }

    public boolean getBoolean(String key){
        boolean ret = false;
        try {
            byte[] bytes = db.get(key.getBytes());
            ret = bytes[0] == 1;
        } catch (LevelDBClosedException e){
            if(reopen()) return getBoolean(key);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return ret;
    }

    public byte[] getBytes(String key){
        byte[] ret = null;
        try {
            ret = db.get(key.getBytes());
        } catch (LevelDBClosedException e){
            if(reopen()) return getBytes(key);
            else e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return ret;
    }


    public void close(){
        db.close();
    }
}
