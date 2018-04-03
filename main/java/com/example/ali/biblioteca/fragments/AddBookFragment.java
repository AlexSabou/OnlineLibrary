package com.example.ali.biblioteca.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.ali.biblioteca.R;
import com.example.ali.biblioteca.activities.HomeActivity;
import com.example.ali.biblioteca.model.Book;
import com.example.ali.biblioteca.model.Stock;
import com.example.ali.biblioteca.utility.FragmentStateAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Ali on 07.01.2018.
 */

public class AddBookFragment extends Fragment {
    private static final String TAG = "AddBookFragment";
    private EditText etLocation;
    private RadioGroup rgState;
    private RadioButton rbSelected;
    private Button btnAdd;
    private DatabaseReference dbReference;
    private Stock stock;
    private RadioButton[] radioButtons = new RadioButton[5];

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_book, container, false);

        stock = ((HomeActivity)getActivity()).getTransferStock();
        dbReference = FirebaseDatabase.getInstance().getReference();
        Log.e(TAG, "Started");
        loadElements(view);

        return view;
    }

    /**
     * Refreshes the page's data.
     */
    public void refreshFragment() {
        etLocation.setText("");
        stock = ((HomeActivity)getActivity()).getTransferStock();
    }

    /**
     * Method that loads the UI elements needed for the page.
     * @param view
     */
    private void loadElements(final View view) {
        rgState = (RadioGroup) view.findViewById(R.id.rgState);
        etLocation = (EditText) view.findViewById(R.id.etLocation);

        for(int i=0; i<5; i++) {
            radioButtons[i] = new RadioButton(getActivity());
            rgState.addView(radioButtons[i]);
            radioButtons[i].setText(Book.stateDesc[i]);
            radioButtons[i].setTextColor(getResources().getColor(R.color.colorWhite));
        }
        radioButtons[0].setChecked(true);

        btnAdd = (Button) view.findViewById(R.id.btnAddBook);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareAddBook(view);
            }
        });
    }

    /**
     * Checks to see if all the data from the UI are correct.
     * @param view
     */
    private void prepareAddBook(View view) {
        int checkedId = rgState.getCheckedRadioButtonId();
        rbSelected = (RadioButton) view.findViewById(checkedId);
        int state = 0;
        for(int i=0; i<5; i++) {
            if(rbSelected.getText().toString().equals(Book.stateDesc[i])) {
                state = i;
                break;
            }
        }

        String location = etLocation.getText().toString().trim();

        if(state < 0 || state > 4) {
            Toast.makeText(getActivity(), "Please insert a state.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(location.isEmpty()) {
            Toast.makeText(getActivity(), "Please insert a location.", Toast.LENGTH_SHORT).show();
            return;
        }

        addBook(state, location);
    }

    /**
     * Creates a new book with the state=@param state and location=@param location.
     * @param state
     * @param location
     */
    private void addBook(final int state, final String location) {
        String key = dbReference.push().getKey();
        Book book = new Book(stock.getKey(), state, location, false, false);
        dbReference.child("book").child(key).setValue(book).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Book added.", Toast.LENGTH_SHORT).show();
                    ((HomeActivity)getActivity()).setViewPager(FragmentStateAdapter.HOME_SEARCH_BOOK);
                    String stockKey = stock.getKey();
                    stock.setNrOfBooks(stock.getNrOfBooks() + 1);
                    stock.setKey(null);
                    dbReference.child("stock").child(stockKey).setValue(stock).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.d("UPDATE", "SUCCES");
                            }
                            else {
                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
