package com.example.mapdemo;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.mapdemo.directionhelpers.FetchURL;
import com.example.mapdemo.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    EditText txtSearch;
    ImageButton btnSearch;
    GoogleMap map;
    Button btn;
    MarkerOptions place1, place2;
    Polyline currentPolyline;
    FusedLocationProviderClient client;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtSearch = findViewById(R.id.txtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = getUrl(place1.getPosition(), place2.getPosition(), "driving");
                new FetchURL(MainActivity.this).execute(url, "driving");
            }
        });
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        place1 = new MarkerOptions().position(new LatLng(21.0324784,105.781801)).title(" near AC Building");
        place2 = new MarkerOptions().position(new LatLng(21.004675, 105.841479)).title("BK");
        client = LocationServices.getFusedLocationProviderClient(this);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = txtSearch.getText().toString();
                List<Address> addressList = new ArrayList<>();
                MarkerOptions searchMarkerOptions= new MarkerOptions();
                if(!TextUtils.isEmpty(address)){
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(address,2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(addressList.size()>0){
                            for(int i=0;i<addressList.size();i++){
                                Address searchAddress= addressList.get(i);
                                LatLng latLng = new LatLng(searchAddress.getLatitude(),searchAddress.getLongitude());
                                searchMarkerOptions.position(latLng);
                                searchMarkerOptions.title(address);
                                map.addMarker(searchMarkerOptions);
                                map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                map.animateCamera(CameraUpdateFactory.zoomTo(11));

                            }
//                        Address searchAddress= addressList.get(0);
//                        LatLng latLng = new LatLng(searchAddress.getLatitude(),searchAddress.getLongitude());
//                        searchMarkerOptions.position(latLng);
//                        searchMarkerOptions.title(address);
//                        map.addMarker(searchMarkerOptions);
//                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
//                        map.animateCamera(CameraUpdateFactory.zoomTo(15));

                    }else{
                        Toast.makeText(MainActivity.this, "khong tim duoc", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(MainActivity.this, "nhap lai", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
   //     map.addMarker(place1);
        map.addMarker(place2);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
        }else{
          //  Toast.makeText(MainActivity.this, "aa", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
    }


    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.key_map);
        return url;
    }
    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = map.addPolyline((PolylineOptions) values[0]);
    }


    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task <Location>task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if(location!= null){
                    LatLng latLng= new LatLng(location.getLatitude(),location.getLongitude());
                    MarkerOptions options = new MarkerOptions().position(latLng).title("I am  here");
                    map.addMarker(options);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                    map.animateCamera(CameraUpdateFactory.zoomTo(15));
                }else{
                    Toast.makeText(MainActivity.this, "aa", Toast.LENGTH_LONG).show();
                }
            }
        });
//        location.addOnCompleteListener(new OnCompleteListener() {
//            @Override
//            public void onComplete(@NonNull Task task) {
//                if(task.isSuccessful()){
//                    Location currentLocation = (Location) task.getResult();
////                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),11));
//                    LatLng latLng= new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
//                    MarkerOptions options = new MarkerOptions().position(latLng).title("I am  here");
//                    map.addMarker(options);
//                }else{
////                    Toast.makeText(MainActivity.this, "aa", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode ==44){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }

}