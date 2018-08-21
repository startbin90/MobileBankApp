package com.example.davychen.helloworld.services;

import android.util.Log;

import com.example.davychen.helloworld.account;
import com.example.davychen.helloworld.fragments.accounts_list;
import com.example.davychen.helloworld.items.account_item;
import com.example.davychen.helloworld.myIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class retrieveAccountInfo implements Runnable {
    private account parentAct;
    private accounts_list frag;
    int err;
    private static String TAG = "retrieveAccountInfoService";
    public retrieveAccountInfo(account act, accounts_list frag) {
        this.parentAct = act;
        this.frag = frag;
    }

    @Override
    public void run() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write(myIO.toBytes(parentAct.nin,18));
            byte mes[] = outputStream.toByteArray( );
            byte[] ret = myIO.toServer(10, mes);
            err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));
            ret = Arrays.copyOfRange(ret, 4, ret.length);

            if (err == 0){
                ArrayList<account_item> temp = new ArrayList<>();
                ArrayList<byte[]> divided = myIO.bytesArrayDivider(ret, 50, 10, 1, 18, 15, 100, 4);
                if (divided.size() >= 7){
                    this.parentAct.email = new String(divided.get(0)).trim();
                    this.parentAct.nick_name = new String(divided.get(1)).trim();
                    this.parentAct.sex = (char) divided.get(2)[0];
                    this.parentAct.nin =  new String(divided.get(3)).trim();
                    this.parentAct.cell = new String(divided.get(4)).trim();
                    this.parentAct.addr = new String(divided.get(5)).trim();
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
                        Log.i(TAG, "no linked account, which is impossible, error must occurred");
                    }
                    parentAct.itemLst.clear();
                    parentAct.itemLst.addAll(temp);

                }else{
                    Log.e(TAG, "the size of returned Arraylist by bytesArrayDivider not correct");
                }

            }

        } catch (IOException e) {
            err  = 7;
        }
        parentAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                frag.adapter.notifyDataSetChanged();
                frag.mRefreshLayout.setRefreshing(false);
            }
        });
        this.parentAct.runOnUiThread(new errDecode(err, parentAct));
    }
}
