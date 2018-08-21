package com.example.davychen.helloworld;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.davychen.helloworld.services.GeneralRequestService;
import com.example.davychen.helloworld.services.errDecode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;


public class SignInActivity extends AppCompatActivity implements View.OnFocusChangeListener{

    String id;
    String account;
    String withdraw;
    Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        submit = findViewById(R.id.signIn_submit);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        account = intent.getStringExtra("account");
        withdraw = intent.getStringExtra("withdraw");

        final TextInputEditText email = findViewById(R.id.signIn_emailField);
        email.setOnFocusChangeListener(this);
        final TextInputEditText nick_name = findViewById(R.id.signIn_nicknameField);
        nick_name.setOnFocusChangeListener(this);
        final TextInputEditText cell = findViewById(R.id.signIn_cellField);
        cell.setOnFocusChangeListener(this);
        final TextInputEditText addr = findViewById(R.id.signIn_addrField);
        addr.setOnFocusChangeListener(this);
        final TextInputEditText pwd = findViewById(R.id.signIn_pwdField);
        pwd.setOnFocusChangeListener(this);
        final TextInputEditText pwd_confirm = findViewById(R.id.signIn_pwdconfirmField);
        pwd_confirm.setOnFocusChangeListener(this);
        final TextInputEditText trans_pwd = findViewById(R.id.signIn_transpwdField);
        trans_pwd.setOnFocusChangeListener(this);
        final TextInputEditText trans_pwd_confirm = findViewById(R.id.signIn_transpwdconfirmField);
        trans_pwd_confirm.setOnFocusChangeListener(this);

    }

    public void submit(View v){
        LinearLayout layout = findViewById(R.id.signIn_layout);
        if (getCurrentFocus() != null){
            getCurrentFocus().clearFocus();
            layout.requestFocus();
        }

        signInAsyncTask task = new signInAsyncTask(this);
        task.execute();


    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        TextInputLayout layout = (TextInputLayout)v.getParent().getParent();
        int itemId = v.getId();
        if (!hasFocus){
            switch (itemId){
                case R.id.signIn_emailField:
                    if (!myIO.isValidEmail(((TextInputEditText)v).getText().toString())){
                        layout.setError("Not a valid Email");
                        v.setTag(false);
                    }else{
                        layout.setError(null);
                        v.setTag(true);
                    }
                    break;
                case R.id.signIn_pwdField:
                    int ret = myIO.pwdChecker(((TextInputEditText)v).getText().toString());
                    if (ret != 0){
                        v.setTag(false);
                        if (ret == 1){
                            layout.setError("Please include both digits and letters");
                        }else{
                            layout.setError("HAVE AT LEAST 8 CHARACTERS");
                        }
                    }else{
                        layout.setError(null);
                        v.setTag(true);
                    }
                    break;
                case R.id.signIn_pwdconfirmField:
                    TextInputEditText pwd = findViewById(R.id.signIn_pwdField);
                    if (!pwd.getText().toString().equals(((TextInputEditText) v).getText().toString())){
                        v.setTag(false);
                        layout.setError("Not the same as the password entered");
                    }else{
                        layout.setError(null);
                        v.setTag(true);
                    }
                    break;
                case R.id.signIn_transpwdField:
                    if (((TextInputEditText) v).getText().toString().length() < 6){
                        v.setTag(false);
                        layout.setError("At least 6 characters");
                    }else{
                        layout.setError(null);
                        v.setTag(true);
                    }
                    break;
                case R.id.signIn_transpwdconfirmField:
                    TextInputEditText trans_pwd = findViewById(R.id.signIn_transpwdField);
                    if (!trans_pwd.getText().toString().equals(((TextInputEditText) v).getText().toString())){
                        v.setTag(false);
                        layout.setError("Not the same as above");
                    }else{
                        layout.setError(null);
                        v.setTag(true);
                    }
                    break;
                default:
                    if(((EditText)v).getText().toString().isEmpty()){
                        layout.setError("Don't leave this empty");
                        v.setTag(false);
                    }else{
                        layout.setError(null);
                        v.setTag(true);
                    }
                    break;
            }

        }else{
            switch (itemId){
                case R.id.signIn_transpwdField:
                    TextInputEditText trans_pwd_confirm = findViewById(R.id.signIn_transpwdconfirmField);
                    trans_pwd_confirm.setText("");
                    trans_pwd_confirm.setTag(false);
                    break;
                case R.id.signIn_pwdField:
                    TextInputEditText pwd_confirm = findViewById(R.id.signIn_pwdconfirmField);
                    pwd_confirm.setText("");
                    pwd_confirm.setTag(false);
                    break;
            }
        }
    }
}
class signInAsyncTask extends AsyncTask<Void, Integer, Void> {
    private WeakReference<SignInActivity> wrap;
    private GeneralRequestService runnable = null;
    private boolean check = false;
    private int err;

