package com.example.davychen.helloworld.services;

import com.example.davychen.helloworld.myIO;

import java.util.Arrays;

public class GeneralRequestService implements Runnable {

    int err;
    int reqCode;
    byte[] data;
    byte[] send;

    public GeneralRequestService(int reqCode, byte[] send) {
        this.send = send;
        this.reqCode = reqCode;
    }

    @Override
    public void run() {
        byte[] ret = myIO.toServer(reqCode, send);
        this.err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));
        this.data = Arrays.copyOfRange(ret, 4, ret.length);
    }

    public int getErr() {
        return err;
    }
}
