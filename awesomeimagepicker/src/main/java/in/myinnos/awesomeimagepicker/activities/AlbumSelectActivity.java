package in.myinnos.awesomeimagepicker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import in.myinnos.awesomeimagepicker.adapter.CustomAlbumSelectAdapter;
import in.myinnos.awesomeimagepicker.R;
import in.myinnos.awesomeimagepicker.helpers.ConstantsCustomGallery;
import in.myinnos.awesomeimagepicker.models.Album;

import static in.myinnos.awesomeimagepicker.R.anim.abc_fade_in;
import static in.myinnos.awesomeimagepicker.R.anim.abc_fade_out;

/**
 * Created by MyInnos on 03-11-2016.
 */
public class AlbumSelectActivity extends HelperActivity {

    private ArrayList<Album> albums;

    private TextView errorDisplay;
    private TextView tvProfile;
    private LinearLayout liFinish;

    private ProgressBar loader;
    private GridView gridView;
    private CustomAlbumSelectAdapter adapter;

    private ActionBar actionBar;

    private ContentObserver observer;
    private Handler handler;
    private Thread thread;

    private boolean multiSelectEnabled = false;

    private final String[] projection = new String[]{
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED,
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_select);
        setView(findViewById(R.id.layout_album_select));

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        ConstantsCustomGallery.limit = intent.getIntExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, ConstantsCustomGallery.DEFAULT_LIMIT);
        multiSelectEnabled = intent.getBooleanExtra(ConstantsCustomGallery.INTENT_EXTRA_MULTI_SELECTION, false);
        errorDisplay = (TextView) findViewById(R.id.text_view_error);
        errorDisplay.setVisibility(View.INVISIBLE);

        tvProfile = (TextView) findViewById(R.id.tvProfile);
        tvProfile.setText(R.string.album_view);
        liFinish = (LinearLayout) findViewById(R.id.liFinish);

        loader = (ProgressBar) findViewById(R.id.loader);
        gridView = (GridView) findViewById(R.id.grid_view_album_select);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (albums.get(position).getName().equals(getString(R.string.capture_photo))) {
                    //HelperClass.displayMessageOnScreen(getApplicationContext(), "HMM!", false);
                } else {

                    Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
                    intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_ALBUM_NAME, albums.get(position).getName());
                    intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_MULTI_SELECTION, multiSelectEnabled);
                    startActivityForResult(intent, ConstantsCustomGallery.REQUEST_CODE);
                }
            }
        });
        liFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(abc_fade_in, abc_fade_out);
            }
        });
    }

    @Override
    protected void onStart() {

        super.onStart();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {

                    case ConstantsCustomGallery.PERMISSION_GRANTED: {
                        loadAlbums();
                        break;
                    }
                    case ConstantsCustomGallery.FETCH_STARTED: {
                        loader.setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.INVISIBLE);
                        break;
                    }
                    case ConstantsCustomGallery.FETCH_COMPLETED: {
                        if (adapter == null) {
                            adapter = new CustomAlbumSelectAdapter(AlbumSelectActivity.this, getApplicationContext(), albums);
                            gridView.setAdapter(adapter);

                            loader.setVisibility(View.GONE);
                            gridView.setVisibility(View.VISIBLE);
                            orientationBasedUI(getResources().getConfiguration().orientation);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    }
                    case ConstantsCustomGallery.ERROR: {
                        loader.setVisibility(View.GONE);
                        errorDisplay.setVisibility(View.VISIBLE);
                        break;
                    }
                    default: {
                        super.handleMessage(msg);
                    }

                }
            }
        };
        observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                loadAlbums();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);
        checkPermission();
    }

    @Override
    protected void onStop() {

        super.onStop();
        stopThread();
        getContentResolver().unregisterContentObserver(observer);
        observer = null;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(null);
        }
        albums = null;
        if (adapter != null) {
            adapter.releaseResources();
        }
        gridView.setOnItemClickListener(null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }

    private void orientationBasedUI(int orientation) {

        final WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        if (adapter != null) {

            int size = orientation == Configuration.ORIENTATION_PORTRAIT ? metrics.widthPixels / 2 : metrics.widthPixels / 4;
            adapter.setLayoutParams(size);
        }
        gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        setResult(RESULT_CANCELED);
        overridePendingTransition(abc_fade_in, abc_fade_out);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ConstantsCustomGallery.REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private void loadAlbums() {
        startThread(new AlbumLoaderRunnable());
    }

    private final class AlbumLoaderRunnable implements Runnable {

        @Override
        public void run() {

            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (adapter == null) {
                sendMessage(ConstantsCustomGallery.FETCH_STARTED);
            }
            Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,null, null, MediaStore.Images.Media.DATE_MODIFIED);
            if (cursor == null) {
                sendMessage(ConstantsCustomGallery.ERROR);
                return;
            }
            ArrayList<Album> temp = new ArrayList<>(cursor.getCount());
            HashSet<Long> albumSet = new HashSet<>();
            File file;
            if (cursor.moveToLast()) {

                do {
                    if (Thread.interrupted()) {
                        cursor.close();
                        return;
                    }
                    long albumId = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String album = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String image = cursor.getString(cursor.getColumnIndex(projection[2]));
                    long albumTimestamp = cursor.getLong(cursor.getColumnIndex(projection[3]));
                    String displayDate = new SimpleDateFormat("dd MMMM", Locale.getDefault()).format(new Date(albumTimestamp));
                    if (!albumSet.contains(albumId)) {
                        /*
                        It may happen that some image file paths are still present in cache,
                        though image file does not exist. These last as long as media
                        scanner is not run again. To avoid get such image file paths, check
                        if image file exists.
                         */
                        file = new File(image);
                        if (file.exists()) {

                            // TODO: 2018/08/28 Complete implementation
                            temp.add(new Album(album, image, displayDate, R.drawable.ic_folder));
                            /*if (!album.equals("Hiding particular folder")) {
                                temp.add(new Album(album, image));
                            }*/
                            albumSet.add(albumId);
                        }
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            if (albums == null) {
                albums = new ArrayList<>();
            }
            albums.clear();
            // adding taking photo from camera option!
            /*albums.add(new Album(getString(R.string.capture_photo),
                    "https://image.freepik.com/free-vector/flat-white-camera_23-2147490625.jpg"));*/
            albums.addAll(temp);

            sendMessage(ConstantsCustomGallery.FETCH_COMPLETED);
        }
    }

    private void startThread(Runnable runnable) {

        stopThread();
        thread = new Thread(runnable);
        thread.start();
    }

    private void stopThread() {

        if (thread == null || !thread.isAlive()) {
            return;
        }
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(int what) {

        if (handler == null) {
            return;
        }
        Message message = handler.obtainMessage();
        message.what = what;
        message.sendToTarget();
    }

    @Override
    protected void permissionGranted() {

        Message message = handler.obtainMessage();
        message.what = ConstantsCustomGallery.PERMISSION_GRANTED;
        message.sendToTarget();
    }

    @Override
    protected void hideViews() {

        loader.setVisibility(View.GONE);
        gridView.setVisibility(View.INVISIBLE);
    }
}
