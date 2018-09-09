package com.example.davychen.mobileBankApp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.davychen.mobileBankApp.R;
import com.example.davychen.mobileBankApp.items.account_item;

import java.util.List;

/**
 * adapter used in recycler view to manage account item
 */
public class accountItemAdapter extends RecyclerView.Adapter<accountItemAdapter.viewHolder> {

    /**
     * list of account_item
     */
    private List<account_item> lst;

    /**
     * context when adapter is called
     */
    private Context context;

    public accountItemAdapter(List<account_item> lst, Context context) {
        this.lst = lst;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_cardview_layout, parent, false);

        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        account_item accountItem = lst.get(position);

        holder.account_num.setText(accountItem.getAccount_num());
        holder.balance.setText(String.valueOf(accountItem.getBalance()));
        holder.first_name.setText(accountItem.getFirst_name());
        holder.last_name.setText(accountItem.getLast_name());

    }

    @Override
    public int getItemCount() {
        return lst.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View v;
        public TextView account_num, balance, first_name, last_name;
        public ImageButton refresh;

        private viewHolder(View itemView){
            super(itemView);
            v = itemView;
            v.setOnClickListener(this);
            account_num = v.findViewById(R.id.account_num);
            balance = v.findViewById(R.id.balance);
            first_name= v.findViewById(R.id.first_name);
            last_name = v.findViewById(R.id.last_name);
            refresh = v.findViewById(R.id.refresh);

        }

        @Override
        public void onClick(View v) {
            // interface callback
            // brings data back to class implemented it
            ((BottomSheetOnItemClickedListener)context).onItemClicked(
                    account_num.getText().toString(),
                    balance.getText().toString(),
                    first_name.getText().toString(),
                    last_name.getText().toString());

        }
    }

    /**
     * callback interface
     * interface is implemented in account class
     * This interface brings account info including account number, account balance and first and
     * last name of account owner back from above adapter class to currently loaded fragment inside
     * account class
     */
    public interface BottomSheetOnItemClickedListener{
        void onItemClicked(String account, String balance, String first_name, String last_name);
    }
}

