package com.example.davychen.mobileBankApp.fragments;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.davychen.mobileBankApp.R;
import com.example.davychen.mobileBankApp.Activity.account;
import com.example.davychen.mobileBankApp.adapters.payeeItemAdapter;
import com.example.davychen.mobileBankApp.items.payee_item;
import com.example.davychen.mobileBankApp.services.getRecipients;
import com.example.davychen.mobileBankApp.services.modifyPayees;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class payeeMaintenance extends Fragment implements payeeMaintenanceInterface{

    private account parentAct;
    private ArrayList<payee_item> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private payeeItemAdapter adapter;
    private SwipeRefreshLayout mRefreshLayout;
    private Button addNewPayee;
    private static int accountNumberLength = 8;
    private View mView;
    public Button submit;
    public AlertDialog dialog;

    public payeeMaintenance() {
        // Required empty public constructor
    }

    public static payeeMaintenance newInstance(account act) {
        payeeMaintenance f = new payeeMaintenance();
        f.parentAct = act;
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_payee_maintenance, container, false);
        this.recyclerView = v.findViewById(R.id.payeeRecyclerView);

        this.recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.adapter = new payeeItemAdapter(this.list, getContext(), payeeMaintenance.this, null);
        this.recyclerView.setAdapter(adapter);

        this.mRefreshLayout = v.findViewById(R.id.payeeSwipeRefreshLayout);
        mRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary
                , R.color.green
                , R.color.dividerGrey
                , R.color.colorAccent);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                list.clear();
                new Thread(new getRecipients(payeeMaintenance.this)).start();
            }
        });

        this.addNewPayee= v.findViewById(R.id.addNewPayeeButton);
        this.addNewPayee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialogAddNewMode();
                dialog.show();
            }
        });
        this.dialog = createAlertDialog();

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Payees Maintenance");
        new Thread(new getRecipients(payeeMaintenance.this)).start();
    }

    public void setDeleteMode(final int position){
        AlertDialog alertDialog = new AlertDialog.Builder(parentAct).create();
        alertDialog.setMessage("Are you sure to remove this payee?");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        modifyPayees run = new modifyPayees(adapter, parentAct);
                        run.setDeleteMode(position);

                        payeeMaintenanceAsyncTask task = new payeeMaintenanceAsyncTask(payeeMaintenance.this);
                        modifyPayees[] arr = {run};
                        task.execute(arr);
                    }
                });
        alertDialog.show();
    }
    public void setDialogEditMode(final int position, final String account, final String fName, final String lName){
        final EditText account_num = mView.findViewById(R.id.account_num_field);
        account_num.setText(account);
        account_num.requestFocus();
        final EditText first_name = mView.findViewById(R.id.first_name_field);
        first_name.setText(fName);
        final EditText last_name = mView.findViewById(R.id.last_name_field);
        last_name.setText(lName);

        Button cancel = mView.findViewById(R.id.cancel);
        final TextView accountChecker = mView.findViewById(R.id.accountChecker);
        final TextView fieldsChecker= mView.findViewById(R.id.fieldsChecker);
        fieldsChecker.setText("");
        accountChecker.setText("");
        submit.setText(R.string.submit);
        submit.setBackgroundResource(android.R.drawable.btn_default);
        account_num.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    if (account_num.getText().length() != accountNumberLength){
                        accountChecker.setText(R.string.short_account_num);
                        accountChecker.setTextColor(getResources().getColor(R.color.colorAccent));
                    }
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account_num.getText().length() != accountNumberLength ||
                        first_name.getText().length() == 0 || last_name.getText().length() == 0){
                    fieldsChecker.setTextColor(getResources().getColor(R.color.colorAccent));
                    fieldsChecker.setText(R.string.empty_names_fields);
                }else if (account_num.getText().toString().equals(account) &&
                        first_name.getText().toString().equals(fName) &&
                        last_name.getText().toString().equals(lName)){
                    fieldsChecker.setTextColor(getResources().getColor(R.color.colorAccent));
                    fieldsChecker.setText(R.string.no_modification);
                }else{
                    modifyPayees run = new modifyPayees(adapter, parentAct);
                    run.setEditMode(position, account_num.getText().toString(), first_name.getText().toString(), last_name.getText().toString());

                    payeeMaintenanceAsyncTask task = new payeeMaintenanceAsyncTask(payeeMaintenance.this);
                    modifyPayees[] arr = {run};
                    task.execute(arr);
                }
            }
        });



        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
    }

    public void setDialogAddNewMode(){
        final EditText account_num = mView.findViewById(R.id.account_num_field);
        account_num.getText().clear();
        account_num.requestFocus();
        final EditText first_name = mView.findViewById(R.id.first_name_field);
        first_name.getText().clear();
        final EditText last_name = mView.findViewById(R.id.last_name_field);
        last_name.getText().clear();

        Button cancel = mView.findViewById(R.id.cancel);
        final TextView accountChecker = mView.findViewById(R.id.accountChecker);
        final TextView fieldsChecker= mView.findViewById(R.id.fieldsChecker);
        fieldsChecker.setText("");
        accountChecker.setText("");
        submit.setText(R.string.submit);
        submit.setBackgroundResource(android.R.drawable.btn_default);
        account_num.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    if (account_num.getText().length() != accountNumberLength){
                        accountChecker.setText(R.string.short_account_num);
                        accountChecker.setTextColor(getResources().getColor(R.color.colorAccent));
                    }else{
                        if (dupAccountChecker(account_num.getText().toString())){
                            accountChecker.setText(R.string.dup_account_num);
                            accountChecker.setTextColor(getResources().getColor(R.color.colorAccent));
                        }else{
                            accountChecker.setText("");
                        }
                    }
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account_num.getText().length() != accountNumberLength ||
                        first_name.getText().length() == 0 ||
                        last_name.getText().length() == 0){
                    fieldsChecker.setTextColor(getResources().getColor(R.color.colorAccent));
                    fieldsChecker.setText(R.string.empty_names_fields);
                }else{
                    modifyPayees run = new modifyPayees(adapter, parentAct);
                    run.setAddNewMode(account_num.getText().toString(), first_name.getText().toString(), last_name.getText().toString());

                    payeeMaintenanceAsyncTask task = new payeeMaintenanceAsyncTask(payeeMaintenance.this);
                    modifyPayees[] arr = {run};
                    task.execute(arr);
                }
            }
        });



        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
    }
    public AlertDialog createAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(parentAct);
        this.mView = getLayoutInflater().inflate(R.layout.add_new_payee_layout, null);
        this.submit = mView.findViewById(R.id.next);
        builder.setView(mView);
        return builder.create();

    }

    private boolean dupAccountChecker(String account){
        for (payee_item each: list){
            if (each.getAccount_num().equals(account)){
                return true; //has duplicates
            }

        }
        return false; //no dup
    }

    @Override
    public account getParentAct() {
        return this.parentAct;
    }

    @Override
    public ArrayList<payee_item> getList() {
        return this.list;
    }

    public payeeItemAdapter getAdapter() {
        return adapter;
    }

    public SwipeRefreshLayout getRefreshLayout() {
        return mRefreshLayout;
    }
}
class payeeMaintenanceAsyncTask extends AsyncTask<modifyPayees, Void , Void>{
    modifyPayees run;
    WeakReference<payeeMaintenance> con;

    payeeMaintenanceAsyncTask(payeeMaintenance con){
        this.con = new WeakReference<>(con);

    }
    @Override
    protected void onPreExecute() {
        con.get().submit.setText(R.string.submitting);
        con.get().submit.setBackgroundColor(con.get().getResources().getColor(android.R.color.darker_gray));
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (run.getErr() == 0){
            con.get().dialog.dismiss();
        }else{
            con.get().submit.setText(R.string.submit);
            con.get().submit.setBackgroundResource(android.R.drawable.btn_default);
        }
    }

    @Override
    protected Void doInBackground(modifyPayees... voids) {
        this.run = voids[0];
        voids[0].run();
        return null;
    }
}

