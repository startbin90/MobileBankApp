package com.example.davychen.helloworld.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.davychen.helloworld.R;
import com.example.davychen.helloworld.Activity.account;
import com.example.davychen.helloworld.adapters.payeeItemAdapter;
import com.example.davychen.helloworld.items.account_item;
import com.example.davychen.helloworld.items.payee_item;
import com.example.davychen.helloworld.myIO;
import com.example.davychen.helloworld.Activity.transactionConfirmationPage;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class transfer extends Fragment implements payeeItemAdapter.OnItemClickListener, View.OnFocusChangeListener{
    private account parentAct;
    private List<account_item> accountList;
    private alertDialogFragment dialog = null;
    private View mView;
    private account_item from_selected = null;
    private static String TAG = "transfer";
    //private static int accountNumberLength = 8;

    public transfer() {
        // Required empty public constructor
    }

    public static transfer newInstance(account act) {
        transfer f = new transfer();
        f.parentAct = act;
        f.accountList = act.itemLst;
        return f;
    }

    @Override
    public void onItemClick(payee_item item) {
        if (dialog != null)
        dialog.dismiss();
        EditText recipient = mView.findViewById(R.id.recipient_field);
        recipient.setText(item.getAccount_num());
        if (item.getType() == 0) {
            EditText first_name = mView.findViewById(R.id.first_name_field);
            first_name.setText(item.getFirst_name());

            EditText last_name = mView.findViewById(R.id.last_name_field);
            last_name.setText(item.getLast_name());

        }else if (item.getType() == 1){
            EditText first_name = mView.findViewById(R.id.first_name_field);
            first_name.setText(item.getFirst_name());

            EditText last_name = mView.findViewById(R.id.last_name_field);
            last_name.setText(item.getLast_name());


        }
    }
    class fromSelectOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            FragmentManager manager = getFragmentManager();
            BottomSheetDialogFragment bottomSheet = accounts_list.newInstance(parentAct);
            assert manager != null: "getFragmentManager failed, get null object instead";
            bottomSheet.show(manager, "payment_account_select_dialog");
        }
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);
        this.mView = view;

        Button but = view.findViewById(R.id.select);
        but.setOnClickListener(new fromSelectOnClickListener());
        View included_select = view.findViewById(R.id.included_select);
        included_select.setOnClickListener(new fromSelectOnClickListener());

        ImageButton payee_select = view.findViewById(R.id.payee_select);
        payee_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                dialog = alertDialogFragment.newInstance(parentAct, transfer.this);
                assert manager != null : "getFragmentManager failed, get null object instead";
                dialog.show(manager, "payee_select_dialog");
            }
        });

        EditText value = mView.findViewById(R.id.value_field);
        value.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2)});


        Button next = view.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clear current focus and request focus on the layout in order to trigger
                //OnFocusChanged callback on the last edited item.
                TableLayout root_layout = mView.findViewById(R.id.table_layout);
                if (parentAct.getCurrentFocus() != null){
                    parentAct.getCurrentFocus().clearFocus();
                    root_layout.requestFocus();
                }
                EditText[] arr = new EditText[4];
                EditText payee = mView.findViewById(R.id.recipient_field);
                EditText fName = mView.findViewById(R.id.first_name_field);
                EditText lName = mView.findViewById(R.id.last_name_field);
                EditText value = mView.findViewById(R.id.value_field);

                boolean check = true;
                if (from_selected == null){
                    check = false;
                    String errMsg = "Please select a payment account";
                    Toast.makeText(parentAct, errMsg, Toast.LENGTH_SHORT).show();
                }

                arr[0] = payee;
                arr[1] = fName;
                arr[2] = lName;
                arr[3] = value;
                for (EditText each: arr){
                    boolean ret = fieldsChecker(each);
                    if (!ret)
                        check = false;
                }
                EditText memo = mView.findViewById(R.id.memo_field);

                if (check){
                    Intent signIn = new Intent(parentAct, transactionConfirmationPage.class);
                    signIn.putExtra("from", from_selected.getAccount_num());
                    signIn.putExtra("payee", payee.getText().toString());
                    signIn.putExtra("firstName", fName.getText().toString());
                    signIn.putExtra("lastName", lName.getText().toString());
                    signIn.putExtra("value", Float.parseFloat(value.getText().toString()));
                    signIn.putExtra("memo", memo.getText().toString());
                    startActivity(signIn);
                }

            }
        });

        final TextInputEditText recipient = mView.findViewById(R.id.recipient_field);
        recipient.setOnFocusChangeListener(this);
        final TextInputEditText first_name = mView.findViewById(R.id.first_name_field);
        first_name.setOnFocusChangeListener(this);
        final TextInputEditText last_name = mView.findViewById(R.id.last_name_field);
        last_name.setOnFocusChangeListener(this);
        final TextInputEditText value_field = mView.findViewById(R.id.value_field);
        value_field.setOnFocusChangeListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        assert getActivity() != null: "getActivity failed, get null object instead";
        getActivity().setTitle("Transfer");
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int itemId = v.getId();
        if (!hasFocus){
            fieldsChecker(v);
        }else{
            switch (itemId){
                case R.id.recipient_field:
                    TextInputEditText first_name = parentAct.findViewById(R.id.first_name_field);
                    first_name.setText("");
                    TextInputEditText last_name = parentAct.findViewById(R.id.last_name_field);
                    last_name.setText("");
                    break;
            }
        }
    }

    public boolean fieldsChecker(View v){
        TextInputLayout layout = (TextInputLayout)v.getParent().getParent();
        int itemId = v.getId();
        switch (itemId){
            case R.id.recipient_field:
                String account = ((TextInputEditText)v).getText().toString();
                if (myIO.isAccountNumber(account)){
                    if (from_selected != null){
                        if (account.equals(from_selected.getAccount_num())){
                            layout.setError("Do not support transfer in the same account");
                            return false;
                        }
                    }else{
                        layout.setError(null);
                        return true;
                    }

                }else{
                    layout.setError("Invalid account number");
                    return false;
                }
            case R.id.value_field:
                String value = ((TextInputEditText)v).getText().toString();
                if (value.length() == 0){
                    layout.setError("Cannot be empty");
                    return false;
                }else {
                    try {
                        float result = Float.parseFloat(value);
                        if (result <= 0){
                            layout.setError("Must be positive amount");
                            return false;
                        }
                        layout.setError(null);
                        return true;
                    } catch (NumberFormatException e) {
                        layout.setError("Invalid amount");
                        return false;
                    }
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

    public class DecimalDigitsInputFilter implements InputFilter {

        private final int decimalDigits;

        /**
         * Constructor.
         *
         * @param decimalDigits maximum decimal digits
         */
        private DecimalDigitsInputFilter(int decimalDigits) {
            this.decimalDigits = decimalDigits;
        }

        @Override
        public CharSequence filter(CharSequence source,
                                   int start,
                                   int end,
                                   Spanned dest,
                                   int dstart,
                                   int dend) {


            int dotPos = -1;
            int len = dest.length();
            for (int i = 0; i < len; i++) {
                char c = dest.charAt(i);
                if (c == '.' || c == ',') {
                    dotPos = i;
                    break;
                }
            }
            if (dotPos >= 0) {

                // protects against many dots
                if (source.equals(".") || source.equals(","))
                {
                    return "";
                }
                // if the text is entered before the dot
                if (dend <= dotPos) {
                    return null;
                }
                if (len - dotPos > decimalDigits) {
                    return "";
                }
            }

            return null;
        }

    }

    public List<account_item> getAccountList() {
        return accountList;
    }

    public String getFromAccountNum() {
        if (from_selected != null)
            return from_selected.getAccount_num();
        return null;
    }

    public void onPaymentAccountSelected(account_item item){
        if (getFragmentManager() != null) {
            BottomSheetDialogFragment frag = (BottomSheetDialogFragment) getFragmentManager().findFragmentByTag("payment_account_select_dialog");
            if (frag != null){
                frag.dismiss();
            }else{
                Log.i(TAG, "BottomSheetDialogFragment not found");
            }
        }
        from_selected = item;
        Button select = mView.findViewById(R.id.select);
        select.setVisibility(View.GONE);
        CardView included_select = mView.findViewById(R.id.included_select);
        included_select.setVisibility(View.VISIBLE);
        TextView account = included_select.findViewById(R.id.account_num);
        account.setText(item.getAccount_num());

        TextView balance = included_select.findViewById(R.id.balance);
        balance.setText(String.valueOf(item.getBalance()));

        TextView first_name = included_select.findViewById(R.id.first_name);
        first_name.setText(item.getFirst_name());

        TextView last_name = included_select.findViewById(R.id.last_name);
        last_name.setText(item.getLast_name());

        EditText payee = mView.findViewById(R.id.recipient_field);
        EditText fName = mView.findViewById(R.id.first_name_field);
        EditText lName = mView.findViewById(R.id.last_name_field);
        if (item.getAccount_num().equals(payee.getText().toString())){
            payee.setText("");
            fName.setText("");
            lName.setText("");
        }
    }
}

