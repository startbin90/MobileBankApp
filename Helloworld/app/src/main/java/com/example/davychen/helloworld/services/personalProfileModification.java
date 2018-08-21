package com.example.davychen.helloworld.services;

import com.example.davychen.helloworld.fragments.personalProfileFragment;
import com.example.davychen.helloworld.myIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class personalProfileModification implements Runnable {

    int err;
    personalProfileFragment frag;
    String email;
    String cell;
    String addr;
    String nin;

    public personalProfileModification(personalProfileFragment frag, String email, String cell, String addr, String nin) {
        this.frag = frag;
        this.email = email;
        this.cell = cell;
        this.addr = addr;
        this.nin = nin;
    }

    @Override
    public void run() {
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write(myIO.toBytes(email, 50));
            outputStream.write(myIO.toBytes(cell, 15));
            outputStream.write(myIO.toBytes(addr, 100));
            outputStream.write(myIO.toBytes(nin, 18));
            byte mes[] = outputStream.toByteArray( );
            byte[] ret = myIO.toServer(5, mes);
            err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));

        }catch (IOException e){
            err  = 7;
        }
        frag.parentAct.runOnUiThread(new errDecode(err, frag.parentAct));
    }

    public int getErr() {
        return err;
    }

    public personalProfileFragment getFrag() {
        return frag;
    }
}
