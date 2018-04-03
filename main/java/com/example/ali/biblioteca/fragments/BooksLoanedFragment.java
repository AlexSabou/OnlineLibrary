package com.example.ali.biblioteca.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ali.biblioteca.R;
import com.example.ali.biblioteca.activities.HomeActivity;
import com.example.ali.biblioteca.model.Book;
import com.example.ali.biblioteca.model.Loan;
import com.example.ali.biblioteca.model.Stock;
import com.example.ali.biblioteca.model.User;
import com.example.ali.biblioteca.utility.FragmentStateAdapter;
import com.example.ali.biblioteca.utility.PicassoClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ali on 10.01.2018.
 */

public class BooksLoanedFragment extends Fragment {
    private static final String TAG = "BooksLoanedFragment";
    
    private ListView lwLoans;
    private List<Loan> loanList;
    private HashMap<String, Stock> stockMap;
    private HashMap<String, User> userMap;
    private HashMap<String, Book> bookMap;
    private BooksLoanedFragment.LoanAdapter loansAdapter;
    private View view;
    private DatabaseReference dbReference;
    private boolean loanDone = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.books_loaned, container, false);

        Log.e(TAG, "Started");

        dbReference = FirebaseDatabase.getInstance().getReference();
        loadElements(view);

        return view;
    }

    /**
     * Refreshes the page's data.
     */
    public void refreshFragment() {
        selectAll();
    }

    /**
     * Method that loads the UI elements needed for the page.
     * @param view
     */
    private void loadElements(View view) {
        lwLoans = (ListView) view.findViewById(R.id.lwLoans);
    }

    /**
     * Searches for all the loaned books.
     */
    private void selectAll() {
        dbReference.child("loan").orderByChild("returnDate").addValueEventListener(new ValueEventListener() {
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
     * Handles the query result for the method selectAll.
     * @param dataSnapshot
     */
    private void getLoans(DataSnapshot dataSnapshot) {
        if(loanList == null)
            loanList = new ArrayList<>();
        else
            loanList.clear();
        if(userMap == null)
            userMap = new HashMap<>();
        else
            userMap.clear();
        if(stockMap == null)
            stockMap = new HashMap<>();
        else
            stockMap.clear();
        if(bookMap == null)
            bookMap = new HashMap<>();
        else
            bookMap.clear();

        loanDone = false;
        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            final Loan loan = ds.getValue(Loan.class);
            if(loan != null) {
                final String loanKey = ds.getKey();
                loan.setKey(loanKey);
                if(!loan.isReturned()) {
                    loanList.add(loan);
                    dbReference.child("user").child(loan.getUserKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if(user != null) {
                                user.setKey(dataSnapshot.getKey());
                                userMap.put(loanKey, user);
                                setupLoanAdapter();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    dbReference.child("stock").child(loan.getStockKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Stock stock = dataSnapshot.getValue(Stock.class);
                            if(stock != null) {
                                stock.setKey(dataSnapshot.getKey());
                                stockMap.put(loanKey, stock);
                                setupLoanAdapter();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    dbReference.child("book").child(loan.getBookKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Book book = dataSnapshot.getValue(Book.class);
                            if(book != null) {
                                book.setKey(dataSnapshot.getKey());
                                bookMap.put(loanKey, book);
                                setupLoanAdapter();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }
        loanDone = true;
        setupLoanAdapter();
    }

    /**
     * Creates the adapter for the loan's ListView.
     */
    private void setupLoanAdapter() {
        int loanSize = loanList.size();
        if(loanDone && loanSize == userMap.size() && loanSize == stockMap.size() && loanSize == bookMap.size()) {
            loansAdapter = new LoanAdapter(getActivity(), loanList, stockMap, userMap, bookMap);
            lwLoans.setAdapter(loansAdapter);
        }
    }

    /**
     * Formats @param date into DD-MM-yyyy hh:mm:ss.
     * @param date
     * @return
     */
    public String formatDate(Date date) {
        DateFormat df = new DateFormat();
        return df.format("dd-MM-yyyy HH:mm:ss", date).toString();
    }

    /**
     * A custom adapter for the loan ListView.
     */
    class LoanAdapter extends BaseAdapter {

        private Context context;
        private List<Loan> loanList;
        private HashMap<String, Stock> stockMap;
        private HashMap<String, User> userMap;
        private HashMap<String, Book> bookMap;
        private LayoutInflater inflater;

        public LoanAdapter(Context context, List<Loan> loanList, HashMap<String, Stock> stockMap, HashMap<String, User> userMap, HashMap<String, Book> bookMap) {
            this.context = context;
            this.loanList = loanList;
            this.stockMap = stockMap;
            this.userMap = userMap;
            this.bookMap = bookMap;
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
                convertView = inflater.inflate(R.layout.loans_list_row, parent, false);

            final BooksLoanedFragment.ReservationHolder holder = new BooksLoanedFragment.ReservationHolder(convertView);
            final Loan loan = loanList.get(position);
            final String loanKey = loan.getKey();
            final Stock stock = stockMap.get(loanKey);
            final User user = userMap.get(loanKey);
            final Book book = bookMap.get(loanKey);

            if(user != null) {
                holder.tvName.setText(user.getName());
            }
            if(stock != null) {
                holder.tvTitle.setText(stock.getTitle());
                PicassoClient.downloading(context, stock.getImageUrl(), holder.iwBookCover);
            }
            if(book != null) {
                holder.clearAllRadioButtons();
                switch(book.getState()) {
                    case Book.STATE_EXCELLENT:
                        holder.tvState.setTextColor(ContextCompat.getColor(context, R.color.colorExcellent));
                        holder.rbExcellent.setChecked(true);
                        break;
                    case Book.STATE_VERY_GOOD:
                        holder.tvState.setTextColor(ContextCompat.getColor(context, R.color.colorVeryGood));
                        holder.rbVeryGood.setChecked(true);
                        break;
                    case Book.STATE_GOOD:
                        holder.tvState.setTextColor(ContextCompat.getColor(context, R.color.colorGood));
                        holder.rbGood.setChecked(true);
                        break;
                    case Book.STATE_FAIR:
                        holder.tvState.setTextColor(ContextCompat.getColor(context, R.color.colorFair));
                        holder.rbFair.setChecked(true);
                        break;
                    case Book.STATE_POOR:
                        holder.tvState.setTextColor(ContextCompat.getColor(context, R.color.colorPoor));
                        holder.rbPoor.setChecked(true);
                        break;
                }
                holder.tvState.setText(Book.stateDesc[book.getState()]);
                holder.tvBookKey.setText(book.getKey());
                holder.tvLocation.setText(book.getLocation());

            }

            if(loan != null) {
                //Log.e("Time", "TimeC: " + Calendar.getInstance().getTime() + " | TimeF: " + loan.getReturnDate());
                holder.tvExpireDate.setText(formatDate(loan.getReturnDate()));
            }

            holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String state = holder.getCheckRadioButton();
                    int stateId = 0;
                    for(int i=0; i<Book.stateDesc.length; i++) {
                        if(Book.stateDesc.equals(state)) {
                            stateId = i;
                            break;
                        }
                    }
                    final String stockKey = loan.getStockKey();
                    dbReference.child("book").child(loan.getBookKey()).child("loaned").setValue(false);
                    dbReference.child("book").child(loan.getBookKey()).child("state").setValue(stateId);
                    if(user != null && user.getBooksLoaned() > 0)
                        dbReference.child("user").child(loan.getUserKey()).child("booksLoaned").setValue(user.getBooksLoaned() - 1);
                    dbReference.child("stock").child(stockKey).child("nrOfBooks").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Integer books = dataSnapshot.getValue(Integer.class);
                            if(books != null)
                                dbReference.child("stock").child(stockKey).child("nrOfBooks").setValue(books + 1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    dbReference.child("loan").child(loan.getKey()).child("returned").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(getActivity(), stock.getTitle() + " has been returned by " + user.getName(), Toast.LENGTH_LONG).show();

                            }
                            else {
                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

            return convertView;
        }

    }

    /**
     * A holder class, that holds all the UI elements needed for the loan ListView.
     */
    class ReservationHolder {

        TextView tvName, tvTitle, tvState, tvExpireDate, tvBookKey, tvLocation;
        Button btnAccept;
        ImageView iwBookCover;
        RadioGroup rgState;
        RadioButton rbSelected, rbExcellent, rbVeryGood, rbGood, rbFair, rbPoor;

        public ReservationHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tvName);
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvState = (TextView) view.findViewById(R.id.tvState);
            tvExpireDate = (TextView) view.findViewById(R.id.tvExpireDate);
            tvBookKey = (TextView) view.findViewById(R.id.tvBookKey);
            tvLocation = (TextView) view.findViewById(R.id.tvLocation);

            iwBookCover = (ImageView) view.findViewById(R.id.iwBookCover);

            btnAccept = (Button) view.findViewById(R.id.btnAccept);
            rgState = (RadioGroup) view.findViewById(R.id.rgState);
            rbExcellent = (RadioButton) view.findViewById(R.id.rbExcellent);
            rbVeryGood = (RadioButton) view.findViewById(R.id.rbVeryGood);
            rbGood = (RadioButton) view.findViewById(R.id.rbGood);
            rbFair = (RadioButton) view.findViewById(R.id.rbFair);
            rbPoor = (RadioButton) view.findViewById(R.id.rbPoor);

            rbExcellent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearAllRadioButtons();
                    rbExcellent.setChecked(true);
                }
            });
            rbVeryGood.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearAllRadioButtons();
                    rbVeryGood.setChecked(true);
                }
            });
            rbGood.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearAllRadioButtons();
                    rbGood.setChecked(true);
                }
            });
            rbFair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearAllRadioButtons();
                    rbFair.setChecked(true);
                }
            });
            rbPoor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearAllRadioButtons();
                    rbPoor.setChecked(true);
                }
            });
        }

        private void clearAllRadioButtons() {
            rbExcellent.setChecked(false);
            rbVeryGood.setChecked(false);
            rbGood.setChecked(false);
            rbFair.setChecked(false);
            rbPoor.setChecked(false);
        }

        private String getCheckRadioButton() {
            if(rbExcellent.isChecked())
                return rbExcellent.getText().toString();
            else if(rbVeryGood.isChecked())
                return rbVeryGood.getText().toString();
            else if(rbGood.isChecked())
                return rbGood.getText().toString();
            else if(rbFair.isChecked())
                return rbFair.getText().toString();
            else
                return rbPoor.getText().toString();
        }

    }
}
