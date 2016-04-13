package dhbk.android.testgooglesearchreturn.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import dhbk.android.testgooglesearchreturn.ListTripActivity;
import dhbk.android.testgooglesearchreturn.R;


public class ShareActivity extends BaseActivity {


    private static final int ACTIVITY_CAMERA_APP = 0;
    // private static final String GALLERY_LOCATION = "tripGallery" + new SimpleDateFormat("ydM_Hms").format(new Date()); // name of the folder gallery
    //TODO: Nhân - đặt tên folder gallery hợp lí
    private static final String GALLERY_LOCATION = "tripGallery2016313_232723";
    LocationManager locationManager;
    RoadManager roadManager = new OSRMRoadManager(this);
    ArrayList<GeoPoint> route;
    Marker mStart, mEnd;
    MapView mMap;
    LocationListener locationListenerGPS, locationListenerNetWork;
    private IMapController mapController;
    private ImageView img;

    private String imageFileLocation = "";
    private File galleryFoler;


    //Pic can save but not show auto in gallery
    public static void addPicToGallery(Context context, String photoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        /*Gallery*/
        createImageGallery(); //create gallery when onCreate
        /*Map*/
        mMap = (MapView) findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);
        mapController = mMap.getController();
        mapController.setZoom(18);
        GeoPoint startPoint = new GeoPoint(10.772241, 106.657676);
        mapController.setCenter(startPoint);
        route = new ArrayList<GeoPoint>();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mStart = new Marker(mMap);
        Drawable startIcon = getResources().getDrawable(R.drawable.start);
        mStart.setIcon(startIcon);
        mEnd = new Marker(mMap);
        Drawable end = getResources().getDrawable(R.drawable.end);
        mEnd.setIcon(end);


        locationListenerNetWork = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                GeoPoint temp = new GeoPoint(location.getLatitude(), location.getLongitude(), 16);
                // Toast.makeText(getBaseContext(), String.valueOf(location.getLatitude()), Toast.LENGTH_SHORT).show();
                mapController.setCenter(temp);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                GeoPoint temp = new GeoPoint(location.getLatitude(), location.getLongitude(), location.getAltitude());
                route.add(temp);
                Road road = new Road(route);
                Polyline line = roadManager.buildRoadOverlay(road, getBaseContext());
                mMap.getOverlays().add(line);
                mMap.invalidate();
                if (route.size() == 1) {
                    mStart.setPosition(temp);
                    mMap.getOverlays().add(mStart);
                    mMap.invalidate();
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        locationManager.removeUpdates(locationListenerNetWork);
                    }

                } else if (route.size() > 2) {
                    if (mEnd.getPosition() != null)
                        mMap.getOverlays().remove(mEnd);
                    mEnd.setPosition(temp);
                    mMap.getOverlays().add(mEnd);
                    mMap.invalidate();
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 10000, 0, locationListenerNetWork);
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
            mMap.getOverlays().clear();
        }

    }

    public void cameraActivtiy(View v) {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = null;
        try {
            photo = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        startActivityForResult(intent, ACTIVITY_CAMERA_APP);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ACTIVITY_CAMERA_APP && resultCode == RESULT_OK) {
            Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show();
            addPicToGallery(this, imageFileLocation);

            //setReductImageSize();

//            RecyclerView.Adapter newImageAdapter= new ListAdapter(sortFile(galleryFoler));
//            recyclerView.swapAdapter(newImageAdapter,false);
        }
    }

    private void createImageGallery() {
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera"); //path to folder gallery
        galleryFoler = new File(storageDirectory, GALLERY_LOCATION); // prepare create folder
        if (!galleryFoler.exists()) {
            galleryFoler.mkdirs(); //create folder
        }
    }

    public File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyydMM_Hms").format(new Date());
        String imgName = "IMG_" + timestamp;

        File img = File.createTempFile(imgName, ".jpg", galleryFoler);
        img.setReadable(true);
        imageFileLocation = img.getAbsolutePath();

        return img;
    }


    public void startGalleryActivity(View v) {
        Intent intent = new Intent(this, ListTripActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("galleryLocation", GALLERY_LOCATION);
        intent.putExtras(bundle);
        startActivity(intent);
    }


}