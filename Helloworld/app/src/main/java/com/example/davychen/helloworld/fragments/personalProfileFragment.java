package com.example.davychen.helloworld.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.example.davychen.helloworld.Activity.account;
import com.example.davychen.helloworld.myIO;
import com.example.davychen.helloworld.returnMessage;
import com.example.davychen.helloworld.services.GeneralRequestService;
import com.example.davychen.helloworld.services.errDecode;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class personalProfileFragment extends Fragment {

    public account parentAct;
    View view;
    AlertDialog dialog;
    View dialogView;
    Button submit;

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

                        profileModificationAsyncTask task = new profileModificationAsyncTask(
                                personalProfileFragment.this);
                        task.execute();
                    }
                }else{
                    Toast.makeText(parentAct, "no changes made", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Personal Profile");
    }


}
class profileModificationAsyncTask extends AsyncTask<Void, Void, returnMessage> {
    private WeakReference<personalProfileFragment> wrap;
    //private returnMessage retMsg;

    profileModificationAsyncTask(personalProfileFragment frag){
        this.wrap = new WeakReference<>(frag);

    }
    @Override
    protected void onPreExecute() {
        personalProfileFragment context = wrap.get();
        context.submit.setEnabled(false);
        context.submit.setText(R.string.submitting);
        context.submit.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
    }

    @Override
    protected void onPostExecute(returnMessage ret){
        personalProfileFragment context = wrap.get();

        if (ret.getRet() == 0){
            TextView email = context.view.findViewById(R.id.email);
            TextView cell = context.view.findViewById(R.id.cell);
            TextView addr = context.view.findViewById(R.id.address);
            EditText new_email = context.dialogView.findViewById(R.id.email_field);
            EditText new_cell = context.dialogView.findViewById(R.id.cell_field);
            EditText new_addr = context.dialogView.findViewById(R.id.address_field);
            email.setText(new_email.getText().toString());
            cell.setText(new_cell.getText().toString());
            addr.setText(new_addr.getText().toString());
            context.submit.setText(R.string.success);
            context.submit.setBackgroundResource(android.R.color.holo_green_light);
        }else{
            context.submit.setEnabled(true);
            context.submit.setText(R.string.submit);
            context.submit.setBackgroundResource(android.R.drawable.btn_default);
            context.parentAct.runOnUiThread(new errDecode(ret.getRet(), context.parentAct));
        }
    }

    @Override
    protected returnMessage doInBackground(Void... voids) {
        personalProfileFragment context = wrap.get();
        final EditText email = context.dialogView.findViewById(R.id.email_field);
        final EditText cell = context.dialogView.findViewById(R.id.cell_field);
        final EditText addr = context.dialogView.findViewById(R.id.address_field);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(myIO.toBytes(email.getText().toString(), 50));
            outputStream.write(myIO.toBytes(cell.getText().toString(), 15));
            outputStream.write(myIO.toBytes(addr.getText().toString(), 100));
            outputStream.write(myIO.toBytes(context.parentAct.nin, 18));
            byte msg[] = outputStream.toByteArray();
            return new GeneralRequestService(5, msg).call();
        } catch (Exception e) {
            e.printStackTrace();
            return new returnMessage(-2);
        }

    }
}
