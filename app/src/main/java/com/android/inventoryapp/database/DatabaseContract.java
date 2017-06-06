package com.android.inventoryapp.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ART_F on 2017-06-05.
 */

public class DatabaseContract {

    public static final String CONTENT_AUTHORITY = "com.android.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";

    private DatabaseContract() {
    }

    public static class Products implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;


        public static final String TABLE_NAME = "inventoryOfProducts";
        public static final String PRODUCT_NAME = "productName";
        public static final String CURRENT_QUANTITY = "currentQuantity";
        public static final String PRODUCT_PRICE = "productPrice";

        public static final String[] projection = {
                _ID,
                PRODUCT_NAME,
                CURRENT_QUANTITY,
                PRODUCT_PRICE
        };

        static final String SQL_CREATE_HABITS =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + PRODUCT_NAME + " TEXT,"
                        + CURRENT_QUANTITY + " TEXT,"
                        + PRODUCT_PRICE + " TEXT"
                        + ");";

        static final String SQL_DELETE_HABITS =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
