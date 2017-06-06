package com.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.inventoryapp.database.DatabaseContract.Products;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PRODUCT_LOADER = 0;
    @BindView(R.id.list_view) ListView productListView;
    @BindView(R.id.empty_view) View emptyView;
    private ProductCursorAdapter productCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        productListView.setEmptyView(emptyView);
        productCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(productCursorAdapter);
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this).forceLoad();
    }

    @OnItemClick(R.id.list_view)
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        Uri currentProductUri = ContentUris.withAppendedId(Products.CONTENT_URI, id);
        intent.setData(currentProductUri);
        startActivity(intent);
    }

    @OnClick(R.id.fab)
    public void fabOnclickListener() {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, Products.CONTENT_URI, Products.projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        productCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productCursorAdapter.swapCursor(null);
    }
}
