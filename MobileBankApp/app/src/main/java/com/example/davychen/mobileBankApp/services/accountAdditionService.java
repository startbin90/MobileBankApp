package com.example.davychen.mobileBankApp.services;

import android.support.v4.app.Fragment;

import com.example.davychen.mobileBankApp.fragments.accountAdditionFragment;
import com.example.davychen.mobileBankApp.myIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class accountAdditionService implements Runnable {

    /*account parentAct;
    String account;
    String withdrawal_passwrod;
    String id;
    String last_name;*/
    int err;
    accountAdditionFragment frag;

    public accountAdditionService(Fragment frag) {
        this.frag = (accountAdditionFragment) frag;
    }

    @Override
    public void run() {
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write(myIO.toBytes(frag.account.getText().toString(), 8));
            outputStream.write(myIO.toBytes(frag.withdrawalPwd.getText().toString(), 6));
            outputStream.write(myIO.toBytes(frag.parentAct.nin, 18));
            byte mes[] = outputStream.toByteArray( );
            byte[] ret = myIO.toServer(8, mes);
            err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));

        }catch (IOException e){
            err  = 7;
        }
        frag.parentAct.runOnUiThread(new errDecode(err, frag.parentAct));
    }

    public int getErr() {
        return err;
    }

    public accountAdditionFragment getFrag() {
        return frag;
    }
}
