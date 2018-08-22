package com.example.davychen.helloworld.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.davychen.helloworld.R;
import com.example.davychen.helloworld.myIO;
import com.example.davychen.helloworld.services.errDecode;
import com.example.davychen.helloworld.services.passwordReset;

public class password_reset extends AppCompatActivity{

    Button submit;
    String nin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        Intent intent = getIntent();
        nin = intent.getStringExtra("Message");
        final EditText pwd = findViewById(R.id.pwd_field);
        final EditText new_pwd = findViewById(R.id.newpwd_field);
        final EditText confirm_new_pwd = findViewById(R.id.conifrm_newpwd_field);
        submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = true;
                int check_ret;
                if ((check_ret = myIO.pwdChecker(new_pwd.getText().toString())) != 0){
                    check = false;
                    password_reset.this.runOnUiThread(new errDecode(check_ret, password_reset.this));
                }
                if (!new_pwd.getText().toString().equals(confirm_new_pwd.getText().toString())){
                    check = false;
                    Toast.makeText(password_reset.this, "Please confirm entered new password", Toast.LENGTH_SHORT).show();
                }
                if (pwd.getText().toString().equals(new_pwd.getText().toString())){
                    check = false;
                    Toast.makeText(password_reset.this, "no changes to the password made", Toast.LENGTH_SHORT).show();
                }
                if (check){
                    passwordReset run = new passwordReset(0 ,nin, pwd.getText().toString(), confirm_new_pwd.getText().toString(), password_reset.this);
                    passwordResetAsyncTask task = new passwordResetAsyncTask(run);
                    task.execute();
                }
            }
        });
    }
}
class passwordResetAsyncTask extends AsyncTask<Void, Void , Void> {
    passwordReset run;

    passwordResetAsyncTask(passwordReset run){
        this.run = run;

    }
    @Override
    protected void onPreExecute() {
        ((password_reset)run.getAct()).submit.setEnabled(false);
        ((password_reset)run.getAct()).submit.setText(R.string.submitting);
        ((password_reset)run.getAct()).submit.setBackgroundColor(run.getAct().getResources().getColor(android.R.color.darker_gray));
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (run.getErr() == 0){
            ((password_reset)run.getAct()).submit.setText(R.string.success);
            ((password_reset)run.getAct()).submit.setBackgroundResource(android.R.color.holo_green_light);
        }else{
            ((password_reset)run.getAct()).submit.setEnabled(true);
            ((password_reset)run.getAct()).submit.setText(R.string.submit);
            ((password_reset)run.getAct()).submit.setBackgroundResource(android.R.drawable.btn_default);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        this.run.run();
        return null;
    }
}
