package com.example.ali.biblioteca.utility;

import android.content.Context;
import android.widget.ImageView;

import com.example.ali.biblioteca.R;
import com.squareup.picasso.Picasso;

/**
 * Created by Ali on 06.01.2018.
 */

public class PicassoClient {
    public static void downloading(Context context, String url, ImageView imageView) {
        if(url != null && url.length() > 0)
            Picasso.with(context).load(url).placeholder(R.drawable.no_image).into(imageView);
        else
            Picasso.with(context).load(R.drawable.no_image).into(imageView);
    }
}
