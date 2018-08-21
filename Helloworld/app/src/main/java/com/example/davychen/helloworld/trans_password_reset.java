package com.example.davychen.helloworld;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.davychen.helloworld.services.passwordReset;

public class trans_password_reset extends AppCompatActivity{

    Button submit;
    String nin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans_password_reset);
        Intent intent = getIntent();
        nin = intent.getStringExtra("Message");
        final EditText trans_pwd = findViewById(R.id.trans_pwd_field);
        final EditText new_pwd = findViewById(R.id.newpwd_field);
        final EditText confirm_new_pwd = findViewById(R.id.conifrm_newpwd_field);
        submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = true;
                if (!new_pwd.getText().toString().equals(confirm_new_pwd.getText().toString())){
                    check = false;
                    Toast.makeText(trans_password_reset.this, "Please confirm entered new password", Toast.LENGTH_SHORT).show();
                }
                if (trans_pwd.getText().toString().equals(new_pwd.getText().toString())){
                    check = false;
                    Toast.makeText(trans_password_reset.this, "no changes to the password made", Toast.LENGTH_SHORT).show();
                }
                if (check){
                    passwordReset run = new passwordReset(1, nin, trans_pwd.getText().toString(), confirm_new_pwd.getText().toString(), trans_password_reset.this);
                    TransPasswordResetAsyncTask task = new TransPasswordResetAsyncTask(run);
                    task.execute();
                }
            }
        });
    }
}
class TransPasswordResetAsyncTask extends AsyncTask<Void, Void , Void> {
    passwordReset run;

    TransPasswordResetAsyncTask(passwordReset run){
        this.run = run;

    }
    @Override
    protected void onPreExecute() {
        ((trans_password_reset)run.getAct()).submit.setEnabled(false);
        ((trans_password_reset)run.getAct()).submit.setText(R.string.submitting);
        ((trans_password_reset)run.getAct()).submit.setBackgroundColor(run.getAct().getResources().getColor(android.R.color.darker_gray));
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (run.getErr() == 0){
            ((trans_password_reset)run.getAct()).submit.setText(R.string.success);
            ((trans_password_reset)run.getAct()).submit.setBackgroundResource(android.R.color.holo_green_light);
        }else{
            ((trans_password_reset)run.getAct()).submit.setEnabled(true);
            ((trans_password_reset)run.getAct()).submit.setText(R.string.submit);
            ((trans_password_reset)run.getAct()).submit.setBackgroundResource(android.R.drawable.btn_default);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        this.run.run();
        return null;
    }
}

