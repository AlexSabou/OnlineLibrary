package com.example.ali.biblioteca.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ali.biblioteca.R;
import com.example.ali.biblioteca.activities.HomeActivity;
import com.example.ali.biblioteca.model.Book;
import com.example.ali.biblioteca.model.Loan;
import com.example.ali.biblioteca.model.Reservation;
import com.example.ali.biblioteca.model.Rules;
import com.example.ali.biblioteca.model.Stock;
import com.example.ali.biblioteca.model.User;
import com.example.ali.biblioteca.utility.FragmentStateAdapter;
import com.example.ali.biblioteca.utility.ListUtils;
import com.example.ali.biblioteca.utility.PicassoClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.concurrent.ExecutionException;

/**
 * Created by Ali on 08.01.2018.
 */

public class BookFragment extends Fragment {

    private static final String TAG = "BookFragment";
    private final boolean DEBUG = false;

    private DatabaseReference dbReference;
    private User currentUser;

    private ListView lwReservation;
    private TextView tvTitle, tvAuthor, tvGenre, tvStock, tvDescription;
    private ImageView iwCover;
    private Button btnAction, btnAddBook, btnEditStock;
    private RelativeLayout rlList;
    private HashMap<String, Book> bookMap;
    private HashMap<String, User> userMap;
    private List<Reservation> reservationList;
    private ReservationAdapter reservationAdapter;

    private boolean reservationDone = false;

