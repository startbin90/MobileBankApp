package com.example.davychen.mobileBankApp.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.davychen.mobileBankApp.R;
import com.example.davychen.mobileBankApp.Activity.account;
import com.example.davychen.mobileBankApp.myIO;
import com.example.davychen.mobileBankApp.services.accountAdditionService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link accountAdditionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class accountAdditionFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    public account parentAct;
    private View view;
    public EditText account;
    public EditText withdrawalPwd;
    public EditText nin;
    public Button submit;

    public static accountAdditionFragment newInstance(account act){
        accountAdditionFragment instance = new accountAdditionFragment();
        instance.parentAct = act;
        return instance;
    }
    public accountAdditionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.activity_register_authentication, container, false);
        submit = this.view.findViewById(R.id.validate);

        TextInputLayout nin = this.view.findViewById(R.id.nin_layout);
        nin.setVisibility(View.GONE);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clear current focus and request focus on the layout in order to trigger
                //OnFocusChanged callback on the last edited item.
                LinearLayout root_layout = accountAdditionFragment.this.view.findViewById(R.id.registerAuth_layout);
                if (parentAct.getCurrentFocus() != null){
                    parentAct.getCurrentFocus().clearFocus();
                    root_layout.requestFocus();
                }

                boolean check = true;
                EditText[] arr = new EditText[2];
                accountAdditionFragment.this.account = view.findViewById(R.id.account_field);
                accountAdditionFragment.this.withdrawalPwd = view.findViewById(R.id.withdrawals_password_field);
                arr[0] = account;
                arr[1] = withdrawalPwd;
                for (EditText each: arr){
                    boolean ret = fieldsChecker(each);
                    if (!ret)
                        check = false;
                }
                if (check){
                    accountAdditionService run = new accountAdditionService(accountAdditionFragment.this);

                    accountAdditionAsyncTask task = new  accountAdditionAsyncTask(run);
                    task.execute();
                }
            }
        });


        return this.view;
    }

    public boolean fieldsChecker(View v){
        TextInputLayout layout = (TextInputLayout)v.getParent().getParent();
        int itemId = v.getId();
        switch (itemId){
            case R.id.account_field:
                String account = ((TextInputEditText)v).getText().toString();
                if (myIO.isAccountNumber(account)){
                    layout.setError(null);
                    return true;
                }else{
                    layout.setError("Invalid account number");
                    return false;
                }
            default:
                if(((EditText)v).getText().toString().isEmpty()){
                    layout.setError("Don't leave this empty");
                    return false;
                }else{
                    layout.setError(null);
                    return true;
                }
        }

    }

    /*// TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        /*mListener = null;*/
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Account Addition");
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
class accountAdditionAsyncTask extends AsyncTask<Void, Void , Void> {
    accountAdditionService run;

    accountAdditionAsyncTask(accountAdditionService run){
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
            run.getFrag().submit.setText(R.string.validate);
            run.getFrag().submit.setBackgroundResource(android.R.drawable.btn_default);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        this.run.run();
        return null;
    }
}
