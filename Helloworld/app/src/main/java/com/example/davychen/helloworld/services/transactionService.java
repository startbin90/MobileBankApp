package com.example.davychen.helloworld.services;

import com.example.davychen.helloworld.myIO;
import com.example.davychen.helloworld.transactionConfirmationPage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class transactionService implements Runnable {

    private transactionConfirmationPage act;
    int err;

    public transactionService(transactionConfirmationPage act) {
        this.act = act;
    }

    @Override

    public void run() {
        String from_field = act.from;
        String payee_field = act.payee;
        String first_name_field = act.fName;
        String last_name_field = act.lName;
        float value_field = act.value;
        String trans_pwd = act.trans_pwd.getText().toString();
        String memo = act.memo;
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write(myIO.toBytes(from_field, 8));
            outputStream.write(myIO.toBytes(payee_field, 8));
            outputStream.write(myIO.toBytes(first_name_field, 10));
            outputStream.write(myIO.toBytes(last_name_field, 10));

            outputStream.write(myIO.floatToBytes(value_field));
            outputStream.write(myIO.toBytes(trans_pwd, 10));
            outputStream.write(myIO.toBytes(memo, 100));
            byte mes[] = outputStream.toByteArray( );
            byte[] ret = myIO.toServer(7, mes);
            err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));

        }catch (IOException e){
            err  = 7;
        }
        act.runOnUiThread(new errDecode(err, act));
    }

    public int getErr() {
        return err;
    }

    public transactionConfirmationPage getAct() {
        return act;
    }
}
