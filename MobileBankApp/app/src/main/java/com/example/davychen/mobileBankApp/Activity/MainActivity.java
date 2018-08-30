package com.example.davychen.mobileBankApp.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.davychen.mobileBankApp.R;
import com.example.davychen.mobileBankApp.localHelper;
import com.example.davychen.mobileBankApp.myIO;
import com.example.davychen.mobileBankApp.returnMessage;
import com.example.davychen.mobileBankApp.services.GeneralRequestService;
import com.example.davychen.mobileBankApp.services.LogInService;
import com.example.davychen.mobileBankApp.services.errDecode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * Login activity of this application
 */
public class MainActivity extends AppCompatActivity {

    public static String ip = "192.168.0.104";
    public Button logIn;
    static String TAG = "main_activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.logIn = findViewById(R.id.main_logIn);

        localHelper.autoLogInHelper(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.i(TAG, "onPostResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    /**
     * onClick method of the Sign In button
     * @param v sign in button
     */
    public void onSignInTap(View v){
        Intent signIn = new Intent(MainActivity.this, registerAuthentication.class);
        startActivity(signIn);
    }

    /**
     * This method is for testing purpose
     * user can set Server IP address
     */
    public void setIP(View v){
        EditText text = findViewById(R.id.main_ipField);
        String ip = text.getText().toString();
        if (myIO.validIP(ip)){
            MainActivity.ip = ip;
            Toast invalid = Toast.makeText(getApplicationContext(), "ip set successful", Toast.LENGTH_LONG);
            invalid.show();
        }else{
            Toast invalid = Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_LONG);
            invalid.show();
        }
    }

    /**
     * onClick method of the Login Button
     * @param v log in button
     */
    public void logIn(View v){
        loginAsyncTask task = new loginAsyncTask(this);
        task.execute();

    }

    /**
     * onClick method of login option radio button
     * @param view radio button user clicked
     */
    public void onLoginOptClicked(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        TextView text = findViewById(R.id.main_email);
        EditText num = findViewById(R.id.main_emailField);
        EditText pwd = findViewById(R.id.main_pwdField);
        num.getText().clear();
        pwd.getText().clear();
        int maxLen = 50;
        int inputType = InputType.TYPE_CLASS_TEXT;
        if (view.getId() == R.id.main_emailopt){
            text.setText(R.string.your_email);
        }else if (view.getId() == R.id.main_accopt){
            text.setText(R.string.account_number);
            maxLen = 8;
            inputType = InputType.TYPE_CLASS_NUMBER;
        }
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(maxLen);
        num.setInputType(inputType);
        num.setFilters(fa);
        num.requestFocus();
        if (imm != null) {
            imm.hideSoftInputFromWindow(num.getWindowToken(), 0);
            imm.showSoftInput(num, InputMethodManager.SHOW_FORCED);
        }
    }
}

/**
 * AsyncTask runs when Log in button pressed
 */
class loginAsyncTask extends AsyncTask<Void, Void ,returnMessage> {
    private WeakReference<MainActivity> wrap;
    private boolean check = true;
    private String email;
    private String pwd;
    private char loginOpt;

    loginAsyncTask(MainActivity act){
        this.wrap = new WeakReference<>(act);

    }
    @Override
    protected void onPreExecute() {
        MainActivity context = wrap.get();
        context.logIn.setEnabled(false);
        context.logIn.setText(R.string.submitting);
        pwd = ((EditText)context.findViewById(R.id.main_pwdField)).getText().toString();
        EditText text = context.findViewById(R.id.main_emailField);
        email = text.getText().toString();
        RadioGroup radioGroup = context.findViewById(R.id.main_radiogroup);
        int buttonId = radioGroup.getCheckedRadioButtonId();
        RadioButton button = context.findViewById(buttonId);
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
        if (loginOpt == 0 && !myIO.isValidEmail(email)){ //email login and invalid email
            Toast.makeText(context, "Invalid Email", Toast.LENGTH_SHORT).show();
            check = false;
        }else if (loginOpt == 1 && !myIO.isAccountNumber(email)){ //account login and invalid account
            Toast.makeText(context, "Invalid Account Number", Toast.LENGTH_SHORT).show();
            check = false;
        }
    }

    @Override
    protected void onPostExecute(returnMessage ret) {
        MainActivity context = wrap.get();
        if (ret != null){ // success and destroy mainActivity
            if (ret.getRet() == 0){
                Intent account = new Intent(context, account.class);
                account.putExtra("Message",ret.getMessage());
                context.startActivity(account);
                context.finish();
            }else{
                new errDecode(ret.getRet(), context).run();
                context.logIn.setEnabled(true);
                context.logIn.setText(R.string.submit);
            }
        }else{
            context.logIn.setEnabled(true);
            context.logIn.setText(R.string.submit);
        }
    }

    @Override
    protected returnMessage doInBackground(Void... voids) {
        try {
            if (check){
                byte[] emailBytes = myIO.toBytes(email, 50);
                byte[] pwdBytes = myIO.toBytes(pwd, 20);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(loginOpt);
                outputStream.write(emailBytes);
                outputStream.write(pwdBytes);
                byte msg[] = outputStream.toByteArray();
                return new GeneralRequestService(2, msg).call();
            }
        } catch (IOException e) {
            return new returnMessage(myIO.CLIENT_ERROR);
        }
        return null;
    }
}
