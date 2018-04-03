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
import android.widget.TextView;
import android.widget.Toast;

import com.example.ali.biblioteca.R;
import com.example.ali.biblioteca.activities.HomeActivity;
import com.example.ali.biblioteca.model.Rules;
import com.example.ali.biblioteca.model.User;
import com.example.ali.biblioteca.utility.FragmentStateAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

/**
 * Created by Ali on 08.01.2018.
 */

public class RulesFragment extends Fragment {
    private static final String TAG = "RulesFragment";
    private TextView tvReservation, tvLoan, tvTax;
    private EditText etReservation, etLoan, etTax;
    private Button btnReservation, btnLoan, btnTax;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rules_layout, container, false);
        Log.e(TAG, "Started");
        loadElements(view);

        return view;
    }


    public void refreshFragment() {
            etReservation.setText("");
            etLoan.setText("");
            etTax.setText("");

            FirebaseDatabase.getInstance().getReference("rules").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Rules rules = dataSnapshot.getValue(Rules.class);
                    if(rules != null) {
                        //Toast.makeText(getActivity(), "Rules: " + rules, Toast.LENGTH_SHORT).show();
                        tvReservation.setText("Reservation days: " + rules.getReservation());
                        tvLoan.setText("Loan days: " + rules.getLoan());
                        tvTax.setText("Tax/day: " + rules.getTax());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }

    private void loadElements(View view) {
        tvReservation = (TextView) view.findViewById(R.id.tvReservation);
        tvLoan = (TextView) view.findViewById(R.id.tvLoan);
        tvTax = (TextView) view.findViewById(R.id.tvTax);

        etReservation = (EditText) view.findViewById(R.id.etReservation);
        etLoan = (EditText) view.findViewById(R.id.etLoan);
        etTax = (EditText) view.findViewById(R.id.etTax);

        btnReservation = (Button) view.findViewById(R.id.btnReservation);
        btnLoan = (Button) view.findViewById(R.id.btnLoan);
        btnTax = (Button) view.findViewById(R.id.btnTax);

        btnReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareReservation();
            }
        });

        btnLoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareLoan();
            }
        });

        btnTax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareTax();
            }
        });

    }

    private void prepareReservation(){
        try {
            int reservation = Integer.parseInt(etReservation.getText().toString().trim());
            changeReservation(reservation);
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Please insert the number of days for a reservation.", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void changeReservation(final int reservation) {
        FirebaseDatabase.getInstance().getReference("rules").child("reservation").setValue(reservation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Reservation changed.", Toast.LENGTH_SHORT).show();
                    tvReservation.setText("Reservation days: " + reservation);
                }
                else {
                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void prepareLoan(){
        try {
            int loan = Integer.parseInt(etLoan.getText().toString().trim());
            changeLoan(loan);
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Please insert the number of days for a loan.", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void changeLoan(final int loan) {
        FirebaseDatabase.getInstance().getReference("rules").child("loan").setValue(loan).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Loan changed.", Toast.LENGTH_SHORT).show();
                    tvLoan.setText("Loan days: " + loan);
                }
                else {
                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void prepareTax(){
        try {
            int tax = Integer.parseInt(etTax.getText().toString().trim());
            changeTax(tax);
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Please insert the new tax/day.", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void changeTax(final int tax) {
        FirebaseDatabase.getInstance().getReference("rules").child("tax").setValue(tax).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Tax changed.", Toast.LENGTH_SHORT).show();
                    tvTax.setText("Tax/day: " + tax);
                }
                else {
                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
