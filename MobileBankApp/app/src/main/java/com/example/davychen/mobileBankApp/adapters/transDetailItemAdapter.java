package com.example.davychen.mobileBankApp.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.davychen.mobileBankApp.R;
import com.example.davychen.mobileBankApp.Activity.*;
import com.example.davychen.mobileBankApp.items.transaction_detail_item;
import com.example.davychen.mobileBankApp.services.transDetailService;

import java.util.Calendar;
import java.util.List;

public class transDetailItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<transaction_detail_item> lst;
    private Context context;
    private RecyclerView mRecyclerView;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    //for endless recyclerView.
    // preventing potentially unnecessary scrolling down
    private boolean isLoading = false;

    private int visibleThreshold = 1;
    private boolean moreToBeLoad = true;
    private static String TAG = "transDetailItemAdapter";


    public transDetailItemAdapter(List<transaction_detail_item> lst, final Context context) {
        this.lst = lst;
        this.context = context;
        this.mRecyclerView = ((account_detail) context).recyclerView;
        final LinearLayoutManager layoutManager = (LinearLayoutManager) this.mRecyclerView.getLayoutManager();

        this.mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, "Changed state = " + newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Log.d(TAG, "item count " + layoutManager.getItemCount());
                Log.d(TAG, "lastvisibleitem  " + layoutManager.findLastVisibleItemPosition());
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (moreToBeLoad){
                        Log.d(TAG,  "is loading more");
                        Runnable service = new transDetailService((account_detail) context);
                        ((account_detail) context).currentRunner = service;
                        new Thread(service).start();
                    }else{
                        Log.d(TAG, "no more to be load");
                    }
                }

            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.load_bar_layout, parent, false);
            return new footViewHolder(view);
        }else{ //viewType == TYPE_ITEM
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.item_listview_layout, parent, false);
            return new viewHolder(v);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof viewHolder) {
            transaction_detail_item detailItem = lst.get(position);

            long date = detailItem.getTrans_date();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(date);

            String time = DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_YEAR);

            ((viewHolder)holder).date.setText(time);
            ((viewHolder)holder).recipient.setText(detailItem.getTrans_to());
            if (detailItem.trans_dir == '+'){
                ((viewHolder)holder).value.setTextColor(context.getResources().getColor(R.color.green));
                ((viewHolder)holder).value.setText(String.valueOf(detailItem.getTrans_value()));
            }else{
                String text = String.format(context.getResources().getString(R.string.negative_trans_value), String.valueOf(detailItem.getTrans_value()));
                ((viewHolder)holder).value.setText(text);
            }
            ((viewHolder)holder).balance.setText(String.valueOf(detailItem.getTrans_post_balance()));
        }else{
            if (!moreToBeLoad){
                ((footViewHolder)holder).pBar.setVisibility(View.GONE);
                ((footViewHolder)holder).text.setText(R.string.no_more_records);
            }else{
                ((footViewHolder)holder).pBar.setVisibility(View.VISIBLE);
                ((footViewHolder)holder).text.setText(R.string.loading);
            }
        }

    }

    @Override
    public int getItemCount() {
        if (isLoading){
            return lst.size() == 0 ? 0 : lst.size() + 1;
        }else{
            return lst.size() == 0 ? 1 : lst.size() + 1;
        }

    }


    @Override
    public int getItemViewType(int position) {
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    public void setLoaded(){
        this.isLoading = false;
    }

    public void setLoading(){
        this.isLoading = true;
    }

    public void setMoreToBeLoad(boolean more){
        this.moreToBeLoad = more;
    }

    public class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View v;
        TextView date, recipient, value, balance;

        viewHolder(View itemView) {
            super(itemView);
            v = itemView;
            v.setOnClickListener(this);
            date = v.findViewById(R.id.trans_date);
            recipient = v.findViewById(R.id.trans_recipient);
            value = v.findViewById(R.id.trans_value);
            balance = v.findViewById(R.id.trans_post_balance);
        }


        @Override
        public void onClick(View v) {
            final transaction_detail_item item = lst.get(getAdapterPosition());
            AlertDialog.Builder builder = new AlertDialog.Builder(transDetailItemAdapter.this.context);
            @SuppressLint("InflateParams")
            final View mView = ((Activity)transDetailItemAdapter.this.context).getLayoutInflater()
                    .inflate(R.layout.detailed_transaction_layout, null);
            Button redo = mView.findViewById(R.id.redo);
            redo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("from_account",((account_detail)transDetailItemAdapter.this.context).account_num);
                    intent.putExtra("to_account",item.getTrans_to());
                    intent.putExtra("to_first_name",item.getTrans_to_first_name());
                    intent.putExtra("to_last_name",item.getTrans_to_last_name());
                    intent.putExtra("value",item.getTrans_value());
                    intent.putExtra("memo",item.getTrans_memo());

                    ((Activity)transDetailItemAdapter.this.context).setResult(10, intent);
                    ((Activity)transDetailItemAdapter.this.context).finish();

                }
            });
            TextView transaction_id_field = mView.findViewById(R.id.transaction_id_field);
            transaction_id_field.setText(item.getTrans_id());
            TextView transaction_date_field = mView.findViewById(R.id.transaction_date_field);
            transaction_date_field.setText(date.getText());
            TextView transaction_from_field = mView.findViewById(R.id.transaction_from_field);
            transaction_from_field.setText(item.getTrans_from());
            TextView transaction_to_field = mView.findViewById(R.id.transaction_to_field);
            transaction_to_field.setText(item.getTrans_to());
            TextView transaction_to_lname_field = mView.findViewById(R.id.transaction_to_lname_field);
            transaction_to_lname_field.setText(item.getTrans_to_last_name());
            TextView transaction_value_field = mView.findViewById(R.id.transaction_value_field);
            transaction_value_field.setText(String.valueOf(item.getTrans_value()));
            TextView transaction_borrowing_sign_field = mView.findViewById(R.id.transaction_borrowing_sign_field);
            // - stands for borrow, + stands for loan
            if (item.getTrans_dir() == '-'){
                transaction_borrowing_sign_field.setText(R.string.borrow);
                redo.setVisibility(View.VISIBLE);
            }else if (item.getTrans_dir() == '+'){
                transaction_borrowing_sign_field.setText(R.string.loan);
                redo.setVisibility(View.GONE);
            }
            TextView transaction_channel_field = mView.findViewById(R.id.transaction_channel_field);
            //1 stands for mobile Client, 2 stands for Web, 3 stands for bank counter
            if (item.getTrans_channel() == '1'){
                transaction_channel_field .setText(R.string.mobile_client);
            }else if (item.getTrans_dir() == '2'){
                transaction_channel_field .setText(R.string.web);
            }else if (item.getTrans_dir() == '3') {
                transaction_channel_field .setText(R.string.bank_counter);
            }
            TextView transaction_post_balance_field = mView.findViewById(R.id.transaction_post_balance_field);
            transaction_post_balance_field.setText(String.valueOf(item.getTrans_post_balance()));
            TextView transaction_memo_field = mView.findViewById(R.id.transaction_memo_field);
            transaction_memo_field.setText(item.getTrans_memo());

            builder.setView(mView);
            final AlertDialog dialog = builder.create();
            dialog.show();


        }
    }

    public class footViewHolder extends RecyclerView.ViewHolder {

        ProgressBar pBar;
        TextView text;
        footViewHolder(View itemView) {
            super(itemView);
            pBar = itemView.findViewById(R.id.progressBar);
            text = itemView.findViewById(R.id.footView);

        }

    }
}
