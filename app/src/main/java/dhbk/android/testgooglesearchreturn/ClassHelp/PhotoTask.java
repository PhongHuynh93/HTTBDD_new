package dhbk.android.testgooglesearchreturn.ClassHelp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;

/**
 * Created by huynhducthanhphong on 4/1/16.
 */
public abstract class PhotoTask extends AsyncTask<PhotoTask.MyTaskParams, Void, ArrayList<PhotoTask.AttributedPhoto>> {
    private static final String TAG = PhotoTask.class.getName();
    private int mHeight;
    private int mWidth;

    public PhotoTask(int width, int height) {
        mHeight = height;
        mWidth = width;
    }

    // to handle params
    public static class MyTaskParams {
        String mPlaceId;
        GoogleApiClient mGoogleApiClient;

        public MyTaskParams(String mPlaceId, GoogleApiClient mGoogleApiClient) {
            this.mPlaceId = mPlaceId;
            this.mGoogleApiClient = mGoogleApiClient;
        }
    }

    /**
     * Holder for an image and its attribution.
     */
    public class AttributedPhoto {
        public final CharSequence attribution;
        public final Bitmap bitmap;
        public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
            this.attribution = attribution;
            this.bitmap = bitmap;
        }
    }

    /**
     * Loads the first photo for a place id from the Geo Data API.
     * The place id must be the first (and only) parameter.
     */
    @Override
    protected ArrayList<AttributedPhoto> doInBackground(MyTaskParams... params) {
        if (params.length != 1) {
            return null;
        }
        final String placeId = params[0].mPlaceId;
        final GoogleApiClient mGoogleApiClient = params[0].mGoogleApiClient;
        AttributedPhoto attributedPhoto = null;

        Log.i(TAG, "doInBackground: GoogleApiClient:" + mGoogleApiClient);

        // Get a PlacePhotoMetadataResult containing metadata for the first 10 photos.
        PlacePhotoMetadataResult result = Places.GeoDataApi
                .getPlacePhotos(mGoogleApiClient, placeId).await();

        // arraylist to contains all of image which is downloaded.
        ArrayList<AttributedPhoto> attributedPhotoArrayList = new ArrayList<>();

        if (result.getStatus().isSuccess()) {
            // Get a PhotoMetadataBuffer instance containing a list of photos (PhotoMetadata).
            PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
            Log.i(TAG, "doInBackground: Ket noi thanh cong google photo");

            if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                int countPhoto = photoMetadataBuffer.getCount();
                while(countPhoto > 0) {
                    Log.i(TAG, "doInBackground: đã có image " + countPhoto);

                    // Get the first bitmap and its attributions.
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(countPhoto - 1);
                    CharSequence attribution = photo.getAttributions();

                    // Load a scaled bitmap for this photo.(scale đúng theo tỷ lệ) + có kèm theo cache + await là lock cho đến khi xong
                    Bitmap image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                            .getBitmap();

                    attributedPhoto = new AttributedPhoto(attribution, image);
                    attributedPhotoArrayList.add(attributedPhoto);
                    countPhoto--;
                }


            }
            // Release the PlacePhotoMetadataBuffer.
            photoMetadataBuffer.release();
        }
        // return array of photos.
        return attributedPhotoArrayList;
    }
}
