package com.android.inventoryapp;


import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.inventoryapp.database.DatabaseContract;
import com.android.inventoryapp.database.DatabaseContract.Products;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;


public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PET_LOADER = 0;
    @BindView(R.id.productName) EditText productNameEdit;
    @BindView(R.id.currentQuantity) EditText currentQuantityEdit;
    @BindView(R.id.productPrice) EditText productPriceEdit;
    @BindView(R.id.soldQuantity) EditText soldQuantity;
    @BindView(R.id.receivedQuantity) EditText receivedQuantity;
    @BindView(R.id.saveNewProduct) Button saveNewProduct;
    @BindView(R.id.editView) LinearLayout editView;
    @BindViews({R.id.productNameSave, R.id.currentQuantitySave, R.id.productPriceSave}) List<Button> buttonList;
    private Uri currentProductUri;
    private boolean productHasChanged = false;
    private int currentQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        if (currentProductUri == null) {
            for (Button button : buttonList) {
                button.setVisibility(View.GONE);
            }
            editView.setVisibility(View.GONE);
            setTitle(getString(R.string.detail_activity_title_new_product));
        } else {
            saveNewProduct.setVisibility(View.GONE);
            setTitle(getString(R.string.detail_activity_title_edit_product));
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

    }

    @OnTouch({R.id.productName, R.id.currentQuantity, R.id.productPrice, R.id.soldQuantity, R.id.receivedQuantity})
    public boolean onTouchListener() {
        productHasChanged = true;
        return false;
    }

    @OnClick(R.id.orderMore)
    public void orderMore() {
        String email = "mailto:ruacalendarpro@gmail.com";
        Uri addressUri = Uri.parse(email);
        Intent intent = new Intent(Intent.ACTION_SENDTO, addressUri);
        startActivity(intent);
    }

    @OnClick({R.id.saveNewProduct, R.id.productNameSave, R.id.currentQuantitySave, R.id.productPriceSave, R.id.soldQuantitySave, R.id.receivedQuantitySave})
    public void saveProduct(View view) {

        String productNameString = productNameEdit.getText().toString().trim();
        String currentQuantityString = currentQuantityEdit.getText().toString().trim();
        String productPriceString = productPriceEdit.getText().toString().trim();

        if (TextUtils.isEmpty(productNameString) || TextUtils.isEmpty(currentQuantityString) || TextUtils.isEmpty(productPriceString)) {
            Toast.makeText(this, getString(R.string.detail_new_product_requirement), Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();

        if (currentProductUri == null) {

            values.put(Products.PRODUCT_NAME, productNameString);
            values.put(Products.CURRENT_QUANTITY, currentQuantityString);
            values.put(Products.PRODUCT_PRICE, productPriceString);

            Uri newUri = getContentResolver().insert(Products.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            int newCurrentQuantity;
            switch (view.getId()) {
                case R.id.productNameSave:
                    values.put(Products.PRODUCT_NAME, productNameString);
                    break;
                case R.id.currentQuantitySave:
                    values.put(Products.CURRENT_QUANTITY, currentQuantityString);
                    break;
                case R.id.productPriceSave:
                    values.put(Products.PRODUCT_PRICE, productPriceString);
                    break;
                case R.id.soldQuantitySave:
                    int soldQuantityInt = Integer.valueOf(soldQuantity.getText().toString());
                    newCurrentQuantity = currentQuantity - soldQuantityInt;
                    if (0 > newCurrentQuantity) {
                        Toast.makeText(this, getString(R.string.detail_quantity_isnt_enough), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    values.put(Products.CURRENT_QUANTITY, String.valueOf(newCurrentQuantity));
                    break;
                case R.id.receivedQuantitySave:
                    int receivedQuantityInt = Integer.valueOf(receivedQuantity.getText().toString());
                    newCurrentQuantity = currentQuantity + receivedQuantityInt;
                    values.put(Products.CURRENT_QUANTITY, String.valueOf(newCurrentQuantity));
                    break;
            }
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, currentProductUri, DatabaseContract.Products.projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            String productName = cursor.getString(cursor.getColumnIndex(Products.PRODUCT_NAME));
            currentQuantity = cursor.getInt(cursor.getColumnIndex(Products.CURRENT_QUANTITY));
            double productPrice = cursor.getDouble(cursor.getColumnIndex(Products.PRODUCT_PRICE));

            productNameEdit.setText(productName);
            currentQuantityEdit.setText(String.valueOf(currentQuantity));
            productPriceEdit.setText(String.valueOf(productPrice));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameEdit.setText("");
        currentQuantityEdit.setText("");
        productPriceEdit.setText("");
    }

    @OnClick(R.id.delete)
    public void deleteRecord() {
        new DialogConfirmation(this);
    }

    private void deletePet() {
        if (currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful), Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    public class DialogConfirmation {
        AlertDialog dialog;

        DialogConfirmation(Context context) {
            dialog = new AlertDialog.Builder(context)
                    .setView(R.layout.dialog_delete)
                    .create();
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(false);
            ButterKnife.bind(this, dialog);
        }

        @OnClick(R.id.cancelDialog)
        public void cancelDialogOnClick() {
            if (dialog != null) {
                dialog.dismiss();
            }
        }

        @OnClick(R.id.deleteDialog)
        public void deleteDialogOnClick() {
            deletePet();
        }
    }


}
