package com.example.ali.biblioteca.fragments;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ali.biblioteca.R;
import com.example.ali.biblioteca.activities.HomeActivity;
import com.example.ali.biblioteca.model.Stock;
import com.example.ali.biblioteca.utility.FragmentStateAdapter;
import com.example.ali.biblioteca.utility.PicassoClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ali on 06.01.2018.
 */

public class SearchBookFragment extends Fragment {
    private static final String TAG = "SearchBookFragment";

    private ListView lwBooks;
    private DatabaseReference dbRef;
    private List<Stock> stockList;
    private StockAdapter stockAdapter;
    private View view;

    public static final String CRITERIA_TITLE = "title";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_book, container, false);

        lwBooks = (ListView) view.findViewById(R.id.lwBooks);
        dbRef = FirebaseDatabase.getInstance().getReference("stock");

        Log.e(TAG, "Started");
        lwBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Stock stock = (Stock) stockAdapter.getItem(position);
                ((HomeActivity)getActivity()).setTransferStock(stock);
                ((HomeActivity)getActivity()).setViewPager(FragmentStateAdapter.SEARCH_SHOW_BOOK);
            }
        });

        selectAll(CRITERIA_TITLE);

        return view;
    }


    public void selectSpecificStock(String searchCriteria, final String genre, final String text) {
        if(dbRef == null)
            dbRef = FirebaseDatabase.getInstance().getReference("stock");
        dbRef.orderByChild(searchCriteria).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSpecificUpdates(dataSnapshot, genre, text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void selectAll(String orderCriteria) {
        if(dbRef == null)
            dbRef = FirebaseDatabase.getInstance().getReference("stock");
        dbRef.orderByChild(orderCriteria).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getUpdates(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getSpecificUpdates(DataSnapshot dataSnapshot, String genre, String text) {
        if(stockList == null)
            stockList = new ArrayList<>();
        else
            stockList.clear();

        String formattedText = text.toLowerCase();
        String formattedGenre = genre.toLowerCase().trim();
        if(formattedGenre.isEmpty()) {
            for(DataSnapshot ds: dataSnapshot.getChildren()) {
                Stock stock = ds.getValue(Stock.class);
                if(stock.getTitle().toLowerCase().contains(formattedText) || stock.getAuthor().toLowerCase().contains(formattedText)) {
                    stock.setKey(ds.getKey());
                    stockList.add(stock);
                }
            }
        }
        else {
            for(DataSnapshot ds: dataSnapshot.getChildren()) {
                Stock stock = ds.getValue(Stock.class);
                if(stock.getGenre().toLowerCase().equals(formattedGenre) &&
                        (stock.getTitle().toLowerCase().contains(formattedText) || stock.getAuthor().toLowerCase().contains(formattedText))) {
                    stock.setKey(ds.getKey());
                    stockList.add(stock);
                }
            }
        }

        if(lwBooks == null)
            lwBooks = (ListView) view.findViewById(R.id.lwBooks);

        stockAdapter = new StockAdapter(getContext(), stockList);
        lwBooks.setAdapter(stockAdapter);

        if(stockList.size() == 0) {
            Toast.makeText(getContext(), "No stock found matching this criteria.", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUpdates(DataSnapshot dataSnapshot) {
        if(stockList == null)
            stockList = new ArrayList<>();
        else
            stockList.clear();

        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            Stock stock = ds.getValue(Stock.class);
            stock.setKey(ds.getKey());

            stockList.add(stock);
        }

        if(lwBooks == null)
            lwBooks = (ListView) view.findViewById(R.id.lwBooks);

        stockAdapter = new StockAdapter(getActivity(), stockList);
        lwBooks.setAdapter(stockAdapter);

        if(stockList.size() == 0) {
            Toast.makeText(getContext(), "No stock found matching this criteria.", Toast.LENGTH_SHORT).show();
        }

    }

    public void refreshFragment() {
        selectAll(CRITERIA_TITLE);
    }

    class StockAdapter extends BaseAdapter {

        private Context context;
        private List<Stock> stockList;
        private LayoutInflater inflater;

        public StockAdapter(Context context, List<Stock> stockList) {
            this.context = context;
            this.stockList = stockList;
        }

        @Override
        public int getCount() {
            return stockList.size();
        }

        @Override
        public Object getItem(int position) {
            return stockList.get(position);
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
                convertView = inflater.inflate(R.layout.book_list_row, parent, false);

            StockHolder holder = new StockHolder(convertView);
            Stock stock = stockList.get(position);

            holder.tvTitle.setText(stock.getTitle());
            holder.tvAuthor.setText(stock.getAuthor());
            holder.tvGenre.setText(stock.getGenre());
            PicassoClient.downloading(context, stockList.get(position).getImageUrl(), holder.iwBookCover);
            if(stock.getNrOfBooks() > 0)
                holder.iwAvailability.setImageResource(R.drawable.ic_action_available);
            else
                holder.iwAvailability.setImageResource(R.drawable.ic_action_not_available);



            return convertView;
        }
    }

    class StockHolder {

        ImageView iwBookCover, iwAvailability;
        TextView tvTitle, tvAuthor, tvGenre;

        public StockHolder(View view) {
            iwBookCover = (ImageView) view.findViewById(R.id.iwBookCover);
            iwAvailability = (ImageView) view.findViewById(R.id.iwAvailability);
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvAuthor = (TextView) view.findViewById(R.id.tvAuthor);
            tvGenre = (TextView) view.findViewById(R.id.tvGenre);
        }

    }
}
