package com.example.ali.biblioteca.fragments;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.example.ali.biblioteca.model.Stock;
import com.example.ali.biblioteca.utility.FragmentStateAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Ali on 06.01.2018.
 */

public class AddStockFragment extends Fragment {
    private static final String TAG = "AddStockFragment";
    private EditText etTitle, etAuthor, etDescription, etImageUrl;
    private RadioGroup rgGenre;
    private RadioButton rbSelected;
    private RadioButton[] radioButtons = new RadioButton[Stock.GENRES.length];
    private Button btnAdd;
    private DatabaseReference dbReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_stock, container, false);

        dbReference = FirebaseDatabase.getInstance().getReference("stock");
        Log.e(TAG, "Started");
        loadElements(view);

        return view;
    }

    /**
     * Refreshes the page's data.
     */
    public void refreshFragment() {
        etTitle.setText("");
        etAuthor.setText("");
        etDescription.setText("");
        etImageUrl.setText("");
    }

    /**
     * Method that loads the UI elements needed for the page.
     * @param view
     */
    private void loadElements(final View view) {
        etTitle = (EditText) view.findViewById(R.id.etTitle);
        etAuthor = (EditText) view.findViewById(R.id.etAuthor);
        etDescription = (EditText) view.findViewById(R.id.etDescription);
        etImageUrl = (EditText) view.findViewById(R.id.etImageUrl);

        rgGenre = (RadioGroup) view.findViewById(R.id.rgGenre);

        for(int i=0; i<Stock.GENRES.length; i++) {
            radioButtons[i] = new RadioButton(getActivity());
            rgGenre.addView(radioButtons[i]);
            radioButtons[i].setText(Stock.GENRES[i]);
            radioButtons[i].setTextColor(getResources().getColor(R.color.colorWhite));
        }
        radioButtons[0].setChecked(true);

        btnAdd = (Button) view.findViewById(R.id.btnAddBook);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareAddStock(view);
            }
        });
    }

    /**
     * Checks to see if all the data from the UI are correct.
     * @param view
     */
    private void prepareAddStock(View view) {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        //String genre = etGenre.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();

        rbSelected = view.findViewById(rgGenre.getCheckedRadioButtonId());
        String genre = rbSelected.getText().toString().trim();

        if(title.isEmpty()) {
            Toast.makeText(getActivity(), "Please insert a title.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(author.isEmpty()) {
            Toast.makeText(getActivity(), "Please insert an author.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(genre.isEmpty()) {
            Toast.makeText(getActivity(), "Please select a genre.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(description.isEmpty()) {
            Toast.makeText(getActivity(), "Please insert a description.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(imageUrl.isEmpty()) {
            Toast.makeText(getActivity(), "Please insert an image URL.", Toast.LENGTH_SHORT).show();
            return;
        }
        addStock(title, author, genre, description, imageUrl);
    }

    /**
     * Creates a new stock.
     * @param title
     * @param author
     * @param genre
     * @param description
     * @param imageUrl
     */
    private void addStock(final String title, final String author, final String genre, final String description, final String imageUrl) {

        dbReference.orderByChild("title").equalTo(title).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean found = false;
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    Stock s = ds.getValue(Stock.class);
                    if(s.getAuthor().equals(author)) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    final String key = dbReference.push().getKey();
                    final Stock stock = new Stock(title, author, genre, description, imageUrl, 0);
                    dbReference.child(key).setValue(stock).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Stock newStock = stock;
                                newStock.setKey(key);
                                ((HomeActivity)getActivity()).setTransferStock(newStock);
                                ((HomeActivity)getActivity()).setViewPager(FragmentStateAdapter.ADD_BOOK_COPY);
                                Toast.makeText(getContext(), "Stock added, please add a book.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {  //The stock already exists
                    Toast.makeText(getContext(), "The stock already exists.", Toast.LENGTH_SHORT).show();
                    ((HomeActivity)getActivity()).setViewPager(FragmentStateAdapter.HOME_SEARCH_BOOK);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
