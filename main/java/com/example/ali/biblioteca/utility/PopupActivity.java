package com.example.ali.biblioteca.utility;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ali.biblioteca.R;
import com.example.ali.biblioteca.fragments.SearchBookFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ali on 08.01.2018.
 */

public class PopupActivity extends Activity {

    private RadioGroup rgSortBy, rgGenre;
    private ImageView twCancel;
    private RadioButton rbSortBy;
    private RadioButton rbGenreSelected;
    private RadioButton[] rbSort, rbGenre;
    private String[] genres = new String[]{"All", "Action and adventure", "Anthology", "Art", "Autobiographies", "Biographies", "Children's", "Comics", "Cookbooks", "Diaries",
            "Dictionaries", "Drama", "Encyclopedias", "Fantasy", "Guide", "Health", "History", "Horror", "IT", "Journals", "Math", "Mystery", "Poetry",
            "Prayer books", "Religion", "Romance", "Satire", "Science", "Science fiction", "Self help", "Series", "Travel", "Trilogy"};
    private String[] sortBy = new String[]{"Title", "Author"};

    private String orderCriteria, searchGenre;
    private int exitCase = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_window);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        getWindow().setLayout((int) (width * 0.9), (int) (height * 0.8));



        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            orderCriteria = (String) bundle.getString("orderCriteria");
            searchGenre = (String) bundle.getString("searchGenre");
        }

        exitCase = 0;

        loadElements();

    }

    private void loadElements() {
        rgSortBy = (RadioGroup) findViewById(R.id.rgSortBy);
        rgGenre = (RadioGroup) findViewById(R.id.rgGenre);
        twCancel = (ImageView) findViewById(R.id.twCancel);

        rbSort = new RadioButton[sortBy.length];
        for(int i = 0; i < sortBy.length; i++) {
            rbSort[i] = new RadioButton(this);
            rbSort[i].setText(sortBy[i]);
            rgSortBy.addView(rbSort[i]);
            if(orderCriteria != null)
                if(sortBy[i].toLowerCase().equals(orderCriteria))
                    rbSort[i].setChecked(true);
        }

        rbGenre = new RadioButton[genres.length];
        for(int i = 0; i < genres.length; i++) {
            rbGenre[i] = new RadioButton(this);
            rbGenre[i].setText(genres[i]);
            rgGenre.addView(rbGenre[i]);
            if(searchGenre != null)
                if(genres[i].toLowerCase().equals(searchGenre))
                    rbGenre[i].setChecked(true);
        }

        if(searchGenre != null)
            if(searchGenre.isEmpty())
                rbGenre[0].setChecked(true);

        twCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPopupClose();
            }
        });
    }

    private void onPopupClose() {
        exitCase = 1;
        int checkedRBSort = rgSortBy.getCheckedRadioButtonId(),
                checkedRBGenre = rgGenre.getCheckedRadioButtonId();
        rbSortBy = (RadioButton) findViewById(checkedRBSort);
        rbGenreSelected = (RadioButton) findViewById(checkedRBGenre);

        Intent intent = new Intent();

        if(rbSortBy != null)
            intent.putExtra("orderCriteria", rbSortBy.getText());
        else
            intent.putExtra("orderCriteria", SearchBookFragment.CRITERIA_TITLE);
        if(rbGenreSelected != null) {
            if(rbGenreSelected.getText().equals("All"))
                intent.putExtra("genre", "");
            else
                intent.putExtra("genre", rbGenreSelected.getText());
        }
        else
            intent.putExtra("genre", "");
        setResult(99, intent);
        finish();
    }

    private void onPopupBack() {
        exitCase = 2;
        Intent intent = new Intent();
        intent.putExtra("orderCriteria", orderCriteria);
        intent.putExtra("genre", searchGenre);
        setResult(99, intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        onPopupBack();
    }

    @Override
    public void finish() {
        switch (exitCase) {
            case 1:
            case 2:
                super.finish();
                break;
            default:
                Intent intent = new Intent();
                intent.putExtra("orderCriteria", orderCriteria);
                intent.putExtra("genre", searchGenre);
                setResult(99, intent);
                break;
        }
        super.finish();
    }
}
