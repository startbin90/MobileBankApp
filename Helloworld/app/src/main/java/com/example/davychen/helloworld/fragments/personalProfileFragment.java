package com.example.davychen.helloworld.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.davychen.helloworld.R;
import com.example.davychen.helloworld.account;
import com.example.davychen.helloworld.myIO;
import com.example.davychen.helloworld.services.personalProfileModification;

/**
 * A simple {@link Fragment} subclass.
 */
public class personalProfileFragment extends Fragment {

    public account parentAct;
    View view;
    AlertDialog dialog;
    View dialogView;
    Button submit;
    Button edit;

    public static personalProfileFragment newInstance(account act){
        personalProfileFragment instance = new personalProfileFragment();
        instance.parentAct = act;
        return instance;
    }

    public personalProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_personal_profile, container, false);
        TextView nick_name = view.findViewById(R.id.nick_name);
        TextView email = view.findViewById(R.id.email);
        TextView cell = view.findViewById(R.id.cell);
        TextView addr = view.findViewById(R.id.address);
        nick_name.setText(parentAct.nick_name);
        email.setText(parentAct.email);
        cell.setText(parentAct.cell);
        addr.setText(parentAct.addr);

        Button edit = view.findViewById(R.id.edit_button);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogViewSetup();
                dialog.show();
            }
        });
        this.dialog = createAlertDialog();
        return this.view;
    }
    private android.support.v7.app.AlertDialog createAlertDialog(){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(parentAct);
        this.dialogView= getLayoutInflater().inflate(R.layout.modify_personal_profile_layout, null);
        this.submit = dialogView.findViewById(R.id.submit);
        builder.setView(this.dialogView);
        return builder.create();

    }

    private void dialogViewSetup(){
        final EditText email = this.dialogView.findViewById(R.id.email_field);
        final EditText cell = this.dialogView.findViewById(R.id.cell_field);
        final EditText addr = this.dialogView.findViewById(R.id.address_field);
        email.setText(parentAct.email);
        cell.setText(parentAct.cell);
        addr.setText(parentAct.addr);
        this.submit = this.dialogView.findViewById(R.id.submit);
        this.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!email.getText().toString().equals(parentAct.email) ||
                        !cell.getText().toString().equals(parentAct.cell) ||
                        !addr.getText().toString().equals(parentAct.addr)){
                    if (myIO.isValidEmail(email.getText().toString())){

                        personalProfileModification run = new personalProfileModification(personalProfileFragment.this,
                                email.getText().toString(),
                                cell.getText().toString(), addr.getText().toString(), parentAct.nin);
                        profileModificationAsyncTask task = new  profileModificationAsyncTask(run);
                        task.execute();
                    }
                }else{
                    Toast.makeText(parentAct, "no changes made", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Personal Profile");
    }


}
class profileModificationAsyncTask extends AsyncTask<Void, Void , Void> {
    personalProfileModification run;

    profileModificationAsyncTask(personalProfileModification run){
        this.run = run;

    }
    @Override
    protected void onPreExecute() {
        run.getFrag().submit.setEnabled(false);
        run.getFrag().submit.setText(R.string.submitting);
        run.getFrag().submit.setBackgroundColor(run.getFrag().getResources().getColor(android.R.color.darker_gray));
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (run.getErr() == 0){
            run.getFrag().submit.setText(R.string.success);
            run.getFrag().submit.setBackgroundResource(android.R.color.holo_green_light);
        }else{
            run.getFrag().submit.setEnabled(true);
            run.getFrag().submit.setText(R.string.submit);
            run.getFrag().submit.setBackgroundResource(android.R.drawable.btn_default);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        this.run.run();
        return null;
    }
}
