package com.android.inventoryapp;


import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.inventoryapp.database.DatabaseContract.Products;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;


public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    @BindView(R.id.loadImage) Button loadImage;
    @BindView(R.id.productPicture) ImageView productPicture;
    @BindView(R.id.productName) EditText productNameEdit;
    @BindView(R.id.currentQuantity) EditText currentQuantityEdit;
    @BindView(R.id.productPrice) EditText productPriceEdit;
    @BindView(R.id.soldQuantity) EditText soldQuantity;
    @BindView(R.id.receivedQuantity) EditText receivedQuantity;
    @BindView(R.id.saveNewProduct) Button saveNewProduct;
    @BindView(R.id.editView) LinearLayout editView;
    @BindViews({R.id.productNameSave, R.id.currentQuantitySave, R.id.productPriceSave}) List<Button> buttonList;
    String mCurrentPhotoPath;
    private AlertDialog dialog;
    private Uri currentProductUri;
    private int currentQuantity;
    private String productName;
    private Uri imageUri;

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
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

    }

    @OnClick(R.id.orderMore)
    public void orderMore() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ruacalendarpro@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "We would like to order more " + productName);
        startActivity(intent);
    }

    @OnClick(R.id.loadImage)
    public void loadImage() {
        new DialogPicture(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap imageBitmap = null;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    try {
                        imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    imageUri = saveTakenPic(imageBitmap);
                    File file = new File (mCurrentPhotoPath);
                    file.delete();
                    break;
                case REQUEST_GALLERY:
                    imageUri = data.getData();
                    try {
                        imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
            productPicture.setImageBitmap(imageBitmap);

        } else {
            productPicture.setImageResource(R.drawable.nopic);
        }

        if ((currentProductUri != null && imageUri != null)) {
            ContentValues values = new ContentValues();
            values.put(Products.PRODUCT_IMAGE, imageUri.toString());
            updateInfo(values);
        }


        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private Uri saveTakenPic(Bitmap bitmap) {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/inventoryApp");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = new File(directory, imageFileName + "SGM.jpg");

        try {
            image.createNewFile();
            FileOutputStream out = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(image);

    }

    @OnClick({R.id.saveNewProduct, R.id.productNameSave, R.id.currentQuantitySave, R.id.productPriceSave, R.id.soldQuantitySave, R.id.receivedQuantitySave})
    public void saveProduct(View view) {

        String productNameString = productNameEdit.getText().toString().trim();
        String currentQuantityString = currentQuantityEdit.getText().toString().trim();
        String productPriceString = productPriceEdit.getText().toString().trim();

        if(imageUri == null){
            Toast.makeText(this, getString(R.string.detail_new_product_requirement), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(productNameString)
                || TextUtils.isEmpty(currentQuantityString)
                || TextUtils.isEmpty(productPriceString)
                || TextUtils.isEmpty(imageUri.toString())) {
            Toast.makeText(this, getString(R.string.detail_new_product_requirement), Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();

        if (currentProductUri == null) {

            values.put(Products.PRODUCT_NAME, productNameString);
            values.put(Products.CURRENT_QUANTITY, currentQuantityString);
            values.put(Products.PRODUCT_PRICE, productPriceString);
            values.put(Products.PRODUCT_IMAGE, imageUri.toString());

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
            updateInfo(values);
        }
    }

    private void updateInfo(ContentValues values) {
        int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
        if (rowsAffected == 0) {
            Toast.makeText(this, getString(R.string.editor_update_product_failed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_update_product_successful), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, currentProductUri, Products.projectionDetail, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            productName = cursor.getString(cursor.getColumnIndex(Products.PRODUCT_NAME));
            currentQuantity = cursor.getInt(cursor.getColumnIndex(Products.CURRENT_QUANTITY));
            double productPrice = cursor.getDouble(cursor.getColumnIndex(Products.PRODUCT_PRICE));
            String imageStringUri = cursor.getString(cursor.getColumnIndex(Products.PRODUCT_IMAGE));

            imageUri = Uri.parse(imageStringUri);
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            productNameEdit.setText(productName);
            currentQuantityEdit.setText(String.valueOf(currentQuantity));
            productPriceEdit.setText(String.valueOf(productPrice));
            if (imageBitmap != null) {
                productPicture.setImageBitmap(imageBitmap);
            } else {
                productPicture.setImageResource(R.drawable.nopic);
            }


        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameEdit.setText("");
        currentQuantityEdit.setText("");
        productPriceEdit.setText("");
        productPicture.setImageResource(R.drawable.nopic);
    }

    @OnClick(R.id.delete)
    public void deleteRecord() {
        new DialogConfirmation(this);
    }

    private void deleteProduct() {
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
            deleteProduct();
        }
    }

    public class DialogPicture {

        Context context;

        DialogPicture(Context context) {
            this.context = context;
            dialog = new AlertDialog.Builder(context)
                    .setView(R.layout.dialog_picture)
                    .create();
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ButterKnife.bind(this, dialog);
        }

        @OnClick(R.id.loadFromGallery)
        public void loadPicture() {
            Intent takePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(takePictureIntent, REQUEST_GALLERY);
        }

        @OnClick(R.id.takePicture)
        public void takePicture() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    imageUri = FileProvider.getUriForFile(context,
                            "com.android.inventoryapp.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(takePictureIntent, REQUEST_CAMERA);
                }
            }
        }

        private File createImageFile() throws IOException {
            String imageFileName = "temp";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            mCurrentPhotoPath = image.getAbsolutePath();
            return image;
        }
    }
}