    private Stock stock;


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.book_fragment, container, false);

        dbReference = FirebaseDatabase.getInstance().getReference();

        stock = ((HomeActivity)getActivity()).getTransferStock();
        currentUser = ((HomeActivity)getActivity()).getCurrentUser();

        loadElements(view);

        return view;
    }

    /**
     * Refreshes the page's data.
     */
    public void refreshFragment() {
        stock = ((HomeActivity)getActivity()).getTransferStock();
        currentUser = ((HomeActivity)getActivity()).getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();

        if(currentUser != null) {
            switch (currentUser.getRole()) {
                case User.ROLE_USER:
                case User.ROLE_ADMIN: {
                    btnAction.setText("Reserve");
                    rlList.setVisibility(View.GONE);
                    btnAddBook.setVisibility(View.GONE);
                    btnEditStock.setVisibility(View.GONE);
                    break;
                }
                default: {
                    btnAction.setText("Loan");
                    btnAddBook.setVisibility(View.VISIBLE);
                    btnEditStock.setVisibility(View.VISIBLE);
                    rlList.setVisibility(View.VISIBLE);
                    searchReservations(stock.getKey());
                    break;
                }
            }
        }

        dbReference.child("stock").child(stock.getKey()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                stock = dataSnapshot.getValue(Stock.class);
                if(stock != null) {
                    stock.setKey(dataSnapshot.getKey());
                    updateUIInfo();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Method that loads the UI elements needed for the page.
     * @param view
     */
    private void loadElements(View view) {
        tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvAuthor = (TextView) view.findViewById(R.id.tvAuthor);
        tvGenre = (TextView) view.findViewById(R.id.tvGenre);
        tvStock = (TextView) view.findViewById(R.id.tvStock);
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);
        lwReservation = (ListView) view.findViewById(R.id.lwReservation);
        iwCover = (ImageView) view.findViewById(R.id.iwCover);
        rlList = (RelativeLayout) view.findViewById(R.id.rlList);

        btnAction = (Button) view.findViewById(R.id.btnAction);
        btnAddBook = (Button) view.findViewById(R.id.btnAddBook);
        btnEditStock = (Button) view.findViewById(R.id.btnEditStock);

//        if(currentUser != null) {
//            switch (currentUser.getRole()) {
//                case User.ROLE_USER:
//                case User.ROLE_ADMIN: {
//                    btnAction.setText("Reserve");
//                    rlList.setVisibility(View.GONE);
//                    btnAddBook.setVisibility(View.GONE);
//                    btnEditStock.setVisibility(View.GONE);
//                    break;
//                }
//                default: {
//                    btnAction.setText("Loan");
//                    btnAddBook.setVisibility(View.VISIBLE);
//                    btnEditStock.setVisibility(View.VISIBLE);
//                    rlList.setVisibility(View.VISIBLE);
//                    searchReservations(stock.getKey());
//                    break;
//                }
//            }
//        }

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentUser != null) {
                    switch (currentUser.getRole()) {
                        case User.ROLE_LIBRARIAN: {
                            ((HomeActivity)getActivity()).setViewPager(FragmentStateAdapter.BOOK_LOAN_TO);
                            break;
                        }
                        default: {
                            if(stock.getNrOfBooks() > 0) {
                                Rules rules = ((HomeActivity)getActivity()).getRules();
                                if(rules != null) {
                                    if(currentUser.getBooksLoaned() < rules.getLoan()) {
                                        if(currentUser.getBooksReserved() < rules.getReservation()) {
                                            userReserveBook(currentUser, stock);
                                        }
                                        else {
                                            Toast.makeText(getActivity(), "You have reserved " + rules.getReservation() + " books, you can't do more reservations.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else {
                                        Toast.makeText(getActivity(), "You have loaned " + rules.getLoan() + " books, return them to reserve another one.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else {
                                    if(currentUser.getBooksLoaned() < 3) {
                                        if(currentUser.getBooksReserved() < 2) {
                                            userReserveBook(currentUser, stock);
                                        }
                                        else {
                                            Toast.makeText(getActivity(), "You have reserved 2 books, you can't do more reservations.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else {
                                        Toast.makeText(getActivity(), "You have loaned 3 books, return them to reserve another one.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            else {
                                Toast.makeText(getActivity(), "There are no books available at this moment.", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }
                }
            }
        });

        btnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity)getActivity()).setViewPager(FragmentStateAdapter.ADD_BOOK_COPY);
            }
        });

        btnEditStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity)getActivity()).setViewPager(FragmentStateAdapter.EDIT_BOOK);
            }
        });

        updateUIInfo();
    }

    /**
     * Updates the UI text, images, etc.
     */
    private void updateUIInfo() {
        if(stock != null) {
            tvTitle.setText(stock.getTitle());
            tvAuthor.setText(stock.getAuthor());
            tvGenre.setText(stock.getGenre());
            tvStock.setText("Stock: " + stock.getNrOfBooks());
            tvDescription.setText(stock.getDescription());
            PicassoClient.downloading(getActivity(), stock.getImageUrl(), iwCover);

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
     * Handles the query result for the method userReserveBook.
     * @param dataSnapshot
     */
    private void getBooks(DataSnapshot dataSnapshot) {
        List<Book> bookList = new ArrayList<>();

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
            dbReference.child("book").child(book.getKey()).child("reserved").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        decrementStock(stock);
                        incrementReservations(currentUser);
                        createReservation(currentUser, book);
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

    /**
     * Increments the user's reservations.
     * @param user
     */
    private void incrementReservations(User user) {
        dbReference.child("user").child(user.getKey()).child("booksReserved").setValue(user.getBooksReserved() + 1);
    }

    /**
     * Decrements the stock's number of books available.
     * @param stock
     */
    private void decrementStock(Stock stock) {
        dbReference.child("stock").child(stock.getKey()).child("nrOfBooks").setValue(stock.getNrOfBooks() - 1);
    }

    /**
     * Creates a reservation for book @param book to user @param user.
     * @param user
     * @param book
     */
    private void createReservation(User user, Book book) {
        Rules rules = ((HomeActivity)getActivity()).getRules();
        Date reserveDate = Calendar.getInstance().getTime();
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if(rules == null)
            currentTime += 24 * 60 * 60 * 1_000;    //1 day
        else
            currentTime += rules.getReservation() * 24 * 60 * 60 * 1_000;   //Days
        Calendar cnd = Calendar.getInstance();
        cnd.setTimeInMillis(currentTime);
        final Date expireDate = cnd.getTime();
        Reservation reservation = new Reservation(stock.getKey(), book.getKey(), user.getKey(), reserveDate, expireDate, false);
        String key = dbReference.child("reservation").push().getKey();
        dbReference.child("reservation").child(key).setValue(reservation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getActivity(), "Reservation expires on: " + formatDate(expireDate) + ".", Toast.LENGTH_LONG).show();
                ((HomeActivity)getActivity()).setViewPager(FragmentStateAdapter.HOME_SEARCH_BOOK);
            }
        });
    }


    /**
     * Searches for available books for the stock @param stock
     * @param user
     * @param stock
     */
    private void userReserveBook(final User user, final Stock stock) {
        dbReference.child("book").orderByChild("stockKey").equalTo(stock.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Book book = dataSnapshot.getValue(Book.class);
                if(book != null) {
                    getBooks(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Searches for active reservations for the stock with the unique key @param stockKey
     * @param stockKey
     */
    private void searchReservations(String stockKey) {
        dbReference.child("reservation").orderByChild("stockKey").equalTo(stockKey).addValueEventListener(new ValueEventListener() {
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
     * Handles the query result for the method searchReservations.
     * @param dataSnapshot
     */
    private void getReservations(DataSnapshot dataSnapshot) {
        if(reservationList == null)
            reservationList = new ArrayList<>();
        else
            reservationList.clear();
        if(bookMap == null)
            bookMap = new HashMap<>();
        else
            bookMap.clear();
        if(userMap == null)
            userMap = new HashMap<>();
        else
            userMap.clear();

        reservationDone = false;
        for(DataSnapshot ds:dataSnapshot.getChildren()) {
            Reservation reservation = ds.getValue(Reservation.class);
            if(reservation != null) {
                final String resKey = ds.getKey();
                reservation.setKey(resKey);
                if(!reservation.isCancelled()) {
                    reservationList.add(reservation);
                    dbReference.child("book").child(reservation.getBookKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Book book = dataSnapshot.getValue(Book.class);
                            if(book != null) {
                                book.setKey(dataSnapshot.getKey());
                                bookMap.put(resKey, book);
                                if(DEBUG)
                                    Log.e("Book", "Added");
                                setupAdapter();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    dbReference.child("user").child(reservation.getUserKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if(user != null) {
                                user.setKey(dataSnapshot.getKey());
                                userMap.put(resKey, user);
                                if(DEBUG)
                                    Log.e("User", "Added");
                                setupAdapter();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }
        if(DEBUG)
            Log.e("Reserv", "Done");
        reservationDone = true;
        setupAdapter();


    }

    /**
     * Creates the adapter for the ListView.
     */
    private void setupAdapter() {
        int rSize = reservationList.size();
        if(reservationDone && rSize == bookMap.size() && rSize == userMap.size() && bookMap.size() ==  userMap.size()) {
            if(DEBUG)
                Log.d("Adapter", "Final");

            reservationAdapter = new ReservationAdapter(getActivity(), reservationList, bookMap, userMap);
            lwReservation.setAdapter(reservationAdapter);
        }
    }

    /**
     * Increments user's loans.
     * @param user
     */
    private void incrementLoans(User user) {
        dbReference.child("user").child(user.getKey()).child("booksLoaned").setValue(user.getBooksLoaned() + 1);
    }

    /**
     * Creates a loan for the book @param book to the user @param user.
     * @param user
     * @param book
     */
    private void createLoan(final User user, Book book) {
        incrementLoans(user);
        String bookKey = book.getKey();
        book.setKey(null);
        book.setReserved(false);
        book.setLoaned(true);
        dbReference.child("book").child(bookKey).setValue(book);

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
                }
                else {
                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * The custom adapter created especially for this page.
     **/
    class ReservationAdapter extends BaseAdapter {

        private Context context;
        private List<Reservation> reservationList;
        private HashMap<String, Book> bookMap;
        private HashMap<String, User> userMap;
        private LayoutInflater inflater;

        public ReservationAdapter(Context context, List<Reservation> reservationList, HashMap<String, Book> bookMap, HashMap<String, User> userMap) {
            this.context = context;
            this.reservationList = reservationList;
            this.bookMap = bookMap;
            this.userMap = userMap;

            if(DEBUG)
                for(int i=0; i<reservationList.size(); i++) {
                    Reservation res = reservationList.get(i);
                    Log.e("Adapter", "(" + i + ") | Reservation: " + res.getKey() + ", " + res.getBookKey() +
                    ", " + res.getUserKey() + " | " + bookMap.get(res.getKey()).getKey() + " | " + userMap.get(res.getKey()).getKey());
                }
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(inflater == null)
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null)
                convertView = inflater.inflate(R.layout.copy_list_row, parent, false);

            BookFragment.ReservationHolder holder = new BookFragment.ReservationHolder(convertView);
            final Reservation res = reservationList.get(position);
            final String resKey = res.getKey();
            final User user = userMap.get(resKey);
            final Book book = bookMap.get(resKey);

            if(reservationList.size() > 0) {
                if(user != null)
                    holder.tvName.setText(user.getName());
                if(book != null) {
                    holder.tvBookKey.setText(book.getKey());
                    holder.tvLocation.setText(book.getLocation());
                    switch(book.getState()) {
                        case Book.STATE_EXCELLENT:
                            holder.tvState.setTextColor(ContextCompat.getColor(context, R.color.colorExcellent));
                            break;
                        case Book.STATE_VERY_GOOD:
                            holder.tvState.setTextColor(ContextCompat.getColor(context, R.color.colorVeryGood));
                            break;
                        case Book.STATE_GOOD:
                            holder.tvState.setTextColor(ContextCompat.getColor(context, R.color.colorGood));
                            break;
                        case Book.STATE_FAIR:
                            holder.tvState.setTextColor(ContextCompat.getColor(context, R.color.colorFair));
                            break;
                        case Book.STATE_POOR:
                            holder.tvState.setTextColor(ContextCompat.getColor(context, R.color.colorPoor));
                            break;
                    }
                    holder.tvState.setText(Book.stateDesc[book.getState()]);
                }

            }



            holder.btnLoan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(DEBUG)
                        Log.e("UserRes", userMap.get(resKey).getName());
                    final User user = userMap.get(resKey);
                    final Book book = bookMap.get(resKey);
                    dbReference.child("reservation").child(resKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                if(DEBUG)
                                    Log.e("DELETE", "Success " + resKey);
                                dbReference.child("user").child(user.getKey()).child("booksReserved").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Integer booksReserved = dataSnapshot.getValue(Integer.class);
                                        if(booksReserved != null) {
                                            if(DEBUG)
                                                Log.e("Reserved", "" + booksReserved);
                                            dbReference.child("user").child(user.getKey()).child("booksReserved").setValue(booksReserved - 1);
                                            //dbReference.child("book").child(reservationList.get(position).getBookKey()).child("reserved").setValue(false);

                                            //Loan
                                            if(DEBUG)
                                                Log.e("ResKey", resKey);
                                            createLoan(user, book);
                                            //
                                            searchReservations(stock.getKey());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                            else {

                            }
                        }
                    });
                }
            });

            holder.btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final User user = userMap.get(resKey);
                    final Book book = bookMap.get(resKey);
                    final String stockKey = stock.getKey();
                    dbReference.child("reservation").child(resKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                if(DEBUG)
                                    Log.e("DELETE", "Success " + resKey);
                                dbReference.child("user").child(user.getKey()).child("booksReserved").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Integer booksReserved = dataSnapshot.getValue(Integer.class);
                                        if(booksReserved != null) {
                                            if(DEBUG)
                                                Log.e("Reserved", "" + booksReserved);
                                            dbReference.child("user").child(user.getKey()).child("booksReserved").setValue(booksReserved - 1);
                                            dbReference.child("book").child(book.getKey()).child("reserved").setValue(false);
                                            dbReference.child("stock").child(stockKey).child("nrOfBooks").setValue(stock.getNrOfBooks() + 1);

                                            //Loan
                                            if(DEBUG)
                                                Log.e("ResKey", resKey);
                                            //
                                            searchReservations(stock.getKey());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                            else {

                            }
                        }
                    });
                }
            });

            return convertView;
        }
    }

    /**
     * A holder class, that holds the UI elements for the ReservationAdapter class.
     */
    class ReservationHolder {
        TextView tvName, tvBookKey, tvState, tvLocation;
        Button btnLoan, btnCancel;

        public ReservationHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tvName);
            tvBookKey = (TextView) view.findViewById(R.id.tvBookKey);
            tvState = (TextView) view.findViewById(R.id.tvState);
            tvLocation = (TextView) view.findViewById(R.id.tvLocation);

            btnLoan = (Button) view.findViewById(R.id.btnLoan);
            btnCancel = (Button) view.findViewById(R.id.btnCancel);
        }
    }
}
