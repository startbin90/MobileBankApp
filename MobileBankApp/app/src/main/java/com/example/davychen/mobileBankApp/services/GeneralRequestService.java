package com.example.davychen.mobileBankApp.services;

import com.example.davychen.mobileBankApp.myIO;
import com.example.davychen.mobileBankApp.returnMessage;

import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Service used to send Server request and receive reply
 * implement Runnable and Callable interface so that the class
 * can be used in either way
 */
public class GeneralRequestService implements Runnable, Callable<returnMessage>{

    int err;
    int reqCode;
    byte[] data;
    byte[] send;

    /**
     * constructor
     * @param reqCode request code
     * @param send message or data user wants to send
     */
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

    @Override
    public returnMessage call(){
        byte[] ret = myIO.toServer(reqCode, send);
        this.err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));
        this.data = Arrays.copyOfRange(ret, 4, ret.length);
        return new returnMessage(err, data);
    }
}
