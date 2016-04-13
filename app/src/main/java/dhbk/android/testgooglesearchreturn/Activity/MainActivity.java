package dhbk.android.testgooglesearchreturn.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import org.osmdroid.views.MapView;

import java.util.ArrayList;

import dhbk.android.testgooglesearchreturn.ClassHelp.ImagePagerAdapter;
import dhbk.android.testgooglesearchreturn.ClassHelp.PhotoTask;
import dhbk.android.testgooglesearchreturn.R;


public class MainActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainApp";
    // Map
    private MapView mMapView;
    private GoogleApiClient mGoogleApiClient;

    // contain Google photo
    public static ArrayList<PhotoTask.AttributedPhoto> mArrayListAttributedPhoto;
    private ViewPager viewPager;
    private boolean showFAB = true;

    // place id obtains when search
    public static CharSequence mPlaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Phong - show the map + add 2 zoom button + zoom at a default view point
        makeMapDefaultSetting();
        mMapView = getMapView();
        mGoogleApiClient = getmGoogleApiClient();

        viewPager = (ViewPager) findViewById(R.id.imageSlider);

        // TODO: 3/30/16 Hiếu - khi mở, app sẽ xét xem mình có mở GPS chưa, nếu chưa thì app sẽ hiện 1 hộp thoại "Dialog" yêu cầu người dùng mở GPS, ông sẽ hiện thực hộp thoại này

        declareView();
    }

    private void declareView() {
        // fab
        final FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_my_location);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGoogleConnected()) {
                    if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    // when click, remove old maker, add new marker
                    mMapView.getOverlays().clear();

                    Location userCurrentLocation = getLocation();
                    setMarkerAtLocation(userCurrentLocation, R.drawable.ic_face_black_24dp);
                } else {
                    Log.i(TAG, "onClick: GoogleApi not connect");
                }
            }
        });

        // if click, go to another activity
        final FloatingActionButton floatingActionButtonDirection = (FloatingActionButton) findViewById(R.id.fab_direction);
        floatingActionButtonDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send to Direction activity with place id.
                Intent intent = new Intent(getApplication(), DirectionActivity.class);
                startActivity(intent);
            }
        });

        // To handle FAB animation upon entrance and exit
        final Animation growAnimation = AnimationUtils.loadAnimation(this, R.anim.simple_grow);
        final Animation shrinkAnimation = AnimationUtils.loadAnimation(this, R.anim.simple_shrink);
        shrinkAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                floatingActionButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // bottom sheet
        View bottomSheetDetailPlace = findViewById(R.id.map_bottom_sheets);
        final BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetDetailPlace);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d(TAG, "onStateChanged: goi bottom sheet");
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.i(TAG, "onStateChanged: drag");
                        if (showFAB)
                            floatingActionButton.startAnimation(shrinkAnimation);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.i(TAG, "onStateChanged: collapsed");
                        showFAB = true;
                        floatingActionButton.setVisibility(View.VISIBLE);
                        floatingActionButton.startAnimation(growAnimation);
                        floatingActionButtonDirection.startAnimation(growAnimation);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.i(TAG, "onStateChanged: expanded");
                        showFAB = false;
                        break;
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {

            }
        });
        // place details
        final TextView placeName = (TextView) findViewById(R.id.place_name);
        final TextView addressName = (TextView) findViewById(R.id.address_name);
        final TextView phoneName = (TextView) findViewById(R.id.phone_name);
        final TextView websiteName = (TextView) findViewById(R.id.website_name);

        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            // khi return search place, make bottom sheets appear and set place details to it.
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place Selected: " + place.getName());
                Log.i(TAG, "Place Selected: " + place.getAddress());
                Log.i(TAG, "Place Selected: " + place.getPhoneNumber());
                Log.i(TAG, "Place Selected: " + place.getWebsiteUri());
                mPlaceName = place.getName();
                // Format the returned place's details and display them in the TextView.
//                mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(), place.getId(),
//                        place.getAddress(), place.getPhoneNumber(), place.getWebsiteUri()));

//                CharSequence attributions = place.getAttributions();
//                if (!TextUtils.isEmpty(attributions)) {
//                    mPlaceAttribution.setText(Html.fromHtml(attributions.toString()));
//                } else {
//                    mPlaceAttribution.setText("");
//                }
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    // add place details
                    if (place.getName() != null) {
                        placeName.setText(place.getName());
                    }
                    if (place.getAddress() != null) {
                        addressName.setText(place.getAddress());
                    }
                    if (place.getPhoneNumber() != null) {
                        phoneName.setText(place.getPhoneNumber());
                    }
                    if (place.getWebsiteUri() != null) {
                        websiteName.setText(place.getWebsiteUri() + "");
                    }

                    // add place photos
                    addPhotoToBottomSheet(place.getId(), mGoogleApiClient);

                    bottomSheetBehavior.setPeekHeight(369);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

                // remove marker on the map, center at that point and add marker.
                mMapView.getOverlays().clear();
                Location placeLocation = new Location("Test");
                placeLocation.setLatitude(place.getLatLng().latitude);
                placeLocation.setLongitude(place.getLatLng().longitude);
                setMarkerAtLocation(placeLocation, R.drawable.ic_face_black_24dp);
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "onError: Status = " + status.toString());
                Toast.makeText(getApplication(), "Place selection failed: " + status.getStatusMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // when click delete button, collapse bottom sheets (peekHeight = 0)
        autocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText("");
                view.setVisibility(View.GONE);
                bottomSheetBehavior.setPeekHeight(0);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    // add a google photo to google image view.
    private void addPhotoToBottomSheet(String id, GoogleApiClient mGoogleApiClient) {
        Log.i(TAG, "addPhotoToBottomSheet: Hàm này đã được goi");
        new PhotoTask(viewPager.getWidth(), viewPager.getHeight()) {
            @Override
            protected void onPreExecute() {
                // Display a temporary image to show while bitmap is loading.
            }

            @Override
            protected void onPostExecute(ArrayList<AttributedPhoto> attributedPhotos) {

                // load image on viewpager, remove old images and add new ones.
                if (attributedPhotos.size() > 0) {
                    Log.i(TAG, "onPostExecute: có return image");
                    mArrayListAttributedPhoto = attributedPhotos;
                    ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), attributedPhotos.size());
                    viewPager.setAdapter(imagePagerAdapter);
                }
            }
        }.execute(new PhotoTask.MyTaskParams(id, mGoogleApiClient));
    }

}
