package com.xemplarsoft.dv;

import android.support.v4.util.ArraySet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddressManager {
    private static final AddressManager manager = new AddressManager();
    private ArrayList<Address> addresses = new ArrayList<>();

    public static AddressManager getInstance(){
        return manager;
    }
    private AddressManager(){}

    public boolean hasAddress(String address){
        for(Address a : addresses){
            if(a.getAddress().equals(address)) return true;
        }
        return false;
    }

    public void remove(String address){
        Address remove = null;
        for(Address a : addresses){
            if(a.getAddress().equals(address)){
                remove = a;
                break;
            }
        }

        addresses.remove(remove);
    }

    public void dirtyAll(){
        for(Address a : addresses){
            a.dirty = true;
        }
    }

    public BigDecimal getTotalBalance(){
        BigDecimal ret = new BigDecimal("0.0");
        for(Address a : addresses){
            ret = ret.add(a.balance);
        }

        return ret;
    }

    public BigDecimal getBalance(String address){
        for(Address a : addresses){
            if(a.getAddress().equals(address)) return a.getBalance();
        }
        return null;
    }

    public String getLabel(String address){
        for(Address a : addresses){
            if(a.getAddress().equals(address)) return a.getLabel();
        }
        return null;
    }

    public ArrayList<String> getTXsList(String address){
        for(Address a : addresses){
            if(a.getAddress().equals(address)) return a.getTXsList();
        }
        return null;
    }

    public String[] getTXs(String address){
        for(Address a : addresses){
            if(a.getAddress().equals(address)) return a.getTXs();
        }
        return null;
    }

    public int getTXCount(String address){
        for(Address a : addresses){
            if(a.getAddress().equals(address)) return a.getTXCount();
        }
        return 0;
    }

    public void addAddress(String address, BigDecimal balance){
        if(!hasAddress(address)) this.addresses.add(new Address(address, balance));
    }

    public void addAddress(Address address){
        if(!hasAddress(address.getAddress())) this.addresses.add(address);
    }

    public void setBalance(String address, BigDecimal balance){
        for(Address a : addresses){
            if(a.getAddress().equals(address)){
                a.setBalance(balance);
                return;
            }
        }
    }

    public void setLabel(String address, String label){
        for(Address a : addresses){
            if(a.getAddress().equals(address)){
                a.setLabel(label);
                return;
            }
        }
    }

    public void addTX(String address, String tx){
        for(Address a : addresses){
            if(a.getAddress().equals(address)){
                a.addTX(tx);
                return;
            }
        }
    }
    public void setTXs(String address, String[] txs){
        for(Address a : addresses){
            if(a.getAddress().equals(address)){
                a.setTXs(txs);
                return;
            }
        }
    }

    public boolean isDirty(String address){
        for(Address a : addresses){
            if(a.getAddress().equals(address)) return a.isDirty();
        }
        return false;
    }

    public void clean(String address){
        for(Address a : addresses){
            if(a.getAddress().equals(address)) a.clean();
        }
    }

    public Address getAddress(int index){
        return addresses.get(index);
    }

    public Set<String> getAddressSet(){
        ArraySet<String> ret = new ArraySet<>();
        for(Address a : addresses){
            ret.add(a.getAddress());
        }

        return ret;
    }

    public String[] getAddressArray(){
        String[] ret = new String[size()];
        for(int i = 0; i < ret.length; i++){
            ret[i] = getAddress(i).getAddress();
        }

        return ret;
    }

    public List<String> getAddressList(){
        ArrayList<String> ret = new ArrayList<>();
        for(Address a : addresses){
            ret.add(a.getAddress());
        }

        return ret;
    }

    public ArrayList<Address> getAddresses(){
        return addresses;
    }

    public Address getAddress(String address){
        for(Address a : addresses){
            if(a.getAddress().equals(address)) return a;
        }
        return null;
    }

    public int size(){
        return addresses.size();
    }
}
