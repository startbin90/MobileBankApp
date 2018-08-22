package com.example.davychen.helloworld.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.davychen.helloworld.R;
import com.example.davychen.helloworld.services.transactionService;



public class transactionConfirmationPage extends AppCompatActivity {
    public TextView from_field;

    public TextView payee_field;

    public TextView first_name_field;

    public TextView last_name_field;

    public TextView value_field;

    public TextView memo_field;

    public EditText trans_pwd;

    public String from;
    public String payee;
    public String fName;
    public String lName;
    public float value;
    public String memo;
    public Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_confirmation_page);
        setTitle("Transaction Confirmation");
        Intent intent = getIntent();
        from = intent.getStringExtra("from");
        payee = intent.getStringExtra("payee");
        fName = intent.getStringExtra("firstName");
        lName= intent.getStringExtra("lastName");
        value = intent.getFloatExtra("value", 0);
        memo = intent.getStringExtra("memo");

        from_field = findViewById(R.id.transaction_from_field);
        payee_field = findViewById(R.id.transaction_to_field);
        first_name_field = findViewById(R.id.transaction_first_name_field);
        last_name_field = findViewById(R.id.transaction_last_name_field);
        value_field = findViewById(R.id.transaction_value_field);
        memo_field = findViewById(R.id.memo_field);
        trans_pwd = findViewById(R.id.trans_pwd_field);

        from_field.setText(from);
        payee_field.setText(payee);
        first_name_field.setText(fName);
        last_name_field.setText(lName);
        value_field.setText(String.valueOf(value));
        memo_field.setText(memo);


        submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionService run = new transactionService(transactionConfirmationPage.this);

                transactionAsyncTask task = new transactionAsyncTask(run);
                task.execute();
            }
        });

    }

}
class transactionAsyncTask extends AsyncTask<Void, Void , Void> {
    transactionService run;
    //WeakReference<transactionConfirmationPage> con;

    transactionAsyncTask(transactionService run){
        this.run = run;
        //this.con = new WeakReference<>(con);

    }
    @Override
    protected void onPreExecute() {
        run.getAct().submit.setEnabled(false);
        run.getAct().submit.setText(R.string.submitting);
        run.getAct().submit.setBackgroundColor(run.getAct().getResources().getColor(android.R.color.darker_gray));
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (run.getErr() == 0){
            run.getAct().submit.setText(R.string.success);
            run.getAct().submit.setBackgroundResource(android.R.color.holo_green_light);
        }else{
            run.getAct().submit.setEnabled(true);
            run.getAct().submit.setText(R.string.submit);
            run.getAct().submit.setBackgroundResource(android.R.drawable.btn_default);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        this.run.run();
        return null;
    }
}

