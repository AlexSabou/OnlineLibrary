package com.example.ali.biblioteca.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ali.biblioteca.R;
import com.example.ali.biblioteca.activities.HomeActivity;
import com.example.ali.biblioteca.model.Book;
import com.example.ali.biblioteca.model.Loan;
import com.example.ali.biblioteca.model.Rules;
import com.example.ali.biblioteca.model.Stock;
import com.example.ali.biblioteca.model.User;
import com.example.ali.biblioteca.utility.FragmentStateAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.example.ali.biblioteca.fragments.ManageFragment.ORDER_NAME;

/**
 * Created by Ali on 10.01.2018.
 */

public class LoanToFragment extends Fragment {
    private static final String TAG = "LoanToFragment";
    
    private View view;
    private User currentUser;
    private ListView lwAccounts;
    private LoanToFragment.UserAdapter userAdapter;
    private List<User> userList;
    private Stock stock;
    private DatabaseReference dbReference;
    private String searchText = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.loan_layout, container, false);

        dbReference = FirebaseDatabase.getInstance().getReference();
        stock = ((HomeActivity)getActivity()).getTransferStock();
        currentUser = ((HomeActivity)getActivity()).getCurrentUser();

        Log.e(TAG, "Started");
        loadElements(view);

        return view;
    }

    /**
     * Refreshes the page's data.
     */
    public void refreshFragment() {
        currentUser = ((HomeActivity)getActivity()).getCurrentUser();
        stock = ((HomeActivity)getActivity()).getTransferStock();
        selectAll(ORDER_NAME);
    }

    /**
     * Searches for a specific user that contains @param text, ordered by @param orderCriteria and
     * @param orderCriteria
     * @param text
     */
    public void selectSpecificUser(String orderCriteria, final String text) {
        searchText = text;
        FirebaseDatabase.getInstance().getReference("user").orderByChild(orderCriteria).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSpecificUpdates(dataSnapshot, text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Handles the query result for the method above.
      * @param dataSnapshot
     * @param text
     */
    private void getSpecificUpdates(DataSnapshot dataSnapshot, String text) {
        String formattedText = text.trim().toLowerCase();

        if(userList == null)
            userList = new ArrayList<>();
        else
            userList.clear();

        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            User user = ds.getValue(User.class);
            if(user != null) {
                user.setKey(ds.getKey());
                if(user.getName().toLowerCase().contains(formattedText) || user.getEmail().toLowerCase().contains(formattedText))
                    userList.add(user);
            }
        }

        if(lwAccounts == null)
            lwAccounts = (ListView) view.findViewById(R.id.lwAccounts);

        userAdapter = new LoanToFragment.UserAdapter(getActivity(), userList);
        lwAccounts.setAdapter(userAdapter);

        if(userList.size() == 0) {
            Toast.makeText(getContext(), "No account found matching this criteria.", Toast.LENGTH_SHORT).show();
        }
    }

    public void selectAll(String orderCriteria) {
        searchText = "";
        FirebaseDatabase.getInstance().getReference("user").orderByChild(orderCriteria).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getUpdates(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUpdates(DataSnapshot dataSnapshot) {
        if(userList == null)
            userList = new ArrayList<>();
        else
            userList.clear();

        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            User user = ds.getValue(User.class);
            if(user != null) {
                user.setKey(ds.getKey());
                userList.add(user);
            }
        }

        if(lwAccounts == null)
            lwAccounts = (ListView) view.findViewById(R.id.lwAccounts);

        userAdapter = new LoanToFragment.UserAdapter(getActivity(), userList);
        lwAccounts.setAdapter(userAdapter);

        if(userList.size() == 0) {
            Toast.makeText(getContext(), "No account found matching this criteria.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadElements(View view) {
        lwAccounts = (ListView) view.findViewById(R.id.lwAccounts);
    }

    private void getBooks(final User user, DataSnapshot dataSnapshot) {
        List<Book> bookList = new ArrayList<>();
        Log.e("Children", "" + dataSnapshot.getChildrenCount());
        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            Book book = ds.getValue(Book.class);
            if(book != null) {
                final String bookKey = ds.getKey();
                book.setKey(bookKey);
                if(!book.isReserved() && !book.isLoaned()) {
                    bookList.add(book);
                }
            }
        }

        Collections.sort(bookList);
        if(bookList.size() > 0) {
            final Book book = bookList.get(0);
            dbReference.child("book").child(book.getKey()).child("loaned").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        decrementStock(stock);
                        incrementLoans(user);
                        createLoan(user, book);
                    }
                    else {
                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
        else {
            Toast.makeText(getActivity(), "No more books are available.", Toast.LENGTH_SHORT).show();
        }

    }

    private void loanBookToUser(final User user, final Stock stock) {
        dbReference.child("book").orderByChild("stockKey").equalTo(stock.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getBooks(user, dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void decrementStock(Stock stock) {
        dbReference.child("stock").child(stock.getKey()).child("nrOfBooks").setValue(stock.getNrOfBooks() - 1);
    }

    private void incrementLoans(User user) {
        dbReference.child("user").child(user.getKey()).child("booksLoaned").setValue(user.getBooksLoaned() + 1);
    }

    private void createLoan(final User user, Book book) {
        decrementStock(stock);
        incrementLoans(user);
        String bookKey = book.getKey();

        Date loanDate = Calendar.getInstance().getTime();
        long currentTime = Calendar.getInstance().getTimeInMillis();
        currentTime += 7 * 24 * 60 * 60 * 1_000;    //7 days
        Calendar cnd = Calendar.getInstance();
        cnd.setTimeInMillis(currentTime);
        final Date returnDate = cnd.getTime();
        final String loanKey = dbReference.child("loan").push().getKey();
        Loan loan = new Loan(stock.getKey(), bookKey, user.getKey(), loanDate, returnDate, false);
        dbReference.child("loan").child(loanKey).setValue(loan).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Book loaned to " + user.getName() + ".", Toast.LENGTH_LONG).show();
                    if(searchText.isEmpty())
                        selectAll(ORDER_NAME);
                    else
                        selectSpecificUser(ORDER_NAME, searchText);
                }
                else {
                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    class UserAdapter extends BaseAdapter {

        private Context context;
        private List<User> userList;
        private LayoutInflater inflater;

        public UserAdapter(Context context, List<User> userList) {
            this.context = context;
            this.userList = userList;
        }

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public Object getItem(int position) {
            return userList.get(position);
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
                convertView = inflater.inflate(R.layout.loan_list_row, parent, false);

            LoanToFragment.UserHolder holder = new LoanToFragment.UserHolder(convertView);
            final User user = userList.get(position);

            if(user != null) {
                holder.tvName.setText(user.getName());
                holder.tvBooksLoaned.setText("Books loaned: " + user.getBooksLoaned());
                holder.tvBooksReserved.setText("Books reserved: " + user.getBooksReserved());
            }

            final View copyView = convertView;
            final LoanToFragment.UserHolder copyHolder = holder;

            holder.btnLoan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(user != null) {
                        Rules rules = ((HomeActivity)getActivity()).getRules();
                        if(rules != null) {
                            if(user.getBooksLoaned() < rules.getLoan()) {
                                loanBookToUser(user, stock);
                            }
                            else {
                                Toast.makeText(getContext(), user.getName() + " reached the loan limit.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            if(user.getBooksLoaned() < 3) {
                                loanBookToUser(user, stock);
                            }
                            else {
                                Toast.makeText(getContext(), user.getName() + " reached the loan limit.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });

            return convertView;
        }

    }

    class UserHolder {

        TextView tvName, tvBooksLoaned, tvBooksReserved;
        Button btnLoan;

        public UserHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tvName);
            tvBooksLoaned = (TextView) view.findViewById(R.id.tvBooksLoaned);
            tvBooksReserved = (TextView) view.findViewById(R.id.tvBooksReserved);

            btnLoan = (Button) view.findViewById(R.id.btnLoan);
        }

    }
}
