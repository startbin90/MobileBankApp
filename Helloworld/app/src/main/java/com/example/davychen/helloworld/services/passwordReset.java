package com.example.davychen.helloworld.services;

import android.app.Activity;

import com.example.davychen.helloworld.myIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class passwordReset implements Runnable {

    String nin;
    String pwd;
    String new_pwd;
    Activity act;
    int err;
    int type; // 0 for password 1 for transaction password

    public passwordReset(int type, String nin, String pwd, String new_pwd, Activity act) {
        this.nin = nin;
        this.pwd = pwd;
        this.new_pwd = new_pwd;
        this.act = act;
        this.type = type;
    }

    @Override
    public void run() {
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] ret;
            if (type == 0) {
                outputStream.write(myIO.toBytes(nin, 18));
                outputStream.write(myIO.toBytes(pwd, 20));
                outputStream.write(myIO.toBytes(new_pwd, 20));
                byte mes[] = outputStream.toByteArray();
                ret = myIO.toServer(3, mes);
            }else{
                outputStream.write(myIO.toBytes(nin, 18));
                outputStream.write(myIO.toBytes(pwd, 10));
                outputStream.write(myIO.toBytes(new_pwd, 10));
                byte mes[] = outputStream.toByteArray();
                ret = myIO.toServer(4, mes);
            }
            err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));

        }catch (IOException e){
            err  = 7;
        }
        act.runOnUiThread(new errDecode(err, act));
    }

    public int getErr() {
        return err;
    }

    public Activity getAct() {
        return act;
    }
}
