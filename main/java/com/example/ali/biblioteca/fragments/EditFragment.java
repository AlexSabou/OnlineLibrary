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
import com.example.ali.biblioteca.model.Stock;
import com.example.ali.biblioteca.utility.FragmentStateAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Ali on 10.01.2018.
 */

public class EditFragment extends Fragment {
    private static final String TAG = "EditFragment";

    private EditText etTitle, etAuthor, etDescription, etImageUrl;
    private RadioGroup rgGenre;
    private RadioButton rbSelected;
    private RadioButton[] radioButtons = new RadioButton[Stock.GENRES.length];
    private Button btnEdit;
    private DatabaseReference dbReference;
    private Stock stock;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_stock, container, false);

        dbReference = FirebaseDatabase.getInstance().getReference("stock");
        stock = ((HomeActivity)getActivity()).getTransferStock();
        Log.e(TAG, "Started");

        loadElements(view);


        return view;
    }

    /**
     * Refreshes the page's data.
     */
    public void refreshFragment() {
        stock = ((HomeActivity)getActivity()).getTransferStock();

        if(stock != null) {
            etTitle.setText(stock.getTitle());
            etAuthor.setText(stock.getAuthor());
            etDescription.setText(stock.getDescription());
            etImageUrl.setText(stock.getImageUrl());
            for (int i = 0; i < Stock.GENRES.length; i++) {
                if (Stock.GENRES[i].equals(stock.getGenre())) {
                    radioButtons[i].setChecked(true);
                    break;
                }
            }
        }
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

        btnEdit = (Button) view.findViewById(R.id.btnEditStock);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareEditStock(view);
            }
        });
    }

    /**
     * Checks if all the datas from the UI are correct.
     * @param view
     */
    private void prepareEditStock(View view) {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
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
        editStock(title, author, genre, description, imageUrl);
    }

    /**
     * Edits an existing stock.
     * @param title
     * @param author
     * @param genre
     * @param description
     * @param imageUrl
     */
    private void editStock(final String title, final String author, final String genre, final String description, final String imageUrl) {
        final String stockKey = stock.getKey();
        final Stock newStock = new Stock(title, author, genre, description, imageUrl, stock.getNrOfBooks());
        dbReference.child(stockKey).setValue(newStock).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Stock transferStock = newStock;
                    transferStock.setKey(stockKey);
                    ((HomeActivity)getActivity()).setTransferStock(transferStock);
                    ((HomeActivity)getActivity()).setViewPager(FragmentStateAdapter.SEARCH_SHOW_BOOK);
                    Toast.makeText(getContext(), newStock.getTitle() + " has been edited.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
