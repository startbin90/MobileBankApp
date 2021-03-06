package com.example.davychen.mobileBankApp.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.davychen.mobileBankApp.R;
import com.example.davychen.mobileBankApp.adapters.transDetailItemAdapter;
import com.example.davychen.mobileBankApp.items.transaction_detail_item;
import com.example.davychen.mobileBankApp.services.transDetailService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class account_detail extends AppCompatActivity {

    public static String TAG = "account_datail";

    /**
     * start and end time set by first three tabs
     */
    private long start_time, end_time;

    /**
     * start and end time set by customize tab
     */
    private long cus_start_time, cus_end_time;

    /**
     * list used to store transaction detail item
     */
    public ArrayList<transaction_detail_item> lst = new ArrayList<>();

    /**
     * account info
     */
    public String account_num;
    public String balance;
    public String first_name, last_name;

    /**
     * layout attributes
     */
    public RecyclerView recyclerView;
    public transDetailItemAdapter adapter;
    public SwipeRefreshLayout mRefreshLayout;
    public TabLayout tabLayout;

    public Runnable currentRunner;
    /**
     * indicator that customize time range set
     */
    private boolean customizeSetSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);
        this.setTitle("Account Detail");

        //get account info from intent
        Intent theIntent = getIntent();
        this.account_num = theIntent.getStringExtra("ACCOUNT_NUM");
        this.balance = theIntent.getStringExtra("BALANCE");
        this.first_name =  theIntent.getStringExtra("first_name");
        this.last_name =  theIntent.getStringExtra("last_name");

        //set account info
        TextView account_num_title = findViewById(R.id.account_num);
        account_num_title.setText(this.account_num);
        TextView balance_title = findViewById(R.id.balance);
        balance_title.setText(this.balance);
        TextView first_name_title = findViewById(R.id.first_name);
        first_name_title.setText(this.first_name);
        TextView last_name_title = findViewById(R.id.last_name);
        last_name_title.setText(this.last_name);

        // transfer button
        ImageButton transfer = findViewById(R.id.transfer);
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("from_account",account_detail.this.account_num);
                account_detail.this.setResult(11, intent);
                account_detail.this.finish();

            }
        });

        this.recyclerView = findViewById(R.id.account_detail_list);
        this.mRefreshLayout = findViewById(R.id.account_detail_swipeRefreshLayout);
        mRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary
                , R.color.green
                , R.color.dividerGrey
                , R.color.colorAccent);
        this.mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                lst.clear();
                adapter.notifyDataSetChanged();

                if (customizeSetSuccess){
                    mRefreshLayout.setRefreshing(true);
                    start_time = cus_start_time;
                    end_time = cus_end_time;
                    currentRunner = callTransDetailService();
                }else{
                    setManualTabSelect(tabLayout.getSelectedTabPosition());
                }
            }
        });
        this.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.adapter = new transDetailItemAdapter(lst, this);
        this.recyclerView.setAdapter(adapter);
        this.tabLayout = findViewById(R.id.tabLayout);
        // pass activity to tab 3 in order to call the dialog
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    // One Month
                    case 0:
                        setDaysRangeFromToday(30);
                        mRefreshLayout.setRefreshing(true);
                        currentRunner = callTransDetailService();
                        break;
                    // One week
                    case 1:
                        setDaysRangeFromToday(7);
                        mRefreshLayout.setRefreshing(true);
                        currentRunner = callTransDetailService();
                        break;
                    // Today
                    case 2:
                        setDaysRangeFromToday(1);
                        mRefreshLayout.setRefreshing(true);
                        currentRunner = callTransDetailService();
                        break;
                    // Customize
                    case 3:
                        AlertDialog dialog = dateSelectDialog(account_detail.this);
                        dialog.show();
                        currentRunner = null;
                        //set loading before the date is selected
                        adapter.setLoading();
                        break;

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                lst.clear();
                adapter.notifyDataSetChanged();
                //set customizeSetSuccess to false when customize tab is unselected
                if (tab.getPosition() == 3){
                    customizeSetSuccess = false;
                }

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabUnselected(tab);
                onTabSelected(tab);
            }
        });
        //default tab set to TODAY
        setManualTabSelect(2);

    }

    public AlertDialog dateSelectDialog(Activity act){
        final Calendar dateReceiver = Calendar.getInstance();

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        @SuppressLint("InflateParams")
        final View mView = getLayoutInflater().inflate(R.layout.trans_range_select_layout, null);
        final RadioGroup group = mView.findViewById(R.id.radioGroup);
        final DatePicker datePicker = mView.findViewById(R.id.date_picker);
        builder.setView(mView);
        final AlertDialog dialog = builder.create();
        //set customizeSetSuccess to false when the dialog created
        customizeSetSuccess = false;
        //setup DIALOG
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // if dismiss called when customized time range set successfully
                if (customizeSetSuccess){
                    mRefreshLayout.setRefreshing(true);
                    currentRunner = callTransDetailService();
                }else{
                    // go to default tab when dismiss called without successfully set a time range
                    setManualTabSelect(2);
                }
                Log.i(TAG, "Date choosing dialog dismissed");
            }
        });
        //setup two toggleButtons
        final ToggleButton from = mView.findViewById(R.id.select_from);
        final ToggleButton to = mView.findViewById(R.id.select_to);
        ToggleButton.OnClickListener toggleListener = new ToggleButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tell radioGroup this button is clicked and set this button on and all others off
                group.check(v.getId());
                //keep toggleButton always on when it is clicked again
                ((ToggleButton)v).setChecked(true);
            }
        };
        from.setOnClickListener(toggleListener);

        //setup radioGroup
        RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int j = 0; j < group.getChildCount(); j++) {
                    if (group.getChildAt(j) instanceof ToggleButton){
                        final ToggleButton view = (ToggleButton) group.getChildAt(j);
                        if (view.getId() != checkedId){
                            view.setBackgroundColor(Color.TRANSPARENT);
                            view.setTextOff(view.getTextOn());
                            view.setChecked(false);
                        }else{
                            view.setChecked(true);
                            view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                            try{
                                switch (view.getId()){
                                    case R.id.select_from:
                                        Button next = mView.findViewById(R.id.dateSelectConfirm);
                                        next.setText(R.string.next);
                                        datePicker.setMaxDate(getCurrentTime().getTimeInMillis());
                                        datePicker.setMinDate(0);
                                        to.setTag(null);
                                        break;
                                    case R.id.select_to:
                                        view.setTextOn("");
                                        break;
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    }

                }
            }
        };
        group.setOnCheckedChangeListener(listener);
        group.check(R.id.select_from);

        //setup datePicker
        Calendar cal = getCurrentTime();
        datePicker.setMaxDate(cal.getTimeInMillis());
        datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                ToggleButton checked_but = mView.findViewById(group.getCheckedRadioButtonId());
                int newMonth = monthOfYear + 1;
                checked_but.setTextOn(year + "/" + newMonth + "/" + dayOfMonth);
                checked_but.setChecked(true);
                dateReceiver.set(year, monthOfYear, dayOfMonth);
                if (checked_but == from){
                    to.setTag(dateReceiver);
                }else{
                    from.setTag(dateReceiver);
                }
            }
        });

        //setup two normal buttons
        Button confirm = mView.findViewById(R.id.dateSelectConfirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton current = mView.findViewById(group.getCheckedRadioButtonId());
                if (current == from){
                    if (to.getTag() == null){
                        Toast.makeText(mView.getContext(), "Please select a starting date", Toast.LENGTH_SHORT).show();
                    }else{
                        start_time = getStartOfDay((Calendar) to.getTag()).getTimeInMillis();
                        cus_start_time = start_time;
                        group.check(to.getId());
                        to.setChecked(true);
                        ((Button) v).setText(R.string.confirm);
                    }
                }else {
                    boolean suc = true;
                    if (from.getTag() != null) {
                        end_time = getEndOfDay((Calendar) from.getTag()).getTimeInMillis();
                        cus_end_time = end_time;
                        if (end_time < start_time){
                            Toast.makeText(mView.getContext(), "starting date should precede closing date", Toast.LENGTH_SHORT).show();
                            suc = false;
                        }
                    } else {
                        Toast.makeText(mView.getContext(), "Please select a closing date", Toast.LENGTH_SHORT).show();
                        suc = false;
                    }
                    customizeSetSuccess = suc;
                    Calendar cal = Calendar.getInstance();
                    if (suc) {
                        //two log statements below will return the same time because to.getTag and
                        // from.getTag both get the same Calendar object which is dateReceiver.
                        Log.i(TAG, " from Time set" + ((Calendar)to.getTag()).getTime().toString());
                        Log.i(TAG, " to Time set" + ((Calendar)from.getTag()).getTime().toString());
                        cal.setTimeInMillis(start_time);
                        Log.i(TAG, "Time set" + cal.getTime().toString());
                        cal.setTimeInMillis(end_time);
                        Log.i(TAG, "Time set" + cal.getTime().toString());
                        dialog.dismiss();
                    }

                }
            }
        });

        Button close = mView.findViewById(R.id.dateSelectClose);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;

    }

    /**
     * Manually set tab position
     * @param position the tab position wants to be set
     */
    private void setManualTabSelect(int position){
        if (this.tabLayout != null){
            TabLayout.Tab tab = this.tabLayout.getTabAt(position);
            if (tab != null) {
                tab.select();
            }
        }
    }


    private Runnable callTransDetailService(){
        Runnable service = new transDetailService(this);
        Thread t1 = new Thread(service);
        t1.start();
        return service;
    }

    /**
     * get the current time as Calendar object
     */
    public Calendar getCurrentTime(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        return cal;
    }

    /**
     * set the end_time as the last moment of today and set the start_time as the start moment of
     * 'days' before today
     * @param days before today
     */
    public void setDaysRangeFromToday(int days){
        Calendar end = getEndOfDay(null);
        long end_time = end.getTimeInMillis();
        end.add(Calendar.DATE, -days);
        long from_time = end.getTimeInMillis() + 1;
        setTimeRange(from_time, end_time);
    }

    /**
     * set the start_time and end_time attribute
     */
    public void setTimeRange(long start_time, long end_time) {
        this.start_time = start_time;
        this.end_time = end_time;
    }

    /**
     * set start time
     */
    public void setStart_time(long start_time){
        this.start_time = start_time;
    }

    /**
     * set end time
     */
    public void setEnd_time(long end_time){
        this.end_time = end_time;
    }

    /**
     * start time getter
     * @return start time
     */
    public long getStart_time() {
        return start_time;
    }

    /**
     * end time getter
     * @return end time
     */
    public long getEnd_time() {
        return end_time;
    }

    /**
     * given a data return the last moment of that day
     */
    public static Calendar getEndOfDay(Calendar cal) {
        if (cal == null)
            cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
        return cal;
    }

    /**
     * given a data return the first moment of that day
     */
    public static Calendar getStartOfDay(Calendar cal) {
        if (cal == null)
            cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal;
    }
}
