package mobi.zapzap.mediapicker;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by Zapper Development on 03-11-2016.
 */
public final class MediaPickerConstants {

    private MediaPickerConstants() {
    }

    public static final int PERMISSION_REQUEST_CODE = 1000;
    public static final int PERMISSION_GRANTED = 1001;
    public static final int PERMISSION_DENIED = 1002;

    public static final int REQUEST_CODE = 2000;
    public static final int ADD_REQUEST_CODE = 3000;
    public static final int IMAGE_SELECTION_MODE_SINGLE = 80807;
    public static final int IMAGE_SELECTION_MODE_MULTIPLE = 80808;

    public static final int FETCH_STARTED = 2001;
    public static final int FETCH_COMPLETED = 2002;
    public static final int ERROR = 2005;

    public static final int ALBUM_GRID_SPAN_COUNT = 2;
    public static final int IMAGE_GRID_SPAN_COUNT = 3;

    public static final int VIEW_TYPE_LOADING = 0;
    public static final int VIEW_TYPE_HEADER = 1;
    public static final int VIEW_TYPE_ITEM = 2;

    /**
     * Request code for permission has to be < (1 << 8)
     * Otherwise throws java.lang.IllegalArgumentException: Can only use lower 8 bits for requestCode
     */
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 23;

    public static final String INTENT_EXTRA_ALBUM = "album";
    public static final String INTENT_EXTRA_ALBUM_NAME = "album_name";
    public static final String INTENT_EXTRA_IMAGE = "image";
    public static final String INTENT_EXTRA_LIST_IMAGES = "list_images";
    public static final String INTENT_EXTRA_LIMIT = "selection_limit";
    public static final String INTENT_EXTRA_SELECTION_MODE = "selection_mode";
    //Maximum number of images that can be selected at a time
    public static final int DEFAULT_SELECTION_LIMIT = 10;


    public static final int SCROLLBAR_ANIM_DURATION = 300;

    public static final String[] IMAGE_DEFAULT_PROJECTION = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
    };

    public static final Uri IMAGE_CONTENT_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static final String DEFAULT_IMAGE_ORDER_BY = MediaStore.Images.Media.DATE_ADDED + " DESC";

}