    signInAsyncTask(SignInActivity act){
        this.wrap = new WeakReference<>(act);

    }
    @Override
    protected void onPreExecute() {
        SignInActivity unwrap = wrap.get();
        unwrap.submit.setEnabled(false);
        unwrap.submit.setText(R.string.checking);
        unwrap.submit.setBackgroundColor(unwrap.getResources().getColor(android.R.color.darker_gray));
        LinearLayout layout =  unwrap.findViewById(R.id.signIn_layout);
        boolean check = true;
        if (!recursiveCheck(layout)){
            check  = false;
        }
        RadioGroup sextype =  unwrap.findViewById(R.id.signIn_sextype);
        int sexid = sextype.getCheckedRadioButtonId();
        RadioButton sexButton =  unwrap.findViewById(R.id.signIn_female);
        if (sexid == -1){
            check = false;
            sexButton.setError("Please select a sex");
        }else{
            sexButton.setError(null);
        }
        this.check = check;
    }

    @Override
    protected void onPostExecute(Void aVoid) {

        SignInActivity act = wrap.get();
        if (runnable != null){
            this.err = runnable.getErr();
        }
        act.runOnUiThread(new errDecode(err, act));

        if (err == 0){
            act.submit.setText(R.string.success);
            act.submit.setBackgroundResource(android.R.color.holo_green_light);
            act.setResult(10);
            act.finish();
        }else {
            act.submit.setEnabled(true);
            act.submit.setText(R.string.submit);
            act.submit.setBackgroundResource(android.R.drawable.btn_default);

        }
    }
    private boolean recursiveCheck(View v){
        if (!(v instanceof ViewGroup)){
            if (v instanceof TextInputEditText) {
                if (v.getTag() == null){
                    TextInputLayout layout = (TextInputLayout) v.getParent().getParent();
                    layout.setError("Don't leave this field empty");
                    return false;
                }
                return (boolean) v.getTag();
            }
            return true;
        }else{
            boolean check = true;
            for (int i = 0; i < ((ViewGroup)v).getChildCount(); i++){
                if (!recursiveCheck(((ViewGroup) v).getChildAt(i))){
                    check = false;
                }
            }
            return check;

        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        SignInActivity unwrap = wrap.get();
        if (values[0] == 2) {
            unwrap.submit.setText(R.string.submitting);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        SignInActivity act = wrap.get();
        if (check){
            EditText email1 = act.findViewById(R.id.signIn_emailField);
            String email = email1.getText().toString();

            EditText nick_name = act.findViewById(R.id.signIn_nicknameField);
            String nick = nick_name.getText().toString();

            char sex;
            RadioGroup sextype = act.findViewById(R.id.signIn_sextype);
            int sexid = sextype.getCheckedRadioButtonId();
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
                    sex = 2; // unknown sex
                    break;
            }


            EditText cell1 = act.findViewById(R.id.signIn_cellField);
            String cell = cell1.getText().toString();

            EditText add = act.findViewById(R.id.signIn_addrField);
            String addr = add.getText().toString();

            EditText pwd1 = act.findViewById(R.id.signIn_pwdField);
            String pwd1s = pwd1.getText().toString();

            EditText transpwd = act.findViewById(R.id.signIn_transpwdField);
            String trans = transpwd.getText().toString();
            byte[] ninBytes =  myIO.toBytes(act.id, 18);
            byte[] accountBytes =  myIO.toBytes(act.account, 8);
            byte[] withdrawBytes =  myIO.toBytes(act.withdraw, 6);
            byte[] emailBytes = myIO.toBytes(email, 50);
            byte[] nickBytes = myIO.toBytes(nick, 10);
            byte sexBytes = (byte) sex;
            byte[] cellBytes = myIO.toBytes(cell, 15);
            byte[] addrBytes = myIO.toBytes(addr, 100);
            byte[] pwdBytes = myIO.toBytes(pwd1s, 20);
            byte[] transpwdBytes = myIO.toBytes(trans, 10);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            try {
                outputStream.write(ninBytes);
                outputStream.write(accountBytes);
                outputStream.write(withdrawBytes);
                outputStream.write(emailBytes);
                outputStream.write(nickBytes);
                outputStream.write(sexBytes);
                outputStream.write(cellBytes);
                outputStream.write(addrBytes);
                outputStream.write(pwdBytes);
                outputStream.write(transpwdBytes);
                publishProgress(2);
                byte mes[] = outputStream.toByteArray();
                this.runnable = new GeneralRequestService(1, mes);
                this.runnable.run();
            } catch (IOException e) {
                err = -2; //client side error
            }
        }else{
            err = 22; // fields check failed
        }
        return null;
    }
}

