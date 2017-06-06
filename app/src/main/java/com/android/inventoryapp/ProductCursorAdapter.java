package com.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.inventoryapp.database.DatabaseContract.Products;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ART_F on 2017-06-05.
 */

public class ProductCursorAdapter extends CursorAdapter {

    private Context context;

    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.listview_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String productNameString = cursor.getString(cursor.getColumnIndex(Products.PRODUCT_NAME));
        String currentQuantityString = cursor.getString(cursor.getColumnIndex(Products.CURRENT_QUANTITY));
        String productPriceString = cursor.getString(cursor.getColumnIndex(Products.PRODUCT_PRICE));

        holder.productName.setText(productNameString);
        holder.currentQuantity.setText(currentQuantityString);
        holder.price.setText(productPriceString);

        holder.soldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButterKnife.bind(this, v);
                int currentQuantityString = cursor.getInt(cursor.getColumnIndex(Products.CURRENT_QUANTITY));
                long id = cursor.getLong(cursor.getColumnIndex(Products._ID));
                Uri currentProductUri = ContentUris.withAppendedId(Products.CONTENT_URI, id);

                int newCurrentQuantity = currentQuantityString - 1;
                if (0 > newCurrentQuantity) {
                    Toast.makeText(context, context.getString(R.string.detail_quantity_isnt_enough), Toast.LENGTH_SHORT).show();
                    return;
                }
                ContentValues values = new ContentValues();
                values.put(Products.CURRENT_QUANTITY, String.valueOf(newCurrentQuantity));

                int rowsAffected = context.getContentResolver().update(currentProductUri, values, null, null);
            }
        });
    }


    static class ViewHolder {
        @BindView(R.id.productName) TextView productName;
        @BindView(R.id.currentQuantity) TextView currentQuantity;
        @BindView(R.id.price) TextView price;
        @BindView(R.id.soldButton) Button soldButton;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
