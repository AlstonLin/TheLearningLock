package io.alstonlin.thelearninglock.setup;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.shared.Const;
import io.alstonlin.thelearninglock.shared.OnFragmentFinishedListener;


/**
 * A Fragment where the user can select the Lock screen background
 */
public class BackgroundPickerFragment extends Fragment {
    // Ratio of the what % of full screen res to save image as
    private static final int MAX_BG_SIZE = (int) (Math.max(Resources.getSystem().getDisplayMetrics().heightPixels,
            Resources.getSystem().getDisplayMetrics().widthPixels) * Const.SCREEN_BG_RESIZE_RATIO);
    private int PICK_IMAGE_REQUEST = 1;


    /**
     * Factory method to create a new instance of this Fragment
     *
     * @return A new instance of fragment BackgroundPickerFragment.
     */
    public static BackgroundPickerFragment newInstance() {
        BackgroundPickerFragment fragment = new BackgroundPickerFragment();
        return fragment;
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
    private static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
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
    private static String getDataColumn(Context context, Uri uri, String selection,
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
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
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
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Pick a Background");
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Pick a Background");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_background_picker, container, false);
        // TODO: Maybe also have default backgrounds to choose from?
        Button galleryPickButton = (Button) view.findViewById(R.id.fragment_background_picker_gallery_button);
        galleryPickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFromGallery();
            }
        });
        Button selectPictureButton = (Button) view.findViewById(R.id.fragment_background_picker_select_button);
        selectPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });
        return view;
    }

    /**
     * Picks from the user's gallery
     */
    private void pickFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Picks from the default pictures
     */
    private void selectPicture() {
        // Sets up the gridview
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup pickerView = (ViewGroup) inflater.inflate(R.layout.layout_select_bg, null, false);
        GridView imagesGrid = (GridView) pickerView.findViewById(R.id.bg_grid);
        // Ugh Alston why are you obsessed with PopupWindow, they are impossible to style without being hacky.
        final PopupWindow popup = new PopupWindow(pickerView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true);
        final BGImagePickerAdapter bgImageAdapter = new BGImagePickerAdapter(getContext());
        imagesGrid.setAdapter(bgImageAdapter);
        imagesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Usually, full resolution = alot of memory
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), bgImageAdapter.getItem(position));
                setBackgroundBitmap(bitmap);
                onBackgroundSelected();
                popup.dismiss();
            }
        });
        popup.showAtLocation(getView(), Gravity.CENTER, 0, 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            Bitmap bitmap = BitmapFactory.decodeFile(getPath(getContext(), uri));
            setBackgroundBitmap(bitmap);
            onBackgroundSelected();
        }
    }

    public void onBackgroundSelected() {
        ((OnFragmentFinishedListener) getActivity()).onFragmentFinished();
    }

    private void setBackgroundBitmap(Bitmap bg) {
        // Resizes the bitmap
        Bitmap resized = Bitmap.createScaledBitmap(bg, MAX_BG_SIZE, MAX_BG_SIZE, false);
        // Saves the new resized bg to a file
        File dir = new File(Const.BACKGROUND_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, Const.BACKGROUND_FILE);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            resized.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            Snackbar.make(getView(), "Couldnt save background!", Snackbar.LENGTH_SHORT).show();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
