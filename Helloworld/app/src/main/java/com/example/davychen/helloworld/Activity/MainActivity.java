package com.example.davychen.helloworld.Activity;

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
import android.widget.TextView;
import android.widget.Toast;

import com.example.davychen.helloworld.R;
import com.example.davychen.helloworld.localHelper;
import com.example.davychen.helloworld.myIO;
import com.example.davychen.helloworld.services.LogInService;

public class MainActivity extends AppCompatActivity {

    public static String ip = "192.168.0.106";
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

    public void onSignInTap(View v){
        Intent signIn = new Intent(MainActivity.this, registerAuthentication.class);
        startActivity(signIn);
    }

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

    public void logIn(View v){
        LogInService run = new LogInService(this);
        loginAsyncTask task = new loginAsyncTask(run);
        task.execute();

    }

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
class loginAsyncTask extends AsyncTask<Void, Void , Void> {
    LogInService run;

    loginAsyncTask(LogInService run){
        this.run = run;

    }
    @Override
    protected void onPreExecute() {
        run.getParentAct().logIn.setEnabled(false);
        run.getParentAct().logIn.setText(R.string.submitting);
        //run.getParentAct().logIn.setBackgroundColor(run.getParentAct().getResources().getColor(android.R.color.darker_gray));
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (run.getErr() == 0){ // success and destroy mainActivity
            Intent account = new Intent(run.getParentAct(), account.class);
            account.putExtra("Message",run.getMsg());
            run.getParentAct().startActivity(account);
            run.getParentAct().finish();
        }else{
            run.getParentAct().logIn.setEnabled(true);
            run.getParentAct().logIn.setText(R.string.submit);
            //run.getParentAct().logIn.setBackgroundResource(android.R.drawable.btn_default);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        this.run.run();
        return null;
    }
}
