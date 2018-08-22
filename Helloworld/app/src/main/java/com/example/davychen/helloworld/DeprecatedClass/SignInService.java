package com.example.davychen.helloworld.DeprecatedClass;

import android.app.Activity;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.davychen.helloworld.R;
import com.example.davychen.helloworld.myIO;
import com.example.davychen.helloworld.services.errDecode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;


public class SignInService implements Runnable {
    private String email;
    private String fname;
    private String lname;
    private char sex;
    private String nin;
    private String cell;
    private String addr;
    private String pwd1s;
    private String pwd2s;
    private String trans;
    private String transcon;
    private Activity activity;
    int err;

    public SignInService(Activity act){
        this.activity = act;
        EditText email1 = act.findViewById(R.id.signIn_emailField);
        this.email = email1.getText().toString();

        RadioGroup sextype = act.findViewById(R.id.signIn_sextype);
        int sexid = sextype.getCheckedRadioButtonId();
        if (sexid != -1){
            RadioButton sexbutton = act.findViewById(sexid);
            String buttonName = sexbutton.getText().toString();
            switch (buttonName) {
                case "Male":
                    sex = 0;
                    break;
                case "Female":
                    sex = 1;
                    break;
                default:
                    sex = 2;
                    break;
            }
        }else{
            sex = 2;
        }


        EditText id = act.findViewById(R.id.nin_field);
        this.nin = id.getText().toString();

        EditText cell1 = act.findViewById(R.id.signIn_cellField);
        this.cell = cell1.getText().toString();

        EditText add = act.findViewById(R.id.signIn_addrField);
        this.addr = add.getText().toString();

        EditText pwd1 = act.findViewById(R.id.signIn_pwdField);
        this.pwd1s = pwd1.getText().toString();

        EditText pwd2 = act.findViewById(R.id.signIn_pwdconfirmField);
        this.pwd2s = pwd2.getText().toString();

        EditText transpwd = act.findViewById(R.id.signIn_transpwdField);
        this.trans = transpwd.getText().toString();

        EditText tpc = act.findViewById(R.id.signIn_transpwdconfirmField);
        this.transcon = tpc.getText().toString();
    }
    @Override
    public void run() {

        try {
            int checker;
            if (!myIO.isValidEmail(this.email)) {
                err = 2;
            } else if (!this.pwd1s.equals(this.pwd2s)) {
                err = 3;
            } else if ((checker = myIO.pwdChecker(pwd1s)) != 0){
                err = checker;
            } else if (!this.trans.equals(this.transcon)){
                err = 4;
            }else{

                byte[] email = myIO.toBytes(this.email, 50);
                byte[] fname = myIO.toBytes(this.fname, 10);
                byte[] lname = myIO.toBytes(this.lname, 10);
                byte sex = (byte) this.sex;
                byte[] nin =  myIO.toBytes(this.nin, 18);
                byte[] cell = myIO.toBytes(this.cell, 15);
                byte[] addr = myIO.toBytes(this.addr, 100);
                byte[] pwd = myIO.toBytes(this.pwd1s, 20);
                byte[] transpwd = myIO.toBytes(this.trans, 10);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write(email);
                outputStream.write(fname);
                outputStream.write(lname);
                outputStream.write(sex);
                outputStream.write(nin);
                outputStream.write(cell);
                outputStream.write(addr);
                outputStream.write(pwd);
                outputStream.write(transpwd);
                byte mes[] = outputStream.toByteArray( );

                byte[] ret = myIO.toServer(1, mes);
                err = myIO.bytesToInt(Arrays.copyOfRange(ret, 0, 4));
            }
            if (err == 1){
                activity.finish();
            }
        } catch (IOException e) {
            err = 7;
        }

        activity.runOnUiThread(new errDecode(err, activity));
    }



}
