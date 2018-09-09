package com.example.davychen.mobileBankApp.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.davychen.mobileBankApp.R;
import com.example.davychen.mobileBankApp.fragments.alertDialogFragment;
import com.example.davychen.mobileBankApp.fragments.payeeMaintenance;
import com.example.davychen.mobileBankApp.fragments.transfer;
import com.example.davychen.mobileBankApp.items.account_item;
import com.example.davychen.mobileBankApp.items.payee_item;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

/**
 * adapter used in recycler view to manage payee item
 */
public class payeeItemAdapter extends RecyclerView.Adapter<payeeItemAdapter.viewHolder> {

    /**
     * list used to hold payee_item
     */
    private List<payee_item> lst;

    /**
     * context when adapter is called
     */
    private Context context;

    /**
     * fragment that calls this adapter
     */
    private Fragment frag;

    /**
     * callback listener
     */
    private OnItemClickListener listener;

    /**
     * callback interface
     * interface is implemented in account class
     * This interface brings account item info back from adapter class to currently loaded fragment
     * inside account class
     */
    public interface OnItemClickListener {
        void onItemClick(payee_item item);
    }

    private ArrayList<account_item> filter(List<account_item> lst, String recipient){
        ArrayList<account_item> ret = new ArrayList<>();
        for (account_item each: lst){
            if (!each.getAccount_num().equals(recipient)){
                ret.add(each);
            }
        }
        return ret;
    }
    public payeeItemAdapter(List<payee_item> lst, Context context, Fragment frag, OnItemClickListener listener) {
        this.lst = lst;
        this.context = context;
        this.frag = frag;
        this.listener = listener;

        if (frag instanceof alertDialogFragment){
           lst.add(new payee_item(2)); //linked account section title
           lst.addAll(account_itemToPayeeItem(filter(((transfer)listener).getAccountList(), ((transfer)listener).getFromAccountNum())));
           lst.add(new payee_item(3));// payees section title
        }
    }

    private ArrayList<payee_item> account_itemToPayeeItem(List<account_item> input){
        ArrayList<payee_item> ret = new ArrayList<>();
        for (account_item each : input){
            ret.add(new payee_item(each.getAccount_num(), each.getFirst_name(), each.getLast_name(), each.getBalance()));
        }
        return ret;
    }
    @NonNull
    @Override
    public payeeItemAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.payee_item_layout, parent, false);
        return new payeeItemAdapter.viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        final payee_item accountItem = lst.get(position);
        if (accountItem.getType() == 0){
            holder.firstName.setVisibility(View.VISIBLE);
            holder.lastName.setVisibility(View.VISIBLE);
            holder.balance.setVisibility(View.GONE);
            holder.firstName.setText(accountItem.getFirst_name());
            holder.lastName.setText(accountItem.getLast_name());
            holder.account_num.setText(accountItem.getAccount_num());
            holder.v.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
            holder.v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (frag instanceof alertDialogFragment){
                        listener.onItemClick(accountItem);
                    }
                }
            });
        }else if (accountItem.getType() == 1){
            holder.firstName.setVisibility(View.VISIBLE);
            holder.lastName.setVisibility(View.VISIBLE);
            holder.balance.setVisibility(View.VISIBLE);
            holder.firstName.setText(accountItem.getFirst_name());
            holder.lastName.setText(accountItem.getLast_name());
            holder.account_num.setText(accountItem.getAccount_num());
            holder.balance.setText(String.valueOf(accountItem.getBalance()));
            holder.v.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
            holder.v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (frag instanceof alertDialogFragment){
                        listener.onItemClick(accountItem);
                    }
                }
            });
        }else if (accountItem.getType() == 2){
            holder.firstName.setVisibility(GONE);
            holder.lastName.setVisibility(GONE);
            holder.balance.setVisibility(View.GONE);
            holder.account_num.setText(R.string.linked_account_title);
            holder.v.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.v.setOnClickListener(null);
        }else if (accountItem.getType() == 3){
            holder.firstName.setVisibility(GONE);
            holder.lastName.setVisibility(GONE);
            holder.balance.setVisibility(View.GONE);
            holder.account_num.setText(R.string.payee_title);
            holder.v.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.v.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return lst.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        public View v;
        TextView firstName, lastName, account_num, balance;
        Button edit;
        ImageButton delete;

        public viewHolder(View itemView){
            super(itemView);
            v = itemView;
            this.firstName = itemView.findViewById(R.id.first_name_field);
            this.lastName = itemView.findViewById(R.id.last_name_field);
            this.account_num = itemView.findViewById(R.id.account_num_field);
            this.edit = itemView.findViewById(R.id.edit_button);
            this.delete = itemView.findViewById(R.id.delete_button);
            this.balance = itemView.findViewById(R.id.balance_field);
            if (frag instanceof payeeMaintenance){
                this.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((payeeMaintenance)frag).setDialogEditMode(getAdapterPosition(),
                                account_num.getText().toString(),
                                firstName.getText().toString(),
                                lastName.getText().toString());
                        ((payeeMaintenance)frag).dialog.show();
                    }
                });

                this.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((payeeMaintenance)frag).setDeleteMode(getAdapterPosition());
                    }
                });
                this.balance.setVisibility(GONE);
            }else{
                this.edit.setVisibility(GONE);
                this.delete.setVisibility(GONE);
            }
        }
    }

    public void add(payee_item item){
        this.lst.add(item);
    }

    public void remove(int position){
        lst.remove(position);
    }

    public payee_item getItem(int position){
        return lst.get(position);
    }



}
