package com.example.davychen.helloworld.services;

import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.davychen.helloworld.MainActivity;
import com.example.davychen.helloworld.R;
import com.example.davychen.helloworld.myIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class LogInService implements Runnable {
    private String email;
    private String pwd;
    private char loginOpt;
    private MainActivity parentAct;
    private int err;
    private byte[] msg;

    public LogInService(MainActivity act){
        EditText text = act.findViewById(R.id.main_emailField);
        this.email = text.getText().toString();
        EditText pwd1 = act.findViewById(R.id.main_pwdField);
        this.pwd = pwd1.getText().toString();
        RadioGroup radioGroup = act.findViewById(R.id.main_radiogroup);
        int buttonId = radioGroup.getCheckedRadioButtonId();
        RadioButton button = act.findViewById(buttonId);
        String buttonName = button.getText().toString();
        switch (buttonName) {
            case "Email":
                loginOpt = 0;
                break;
            case "Account Number":
                loginOpt = 1;
                break;
            default:
                loginOpt = 2;
                break;
        }
        this.parentAct = act;
    }
    @Override
    public void run() {
        try {
            if (loginOpt == 0 && !myIO.isValidEmail(email)){ //email login and invalid email
                err = 3;
            }else {
                byte[] email = myIO.toBytes(this.email, 50);
                byte[] pwd = myIO.toBytes(this.pwd, 20);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(loginOpt);
                outputStream.write(email);
                outputStream.write(pwd);
                byte mes[] = outputStream.toByteArray();
                byte[] ret = myIO.toServer(2, mes);
                err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));
                msg = Arrays.copyOfRange(ret, 4, ret.length);
            }
        } catch (IOException e) {
            err  = 7;
        }
        parentAct.runOnUiThread(new errDecode(err, parentAct));
    }

    public MainActivity getParentAct() {
        return parentAct;
    }

    public int getErr() {
        return err;
    }

    public byte[] getMsg() {
        return msg;
    }
}
