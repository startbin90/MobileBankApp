package com.example.davychen.mobileBankApp.services;

import android.support.annotation.NonNull;

import com.example.davychen.mobileBankApp.Activity.account;
import com.example.davychen.mobileBankApp.adapters.payeeItemAdapter;
import com.example.davychen.mobileBankApp.items.payee_item;
import com.example.davychen.mobileBankApp.myIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class modifyPayees implements Runnable {
    String email;
    char operationType; //1 add new 2 delete existing 0 modify existing
    int modifyPosition;
    String new_account;

    public int getErr() {
        return err;
    }

    String first_name;
    String last_name;
    payeeItemAdapter adapter;
    account act;
    int err;

    public modifyPayees(@NonNull payeeItemAdapter adapter, @NonNull account act) {
        this.adapter = adapter;
        this.act = act;
        this.email = act.email;
    }

    public void setAddNewMode(@NonNull String new_account, @NonNull String first_name,
                              @NonNull String last_name){
        this.operationType = 1;
        this.new_account = new_account;
        this.first_name = first_name;
        this.last_name = last_name;
    }

    public void setDeleteMode(@NonNull int position){
        this.operationType = 2;
        this.modifyPosition = position;
    }

    public void setEditMode(@NonNull int position, @NonNull String new_account, @NonNull String first_name,
                              @NonNull String last_name){
        this.operationType = 0;
        this.modifyPosition = position;
        this.new_account = new_account;
        this.first_name = first_name;
        this.last_name = last_name;
    }

    @Override
    public void run() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(myIO.toBytes(email, 50));
            outputStream.write(operationType);
            if (operationType == 1){
                outputStream.write(myIO.toBytes(new_account, 8));
                outputStream.write(myIO.toBytes(first_name, 10));
                outputStream.write(myIO.toBytes(last_name, 10));
            }else if (operationType == 2){
                outputStream.write(myIO.toBytes(adapter.getItem(modifyPosition).getAccount_num(), 8));
            }else {
                outputStream.write(myIO.toBytes(adapter.getItem(modifyPosition).getAccount_num(), 8));
                outputStream.write(myIO.toBytes(new_account, 8));
                outputStream.write(myIO.toBytes(first_name, 10));
                outputStream.write(myIO.toBytes(last_name, 10));
            }
            byte mes[] = outputStream.toByteArray( );
            byte[] ret = myIO.toServer(11, mes);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));


        } catch (IOException e) {
            err  = 7;
        }
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (err == 0){
                    if (operationType == 1){
                        adapter.add(new payee_item(new_account, first_name, last_name));
                    }else if (operationType == 2){
                        adapter.remove(modifyPosition);
                    }else {
                        payee_item item = adapter.getItem(modifyPosition);
                        item.setAccount_num(new_account);
                        item.setFirst_name(first_name);
                        item.setLast_name(last_name);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
        act.runOnUiThread(new errDecode(err, act));
    }
}
