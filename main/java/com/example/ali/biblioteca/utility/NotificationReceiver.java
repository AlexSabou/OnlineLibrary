package com.example.ali.biblioteca.utility;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.ali.biblioteca.activities.HomeActivity;
import com.example.ali.biblioteca.model.Loan;
import com.example.ali.biblioteca.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Ali on 10.01.2018.
 */

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        //Log.e("Receiver", "Notification");
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        if(fbUser != null) {
            String userId = fbUser.getUid();
            dbRef.child("user").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final User user = dataSnapshot.getValue(User.class);
                    if(user != null) {
                        user.setKey(dataSnapshot.getKey());
                        dbRef.child("loan").orderByChild("userKey").equalTo(user.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                getLoans(context, user, dataSnapshot);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void getLoans(final Context context, final User user, DataSnapshot dataSnapshot) {
        List<Loan> loanList = new ArrayList<>();


        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            Loan loan = ds.getValue(Loan.class);
            if(loan != null) {
                loan.setKey(ds.getKey());
                if(!loan.isReturned()) {
                    long difference = loan.getReturnDate().getTime() - Calendar.getInstance().getTimeInMillis();
                    if(difference < 0) {
                        sendNotification(context, user, "Some of your loans already expired.");
                        break;
                    }
                    else if((difference/(24 * 60 * 60 * 1_000)) < 3) {
                        sendNotification(context, user, "Some of your loans are about to expired.");
                        break;
                    }
                }
            }
        }
    }

    private void sendNotification(Context context, User user, String text) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent receiveIntent =  new Intent(context, HomeActivity.class);
        receiveIntent.putExtra("Notification", user);
        receiveIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, HomeActivity.NOTIFICATION_REQUEST_CODE, receiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("UTCN Library")
                .setContentText("Some loans are about to expire.")
                .setAutoCancel(true);
        notificationManager.notify(HomeActivity.NOTIFICATION_REQUEST_CODE, builder.build());
    }
}
