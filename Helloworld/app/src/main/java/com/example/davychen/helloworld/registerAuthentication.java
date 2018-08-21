package com.example.davychen.helloworld;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.davychen.helloworld.services.GeneralRequestService;
import com.example.davychen.helloworld.services.errDecode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class registerAuthentication extends AppCompatActivity implements View.OnFocusChangeListener {

    Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_authentication);
        final TextInputEditText id = findViewById(R.id.nin_field);
        id.setOnFocusChangeListener(this);
        final TextInputEditText account = findViewById(R.id.account_field);
        account.setOnFocusChangeListener(this);
        final TextInputEditText withdraw = findViewById(R.id.withdrawals_password_field);
        withdraw.setOnFocusChangeListener(this);
        submit = findViewById(R.id.validate);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = findViewById(R.id.registerAuth_layout);
                if (getCurrentFocus() != null){
                    getCurrentFocus().clearFocus();
                    layout.requestFocus();
                }

                registerAuthenticationAsyncTask task = new registerAuthenticationAsyncTask(registerAuthentication.this);
                task.execute();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == 10) {
               finish();
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)  {
        TextInputLayout layout = (TextInputLayout)v.getParent().getParent();
        int itemId = v.getId();
        if (!hasFocus){
            switch (itemId){
                case R.id.nin_field:
                    if (!myIO.isSimpleIDNumber(((TextInputEditText)v).getText().toString())){
                        layout.setError("Not a valid NIN");
                        v.setTag(false);
                    }else{
                        layout.setError(null);
                        v.setTag(true);
                    }
                    break;
                case R.id.account_field:
                    String account = ((TextInputEditText)v).getText().toString();
                    if (myIO.isAccountNumber(account)){
                        layout.setError(null);
                        v.setTag(true);
                    }else{
                        layout.setError("Invalid account number");
                        v.setTag(false);
                    }
                    break;
                case R.id.withdrawals_password_field:
                    if (((TextInputEditText) v).getText().toString().length() <= 0){
                        v.setTag(false);
                        layout.setError("Cannot be empty");
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
                case R.id.account_field:
                    TextInputEditText withdraw = findViewById(R.id.withdrawals_password_field);
                    withdraw.setText("");
                    withdraw.setTag(false);
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        submit.setEnabled(true);
        submit.setText(R.string.validate);
        submit.setBackgroundResource(android.R.drawable.btn_default);
    }
}
class registerAuthenticationAsyncTask extends AsyncTask<Void, Integer, Void> {
    private WeakReference<registerAuthentication> wrap;
    private GeneralRequestService runnable = null;
    private boolean check = false;
    private int err;

    registerAuthenticationAsyncTask(registerAuthentication act){
        this.wrap = new WeakReference<>(act);

    }
    @Override
    protected void onPreExecute() {
        registerAuthentication unwrap = wrap.get();
        unwrap.submit.setEnabled(false);
        unwrap.submit.setText(R.string.checking);
        unwrap.submit.setBackgroundColor(unwrap.getResources().getColor(android.R.color.darker_gray));

        this.check = fieldsCheck();
    }

    @Override
    protected void onPostExecute(Void aVoid) {

        registerAuthentication act = wrap.get();
        if (runnable != null){
            this.err = runnable.getErr();
        }
        act.runOnUiThread(new errDecode(err, act));
        if (err == 0){
            act.submit.setText(R.string.success);
            act.submit.setBackgroundResource(android.R.color.holo_green_light);
            Intent signIn = new Intent(act, SignInActivity.class);
            final TextInputEditText id = act.findViewById(R.id.nin_field);
            signIn.putExtra("id", id.getText().toString());
            final TextInputEditText account = act.findViewById(R.id.account_field);
            signIn.putExtra("account", account.getText().toString());
            final TextInputEditText withdraw = act.findViewById(R.id.withdrawals_password_field);
            signIn.putExtra("withdraw", withdraw.getText().toString());
            act.startActivityForResult(signIn ,1);
        }else {
            act.submit.setEnabled(true);
            act.submit.setText(R.string.validate);
            act.submit.setBackgroundResource(android.R.drawable.btn_default);

        }

    }
    private boolean fieldsCheck(){
        registerAuthentication act = wrap.get();
        boolean check = true;
        final TextInputEditText id = act.findViewById(R.id.nin_field);
        if (id.getTag() == null || !((boolean) id.getTag())){
            TextInputLayout layout = (TextInputLayout) id.getParent().getParent();
            layout.setError("can't be empty");
            id.setTag(false);
            check = false;
        }
        final TextInputEditText account = act.findViewById(R.id.account_field);
        if (account.getTag() == null || !((boolean)account.getTag())){
            TextInputLayout layout = (TextInputLayout) account.getParent().getParent();
            layout.setError("can't be empty");
            account.setTag(false);
            check = false;
        }
        final TextInputEditText withdraw = act.findViewById(R.id.withdrawals_password_field);
        if (withdraw.getTag() == null || !((boolean) withdraw.getTag())){
            TextInputLayout layout = (TextInputLayout) withdraw.getParent().getParent();
            layout.setError("can't be empty");
            withdraw.setTag(false);
            check = false;
        }
        return check;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        registerAuthentication unwrap = wrap.get();
        if (values[0] == 2) {
            unwrap.submit.setText(R.string.submitting);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        registerAuthentication act = wrap.get();
        if (check){
            EditText id = act.findViewById(R.id.nin_field);
            String nin = id.getText().toString();

            EditText account = act.findViewById(R.id.account_field);
            String acc = account.getText().toString();

            EditText withdrawal = act.findViewById(R.id.withdrawals_password_field);
            String withdrawal_pwd = withdrawal.getText().toString();

            byte[] ninBytes =  myIO.toBytes(nin, 18);
            byte[] accBytes = myIO.toBytes(acc, 8);
            byte[] withdrawalBytes = myIO.toBytes(withdrawal_pwd, 6);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            try {
                outputStream.write(ninBytes);
                outputStream.write(accBytes);
                outputStream.write(withdrawalBytes);
                publishProgress(2);
                byte mes[] = outputStream.toByteArray();
                this.runnable = new GeneralRequestService(12, mes);
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


