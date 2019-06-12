package com.goteacher.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.UserDictionary;
import android.util.Log;
import android.util.TypedValue;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.goteacher.utils.model.Model;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nullable;

import static com.goteacher.Apps.app;

public class Utils {


    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public static synchronized ArrayList<Model> createDataModel(@Nullable QuerySnapshot querySnapshot) {
        ArrayList<Model> data = new ArrayList<>();
        if (querySnapshot == null)
            return data;
        for (@Nullable DocumentSnapshot document : querySnapshot) {
            Model model = new Model();
            try {
                model.setId(document.getId());
                model.setTitle(document.getString(Model.key.title.name()));
                model.setDesc(document.getString(Model.key.desc.name()));
                model.setCategory((List<String>) document.get(Model.key.category.name()));
                model.setCreator(document.getString(Model.key.creator.name()));
                model.setAddress(document.getString(Model.key.address.name()));
                model.setCoordinate(document.getString(Model.key.coordinate.name()));
                model.setActiveAddress(document.getBoolean(Model.key.active_address.name()));
                model.setRates(document.getLong(Model.key.rates.name()));
                model.setImgURL(document.getString(Model.key.imgURL.name()));
                model.setCreated(document.getLong(Model.key.created.name()));
                model.setPublished(document.getBoolean(Model.key.published.name()));
                data.add(model);
            } catch (Exception e1) {
                Log.e("Test222", "error : " + e1.toString());
                e1.printStackTrace();
            }
        }
        return data;
    }

    public static synchronized ArrayList<Model> sortData(ArrayList<Model> data, final int sortIndex) {
        Collections.sort(data, new Comparator<Model>() {
            @Override
            public int compare(Model o1, Model o2) {
                switch (sortIndex) {
                    case 0: // abc
                    case 1: // zxy
                        return String.valueOf(o1.getTitle()).compareTo(String.valueOf(o2.getTitle()));
                    case 2: // latest
                    case 3: // oldest
                        return String.valueOf(o1.getCreated()).compareTo(String.valueOf(o2.getCreated()));
                    case 4: // cheapest
                    case 5: // expensive
                        String v1 = String.valueOf(o1.getRates());
                        String v2 = String.valueOf(o2.getRates());
                        if (v1.length() != v2.length())
                            return v1.length() < v2.length() ? -1 : 1;//assume the shorter string is a smaller value
                        else
                            return v1.compareTo(v2);
                    default: // expensive
                        return String.valueOf(o1.getRates()).compareTo(String.valueOf(o2.getRates()));
                }

            }
        });

        if (sortIndex == 1 || sortIndex == 2 || sortIndex == 4)
            Collections.reverse(data);
        return data;
    }

    public static int dpToPx() { // Converting dp to pixel
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2, app().getResources().getDisplayMetrics()));
    }

    public static String getCurrency(long price) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(localeID);
        String currency = format.format(price);
        return currency.replace("Rp", "Rp ");
    }

    public static boolean phoneNumberChecker(String number) {
        return !number.isEmpty() && number.length() >= 10
                && number.substring(0, 2).equals("08");
    }

    public static Bitmap resizeImage(String path, String newPath, int maxSize) {
        try {
            Bitmap bm = BitmapFactory.decodeFile(new File(path).getPath());
            bm = Utils.resizeBitmap(bm, maxSize);
            FileOutputStream bmpFile = new FileOutputStream(newPath);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bmpFile);
            bmpFile.flush();
            bmpFile.close();
            return bm;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Compress bitmap, set height(portrait) or width(landscape) to 600 or lower
     */
    public static Bitmap resizeBitmap(Bitmap bm, int maxSize) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float ratio = maxSize / (float)(width > height ? width : height); // To detect image mode
        if (ratio < 1.0f) {
            width = Math.round(width * ratio);
            height = Math.round(height * ratio);
        }
        return Bitmap.createScaledBitmap(bm, width, height, false);
    }

    public static boolean directoryExist(File dir) {
        return dir.exists() || dir.mkdirs();
    }

    public static String getUsername() {
        String[] username = Objects.requireNonNull(app().account.getEmail()).split("@");
        return username[0];
    }

    public static String millisToDate(long dateInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", new Locale("id"));
        return formatter.format(new Date(dateInMillis));
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPathFromUri(final Context context, final Uri uri) {

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                Log.e("UriPDF", "type media : " + type);
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            Uri singleUri = ContentUris.withAppendedId(UserDictionary.Words.CONTENT_URI, 316143);
            Log.e("UriPDF", "uri :" + singleUri.toString());


            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndex(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


}
