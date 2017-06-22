package com.android.inventoryapp.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.android.inventoryapp.database.DatabaseContract.Products;

/**
 * Created by ART_F on 2017-06-05.
 */

public class DatabaseProvider extends ContentProvider {

    public static final String LOG_TAG = DatabaseProvider.class.getSimpleName();
    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private DatabaseHelper databaseHelper;

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(Products.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = Products._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(Products.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {

        String name = values.getAsString(Products.PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        Integer currentQuantity = values.getAsInteger(Products.CURRENT_QUANTITY);
        if (currentQuantity == null || 0 > currentQuantity) {
            throw new IllegalArgumentException("Product requires current quantity");
        }

        Double price = values.getAsDouble(Products.PRODUCT_PRICE);
        if (price == null || 0.00 > price) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        String productPictureUri = values.getAsString(Products.PRODUCT_IMAGE);
        if (productPictureUri == null) {
            throw new IllegalArgumentException("Product requires image");
        }

        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        long id = database.insert(Products.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = Products._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {
            return 0;
        }

        if (values.containsKey(Products.PRODUCT_NAME)) {
            String name = values.getAsString(Products.PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (values.containsKey(Products.CURRENT_QUANTITY)) {
            Integer currentQuantity = values.getAsInteger(Products.CURRENT_QUANTITY);
            if (currentQuantity == null || 0 > currentQuantity) {
                throw new IllegalArgumentException("Product requires current quantity");
            }
        }


        if (values.containsKey(Products.PRODUCT_PRICE)) {
            Double price = values.getAsDouble(Products.PRODUCT_PRICE);
            if (price == null || 0.00 > price) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        if (values.containsKey(Products.PRODUCT_IMAGE)) {
            String productPictureUri = values.getAsString(Products.PRODUCT_IMAGE);
            if (productPictureUri == null) {
                throw new IllegalArgumentException("Product requires image");
            }
        }

        SQLiteOpenHelper databaseHelper = new DatabaseHelper(getContext());
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int rowsUpdated = database.update(Products.TABLE_NAME, values, selection, selectionArgs);
        databaseHelper.close();

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(Products.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = Products._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(Products.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return Products.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return Products.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


}
