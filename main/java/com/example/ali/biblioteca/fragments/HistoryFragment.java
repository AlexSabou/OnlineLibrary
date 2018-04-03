package com.example.ali.biblioteca.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ali.biblioteca.R;
import com.example.ali.biblioteca.activities.HomeActivity;
import com.example.ali.biblioteca.model.Loan;
import com.example.ali.biblioteca.model.Reservation;
import com.example.ali.biblioteca.model.Rules;
import com.example.ali.biblioteca.model.Stock;
import com.example.ali.biblioteca.model.User;
import com.example.ali.biblioteca.utility.FragmentStateAdapter;
import com.example.ali.biblioteca.utility.ListUtils;
import com.example.ali.biblioteca.utility.PicassoClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.example.ali.biblioteca.fragments.ManageFragment.ORDER_NAME;

/**
 * Created by Ali on 10.01.2018.
 */

public class HistoryFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "HistoryFragment";

    public final static String ORDER_LOAN_DATE = "returnDate";
    public final static String ORDER_RESERVATION_DATE = "expireDate";

    private View view;
    private DatabaseReference dbReference;
    private User currentUser;
    private ListView lwReservation, lwLoan;
    private List<Reservation> reservationList;
    private List<Loan> loanList;
    private HashMap<String, Stock> reservationStock;
    private HashMap<String, Stock> loanStock;
    private boolean reservationLoaded = false, loanLoaded = false;
    private boolean resAdapterLoaded = false, loanAdapterLoaded = false;
    private ReservationAdapter reservationAdapter;
    private LoanAdapter loanAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.history_layout, container, false);

        dbReference = FirebaseDatabase.getInstance().getReference();
        Log.e(TAG, "Started");
        loadElements(view);

        return view;
    }

    /**
     * Refreshes the page's data.
     */
    public void refreshFragment() {
        currentUser = ((HomeActivity)getActivity()).getCurrentUser();
        selectAllReservations();
        selectAllLoans();
    }

    /**
     * Method that loads the UI elements needed for the page.
     * @param view
     */
    private void loadElements(View view) {
        lwReservation = (ListView) view.findViewById(R.id.lwReservation);
        lwLoan = (ListView) view.findViewById(R.id.lwLoan);
    }

    /**
     * Searches for all the reservations.
     */
    private void selectAllReservations() {
        dbReference.child("reservation").orderByChild("userKey").equalTo(currentUser.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getReservations(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Handles the query result for the method selectAllReservations.
     * @param dataSnapshot
     */
    private void getReservations(DataSnapshot dataSnapshot) {
        if(reservationList == null)
            reservationList = new ArrayList<>();
        else
            reservationList.clear();
        if(reservationStock == null)
            reservationStock = new HashMap<>();
        else
            reservationStock.clear();

        resAdapterLoaded = false;
        reservationLoaded = false;
        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            Reservation reservation = ds.getValue(Reservation.class);
            if(reservation != null) {
                final String resKey = ds.getKey();
                reservation.setKey(resKey);
                reservationList.add(reservation);
                dbReference.child("stock").child(reservation.getStockKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Stock stock = dataSnapshot.getValue(Stock.class);
                        if(stock != null) {
                            stock.setKey(dataSnapshot.getKey());
                            reservationStock.put(resKey, stock);
                            setupReservationAdapter();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        reservationLoaded = true;
        setupReservationAdapter();

    }

    /**
     * Creates the adapter for reservation ListView.
     */
    private void setupReservationAdapter() {
        if(reservationLoaded && reservationList.size() == reservationStock.size()) {
            Collections.sort(reservationList);
            reservationAdapter = new ReservationAdapter(getActivity(), reservationList, reservationStock);
            lwReservation.setAdapter(reservationAdapter);

            resAdapterLoaded = true;
            setListViewHeight();
        }
    }

    /**
     * Searches for all the loans.
     */
    private void selectAllLoans() {
        dbReference.child("loan").orderByChild("userKey").equalTo(currentUser.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getLoans(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Handles the query result for the method selectAllLoans.
     * @param dataSnapshot
     */
    private void getLoans(DataSnapshot dataSnapshot) {
        if(loanList == null)
            loanList = new ArrayList<>();
        else
            loanList.clear();
        if(loanStock == null)
            loanStock = new HashMap<>();
        else
            loanStock.clear();

        loanAdapterLoaded = false;
        loanLoaded = false;
        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            Loan loan = ds.getValue(Loan.class);
            if(loan != null) {
                final String loanKey = ds.getKey();
                loan.setKey(loanKey);
                loanList.add(loan);
                dbReference.child("stock").child(loan.getStockKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Stock stock = dataSnapshot.getValue(Stock.class);
                        if(stock != null) {
                            stock.setKey(dataSnapshot.getKey());
                            loanStock.put(loanKey, stock);
                            setupLoanAdapter();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        loanLoaded = true;
        setupLoanAdapter();

    }

    /**
     * Creates the adapter for loan ListView.
     */
    private void setupLoanAdapter() {
        if(loanLoaded && loanList.size() == loanStock.size()) {
            Collections.sort(loanList);
            loanAdapter = new LoanAdapter(getActivity(), loanList, loanStock);
            lwLoan.setAdapter(loanAdapter);

            loanAdapterLoaded = true;
            setListViewHeight();
        }
    }

    /**
     * Sets the ListViews' heights dynamically.
     */
    private void setListViewHeight() {
        ListUtils.setDynamicHeight(lwReservation);
        ListUtils.setDynamicHeight(lwLoan);
    }

    public String diffDate(Date currentDate, Date forwardDate) {
        long SECOND = 1_000, MINUTE = 60 * SECOND, HOUR = 60 * MINUTE, DAY = 24 * HOUR;
        long diffTime =  forwardDate.getTime() - currentDate.getTime();
        long diffDays = diffTime/DAY, diffHours = diffTime/HOUR, diffMinutes = diffTime/MINUTE, diffSeconds = diffTime/SECOND;
        long value = 0;
        String text = "";
        if(diffDays != 0) {
            value = diffDays;
            text = " days ";
        }
        else {
            if(diffHours > 0) {
                value = diffHours;
                text = " hours ";
            }
            else {
                if(diffMinutes > 0) {
                    value = diffMinutes;
                    text = " minutes ";
                }
                else {
                    if(diffSeconds > 0) {
                        value = diffSeconds;
                        text = " seconds ";
                    }
                    else {
                        value = 0;
                        text = " seconds ";
                    }
                }
            }
        }
        if(diffTime > 0)
            return value + text + "left";
        else
            return ((-1) * value) + text + "late";
    }

    /**
     * Calculates the tax, using the current date, the return date and the tax/day rules.
     * @param currentDate
     * @param returnDate
     * @param rules
     * @return
     */
    private long calculateTax(Date currentDate, Date returnDate, Rules rules) {
        int tax = 1;
        if(rules != null)
            tax = rules.getTax();
        long SECOND = 1_000, MINUTE = 60 * SECOND, HOUR = 60 * MINUTE, DAY = 24 * HOUR;
        long diffTime =  returnDate.getTime() - currentDate.getTime();
        long diffDays = diffTime/DAY;
        if(diffDays < 0)
            return (-1) * (diffDays) * tax;
        else
            return 0;
    }

    /**
     * A custom adapter for reservations ListView.
     */
    class ReservationAdapter extends BaseAdapter {

        private Context context;
        private List<Reservation> reservationList;
        private HashMap<String, Stock> stockMap;
        private LayoutInflater inflater;

        public ReservationAdapter(Context context, List<Reservation> reservationList, HashMap<String, Stock> stockMap) {
            this.context = context;
            this.reservationList = reservationList;
            this.stockMap = stockMap;
        }

        @Override
        public int getCount() {
            return reservationList.size();
        }

        @Override
        public Object getItem(int position) {
            return reservationList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(inflater == null)
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null)
                convertView = inflater.inflate(R.layout.history_row, parent, false);

            HistoryFragment.ReservationHolder holder = new HistoryFragment.ReservationHolder(convertView);
            final Reservation res = reservationList.get(position);
            final String resKey = res.getKey();
            final Stock stock = stockMap.get(resKey);

            if(stock != null) {
                holder.tvTitle.setText(stock.getTitle());
                holder.tvAuthor.setText(stock.getAuthor());
                PicassoClient.downloading(context, stock.getImageUrl(), holder.iwBookCover);
            }

            if(res != null) {
                holder.tvExpireDate.setText(diffDate(Calendar.getInstance().getTime(), res.getExpireDate()));
                holder.tvTax.setVisibility(View.GONE);
            }

            holder.btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentUser != null) {
                        if(currentUser.getBooksReserved() > 0)
                            dbReference.child("user").child(currentUser.getKey()).child("booksReserved").setValue(currentUser.getBooksReserved() - 1);
                        dbReference.child("book").child(res.getBookKey()).child("reserved").setValue(false);
                        dbReference.child("stock").child(stock.getKey()).child("nrOfBooks").setValue(stock.getNrOfBooks() + 1);
                        dbReference.child("reservation").child(res.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Reservation deleted.", Toast.LENGTH_SHORT).show();
                                    selectAllReservations();
                                }
                                else {
                                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });

            return convertView;
        }

    }

    /**
     * A custom adapter for loans ListView.
     */
    class LoanAdapter extends BaseAdapter {

        private Context context;
        private List<Loan> loanList;
        private HashMap<String, Stock> stockMap;
        private LayoutInflater inflater;

        public LoanAdapter(Context context, List<Loan> loanList, HashMap<String, Stock> stockMap) {
            this.context = context;
            this.loanList = loanList;
            this.stockMap = stockMap;
        }

        @Override
        public int getCount() {
            return loanList.size();
        }

        @Override
        public Object getItem(int position) {
            return loanList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(inflater == null)
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null)
                convertView = inflater.inflate(R.layout.history_row, parent, false);

            HistoryFragment.ReservationHolder holder = new HistoryFragment.ReservationHolder(convertView);
            final Loan loan = loanList.get(position);
            final String loanKey = loan.getKey();
            final Stock stock = stockMap.get(loanKey);

            if(stock != null) {
                holder.tvTitle.setText(stock.getTitle());
                holder.tvAuthor.setText(stock.getAuthor());
                PicassoClient.downloading(context, stock.getImageUrl(), holder.iwBookCover);
            }

            if(loan != null) {
                //Log.e("Time", "TimeC: " + Calendar.getInstance().getTime() + " | TimeF: " + loan.getReturnDate());

                if(!loan.isReturned()) {
                    String expireText = diffDate(Calendar.getInstance().getTime(), loan.getReturnDate());
                    long tax = calculateTax(Calendar.getInstance().getTime(), loan.getReturnDate(), ((HomeActivity)getActivity()).getRules());
                    if(expireText.contains("late")) {
                        holder.tvExpireDate.setTextColor(getResources().getColor(R.color.colorRed));
                        holder.tvExpireDate.setText("Expired: " + expireText);
                    }
                    else {
                        holder.tvExpireDate.setTextColor(getResources().getColor(R.color.colorExcellent));
                        holder.tvExpireDate.setText(expireText);
                    }
                    holder.tvTax.setVisibility(View.VISIBLE);
                    holder.tvTax.setText("Tax: " + tax + " lei");
                }
                else {
                    holder.tvExpireDate.setText("Returned");
                    holder.tvTax.setVisibility(View.GONE);
                }

            }

            holder.btnCancel.setVisibility(View.GONE);

            return convertView;
        }

    }

    /**
     * A holder class, that holds the UI elements for the adapters above.
     */
    class ReservationHolder {

        TextView tvTitle, tvAuthor, tvExpireDate, tvTax;
        Button btnCancel;
        ImageView iwBookCover;

        public ReservationHolder(View view) {
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvAuthor = (TextView) view.findViewById(R.id.tvAuthor);
            tvExpireDate = (TextView) view.findViewById(R.id.tvExpireDate);
            tvTax = (TextView) view.findViewById(R.id.tvTax);

            iwBookCover = (ImageView) view.findViewById(R.id.iwBookCover);

            btnCancel = (Button) view.findViewById(R.id.btnCancel);
        }

    }
}
