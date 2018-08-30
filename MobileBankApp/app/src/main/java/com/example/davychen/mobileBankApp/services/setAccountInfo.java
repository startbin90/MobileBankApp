package com.example.davychen.mobileBankApp.services;

import android.util.Log;

import com.example.davychen.mobileBankApp.Activity.account;
import com.example.davychen.mobileBankApp.items.account_item;
import com.example.davychen.mobileBankApp.myIO;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class setAccountInfo implements Callable<Integer>{
    private account parentAct;
    private byte[] msg;
    private static String TAG = "setAccountInfoService";

    public setAccountInfo(account act, byte[] msg) {
        this.parentAct = act;
        this.msg = msg;
    }

    @Override
    public Integer call() {
        int err = 0;
        ArrayList<account_item> temp = new ArrayList<>();
        ArrayList<byte[]> divided = myIO.bytesArrayDivider(msg, 50, 10, 1, 18, 15, 100, 4);
        if (divided.size() >= 7){
            this.parentAct.email = new String(divided.get(0)).trim();
            this.parentAct.nick_name = new String(divided.get(1)).trim();
            this.parentAct.sex = (char) divided.get(2)[0];
            this.parentAct.nin =  new String(divided.get(3)).trim();
            this.parentAct.cell = new String(divided.get(4)).trim();
            this.parentAct.address = new String(divided.get(5)).trim();
            int count = myIO.bytesToInt(divided.get(6));

            if (count > 0 && divided.size() > 7){
                byte[] accountLists = divided.get(7);
                for (int i = 0; i < count; i++){
                    String num = myIO.bytesToString(accountLists,  i * 32, 8);
                    float balance = ByteBuffer.wrap(Arrays.copyOfRange(accountLists, 8 + i * 32, 12 + i * 32)).getFloat();
                    String first_name = myIO.bytesToString(accountLists,  12 + i * 32, 10);
                    String last_name = myIO.bytesToString(accountLists,  22 + i * 32, 10);
                    temp.add(new account_item(num, balance, first_name, last_name));
                }

            }else{
                err = myIO.NO_LINKED_ACCOUNT;
                Log.i(TAG, "no linked account, which is impossible, error must occurred");
            }
            parentAct.itemLst.clear();
            parentAct.itemLst.addAll(temp);

        }else{
            err = myIO.SERVER_ERROR; // could be server error
            Log.e(TAG, "the size of returned Arraylist by bytesArrayDivider not correct");
        }
        return err;
    }
}
