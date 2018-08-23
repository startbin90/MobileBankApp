package com.example.davychen.helloworld.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class errDecode implements Runnable {
    int err;
    private Context con;
    private static String TAG = "errDecode";

    public errDecode(int err, Context con) {
        this.err = err;
        this.con = con;
    }

    @Override
    public void run() {
        String toast;
        switch (err){
            case -2:
                toast = "client error occurred";
                break;
            case -1:
                toast = "server error occurred";
                break;
            case 0:
                toast = "success";
                break;
            case 1:
                toast = "failed";
                break;
            case 2:
                toast = "wrong account and password combination";
                break;
            case 3:
                toast = "Invalid Email address";
                break;
            case 4:
                toast = "Account number not linked";
                break;
            case 5:
                toast = "password not strong";
                break;
            case 6:
                toast = "Transaction failed";
                break;
            case 7:
                toast = "Connect failed";
                break;
            case 8:
                toast = "Account not found";
                break;
            case 9:
                toast = "NIN has not been registered";
                break;
            case 10:
                toast = "Wrong Transaction Password";
                break;
            case 11:
                toast = "mobilereg insertion error";
                break;
            case 12:
                toast = "Payee info not matching";
                break;
            case 13:
                toast = "Not enough balance or Invalid transaction amount";
                break;
            case 14:
                toast = "Provided account has already been linked";
                break;
            case 15:
                toast = "wrong withdrawal password";
                break;
            case 16:
                toast = "account addition: last name not matching system record";
                break;
            case 17:
                toast = "linked account insertion error";
                break;
            case 18:
                toast = "Email been taken";
                break;
            case 19:
                toast = "cell been taken";
                break;
            case 20:
                toast = "Server timeout";
                break;
            case 21:
                toast = "Account specified by account number and nin not found";
                break;
            case 22:
                toast = "Fields not checked";
                break;
            default:
                toast = "unknown error occurred: " + err;
                break;
        }
        if (err != 0){
            Toast suc = Toast.makeText(con, toast, Toast.LENGTH_SHORT);
            suc.show();
        }else{
            Log.i(TAG, "success");
        }

    }
}
