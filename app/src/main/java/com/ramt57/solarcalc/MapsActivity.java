package com.ramt57.solarcalc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.euicc.DownloadableSubscription;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.ramt57.solarcalc.database.DatabaseHelper;
import com.ramt57.solarcalc.model.PhaseTimeModel;
import com.ramt57.solarcalc.model.PlacesModel;
import com.ramt57.solarcalc.notification.BroadcastReciever;
import com.ramt57.solarcalc.phasetimecalculator.PhaseTimeUtils;
import com.ramt57.solarcalc.phasetimecalculator.Zenith;
import com.ramt57.solarcalc.utils.ApplicationUtility;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1002;
    private int PLACE_PICKER_REQUEST = 1;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    TextView search_box;
    PlacesModel placesModel;
    TextView date_txt;
    int calender_date = 0;
    TextView txt_sunrise, txt_sunset, txt_moonrise, txt_moonset;
    DatabaseHelper dbhelper;
    private FusedLocationProviderClient mFusedLocationClient;
    BroadcastReciever notifyReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        notifyReceiver = new BroadcastReciever();
        registerBroadCastReciever();
        placesModel = new PlacesModel();

        dbhelper = new DatabaseHelper(this);
        initViews();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        isLocationServicesAvailable(this);

        search_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapsActivity.this, "loading..", Toast.LENGTH_SHORT).show();
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MapsActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    Log.w("error", "error");
                    e.printStackTrace();
                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initViews() {
        search_box = findViewById(R.id.edt_search);
        date_txt = findViewById(R.id.txt_date);
        date_txt.setText(ApplicationUtility.getCurrentDate(Calendar.getInstance()));
        findViewById(R.id.img_play).setOnClickListener(this);
        findViewById(R.id.img_next).setOnClickListener(this);
        findViewById(R.id.img_prev).setOnClickListener(this);
        txt_sunrise = findViewById(R.id.txt_sunrise);
        txt_sunset = findViewById(R.id.txt_sunset);
        txt_moonrise = findViewById(R.id.txt_moonrise);
        txt_moonset = findViewById(R.id.txt_moonset);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        configureCameraIdle();
        checkpermission();
//        mMap.setMyLocationEnabled(true);
        // Construct a FusedLocationProviderClient.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                                        location.getLongitude()), 5));

                            }
                        }
                    });

        }

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.pin:
                if (placesModel.getName() == null && placesModel.getAddress() == null) {
                    Toast.makeText(this, "failed to save", Toast.LENGTH_SHORT).show();
                    return true;
                }
                dbhelper.insertPlaces(placesModel);
                Toast.makeText(this, "location saved", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.bookmark:
                getAlertDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "connection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String placename = String.format("%s", place.getName());
                Double latitude = place.getLatLng().latitude;
                Double longitude = place.getLatLng().longitude;
                String address = String.format("%s", place.getAddress());
                placesModel.setName(placename);
                placesModel.setLat(latitude);
                placesModel.setLng(longitude);
                placesModel.setAddress(address);
                UpdatePhaseTime();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 5));
                search_box.setText(address);
            }
        }
    }

    /*map draging location method */
    private void configureCameraIdle() {
        GoogleMap.OnCameraIdleListener onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLng latLng = mMap.getCameraPosition().target;
                Geocoder geocoder = new Geocoder(MapsActivity.this);
                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        String locality = addressList.get(0).getAddressLine(0);
                        placesModel.setName(addressList.get(0).getFeatureName());
                        placesModel.setLat(latLng.latitude);
                        placesModel.setLng(latLng.longitude);
                        placesModel.setAddress(locality);
                        search_box.setText(locality);
                        UpdatePhaseTime();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mMap.setOnCameraIdleListener(onCameraIdleListener);
    }

    private void UpdatePhaseTime() {
        PhaseTimeModel model = ApplicationUtility.getPhaseTime(date_txt.getText().toString(), placesModel);
        if (model != null) {
            txt_sunrise.setText(model.getSunrise());
            flashTextView(txt_sunrise);
            txt_sunset.setText(model.getSunset());
            flashTextView(txt_sunset);
            txt_moonrise.setText(model.getMoonrise());
            flashTextView(txt_moonrise);
            txt_moonset.setText(model.getMoonset());
            flashTextView(txt_moonset);

            schedule(); /*schedule notification*/
        }
    }

    @Override
    public void onClick(View view) {
        Calendar calendar = Calendar.getInstance();
        switch (view.getId()) {
            case R.id.img_play:
                calender_date = 0;
                UpdatePhaseTime();
                break;
            case R.id.img_prev:
                calender_date--;
                UpdatePhaseTime();
                break;
            case R.id.img_next:
                calender_date++;
                UpdatePhaseTime();
                break;
        }
        calendar.add(Calendar.DATE, calender_date);
        date_txt.setText(ApplicationUtility.getCurrentDate(calendar));

    }

    public void getAlertDialog() {
        AlertDialog.Builder alertlist = new AlertDialog.Builder(MapsActivity.this);
        alertlist.setIcon(R.drawable.ic_launcher_foreground);
        alertlist.setTitle("Choose Location");

        final ArrayAdapter<PlacesModel> arrayAdapter = new ArrayAdapter<PlacesModel>(MapsActivity.this,
                android.R.layout.select_dialog_singlechoice) {

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).
                            inflate(android.R.layout.select_dialog_singlechoice, parent, false);
                }
                CheckedTextView textView = convertView.findViewById(android.R.id.text1);
                textView.setText(getItem(position).getAddress() + "");
                return convertView;
            }
        };

        arrayAdapter.addAll(dbhelper.getAllPlaces());

        alertlist.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertlist.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                String strName = arrayAdapter.getItem(which).getName();
                AlertDialog.Builder builderInner = new AlertDialog.Builder(MapsActivity.this);
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which1) {
                        placesModel = arrayAdapter.getItem(which);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(placesModel.getLat(), placesModel.getLng()), 5));
                        UpdatePhaseTime();
                        dialog.dismiss();
                    }
                });
                builderInner.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dbhelper.deletePlace(arrayAdapter.getItem(which));
                        Toast.makeText(MapsActivity.this, "location deleted", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        alertlist.show();
    }

    public void checkpermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    public void isLocationServicesAvailable(final Context context) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
//        task.addOnFailureListener()
        task.addOnFailureListener((Activity) context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult((Activity) context,
                                120);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    /*this is just a flashing animation to blink views*/
    public void flashTextView(TextView textView) {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(50); //You can manage the time of the blink with this parameter
        anim.setStartOffset(20);
        anim.setRepeatCount(0);
        textView.startAnimation(anim);
    }

    /*for Oreo you must register in activity or service */
    public void registerBroadCastReciever() {
        registerReceiver(
                notifyReceiver, new IntentFilter()
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notifyReceiver);
    }

    public void schedule() {
        Log.w("TAA", ApplicationUtility.getTimeInMiliSecond(txt_sunrise.getText().toString()) + " d " + txt_sunrise.getText().toString());
        /*on getting location schudele notification*/
        if (!txt_sunrise.getText().toString().isEmpty()) {
            ApplicationUtility.
                    setNotificationForPhaseTime(getApplicationContext(),
                            ApplicationUtility.getTimeInMiliSecond(txt_sunrise.getText().toString()));
        }
        if (!txt_sunset.getText().toString().isEmpty()) {
            ApplicationUtility.
                    setNotificationForPhaseTime(getApplicationContext(),
                            ApplicationUtility.getTimeInMiliSecond(txt_sunset.getText().toString()));
        }
    }
}
