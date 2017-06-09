package com.android.inventoryapp;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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
    private static final int INITIAL_REQUEST = 1337;
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.CAMERA

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkIfAlreadyHavePermission()) {
                requestForSpecificPermission();
            } else {
                startApp();
            }
        } else {
            startApp();
        }
    }

    private void startApp() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        productListView.setEmptyView(emptyView);
        productCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(productCursorAdapter);
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this).forceLoad();
    }

    private boolean checkIfAlreadyHavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestForSpecificPermission() {
        requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case INITIAL_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startApp();
                } else {
                    Toast.makeText(this, "Permissions are necessary", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            checkPermissions();
                        }
                    }, 1500);
                }
                break;
        }
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
        return new CursorLoader(this, Products.CONTENT_URI, Products.projectionList, null, null, null);
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
