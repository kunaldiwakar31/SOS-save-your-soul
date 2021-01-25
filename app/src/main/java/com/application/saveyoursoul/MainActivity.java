package com.application.saveyoursoul;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText contactName;
    private EditText contactNumber;
    private Button contactButton;
    private ListView contactListView;
    private static List<String> contactList;
    private static List<String> savedContactList;
    private ArrayAdapter<String> contactAdapter;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int MY_PERMISSION_READ_PHONE_STATE = 1;
    private static final int MY_PERMISSION_ACCESS_LOCATION = 2;
    static FusedLocationProviderClient fusedLocationProviderClient;
    private static LocationRequest locationRequest;
    private static LocationCallback locationCallback;
    private Double longitude, latitude;
    private boolean isGPS = false;
    public static String Address="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(getApplicationContext(), LockService.class));

        contactName = findViewById(R.id.contactName);
        contactNumber = findViewById(R.id.contactNumber);
        contactButton = findViewById(R.id.contactAdd);
        contactListView = findViewById(R.id.contactList);

        Database database = new Database(MainActivity.this);

        savedContactList = database.getContactList();
        contactList = database.getContacts();
        contactAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, savedContactList);
        contactListView.setAdapter(contactAdapter);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        new GPS(this).turnGPSOn(new GPS.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });

        locationCallback = new LocationCallback() {
            @Override

            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        try {
                            Geocoder geocoder = new Geocoder(MyApplication.getAppContext(), Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            String adminArea = addresses.get(0).getAdminArea();
                            String countryName = addresses.get(0).getCountryName();
                            String locality = addresses.get(0).getLocality();
                            String subLocality = addresses.get(0).getSubLocality();
                            String postalCode = addresses.get(0).getPostalCode();


                            if(subLocality!=null && subLocality.length()>0) {
                                Address +=subLocality+", ";
                                Log.i("subLocality", subLocality);
                            }
                            if(locality!=null && locality.length()>0) {
                                Address +=locality+", ";
                                Log.i("locality", locality);
                            }
                            if(adminArea!=null && adminArea.length()>0) {
                                Address +=adminArea+", ";
                                Log.i("adminArea", adminArea);
                            }
                            if(countryName!=null && countryName.length()>0) {
                                Address +=countryName+", ";
                                Log.i("countryName", countryName);
                            }
                            if(postalCode!=null && postalCode.length()>0) {
                                Address +=postalCode;
                                Log.i("postalcode", postalCode);
                            }
                            Log.i("ADDRESS,Location Callback",Address);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (fusedLocationProviderClient != null) {
                            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                        }
                    }
                }
            }
        };

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contactName.getText() == null || contactNumber.getText() == null) {
                    Toast.makeText(MainActivity.this, "Fields are Empty!!", Toast.LENGTH_SHORT).show();
                } else {
                    String name = contactName.getText().toString();
                    String phone = contactNumber.getText().toString();

                    if (savedContactList.contains("Contact List is empty!!")) {
                        contactAdapter.clear();
                        savedContactList.clear();
                    }
                    savedContactList.add(name + " \t " + phone);
                    contactList.add(phone);
                    contactListView.setAdapter(contactAdapter);
                    database.addOne(name, phone);

                    Toast.makeText(MainActivity.this, "Contact Added Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void checkLocation() {
        if (ActivityCompat.checkSelfPermission(MyApplication.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Location", "permission required");
            checkLocationPermission();
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                Log.i("Location", String.valueOf(location));
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(MyApplication.getAppContext(), Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        String adminArea = addresses.get(0).getAdminArea();
                        String countryName = addresses.get(0).getCountryName();
                        String locality = addresses.get(0).getLocality();
                        String subLocality = addresses.get(0).getSubLocality();
                        String postalCode = addresses.get(0).getPostalCode();


                        if(subLocality!=null) {
                            Address +=subLocality+", ";
                            Log.i("subLocality", subLocality);
                        }
                        if(locality!=null) {
                            Address +=locality+", ";
                            Log.i("locality", locality);
                        }
                        if(adminArea!=null) {
                            Address +=adminArea+", ";
                            Log.i("adminArea", adminArea);
                        }
                        if(countryName!=null) {
                            Address +=countryName+", ";
                            Log.i("countryName", countryName);
                        }
                        if(postalCode!=null) {
                            Address +=postalCode;
                            Log.i("postalcode", postalCode);
                        }
                        Log.i("ADDRESS,checkLocation",Address);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (ActivityCompat.checkSelfPermission(MyApplication.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        checkLocationPermission();
                        return;
                    }
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                }
            }
        });
    }
    public static void sendMessage() {
        checkLocation();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String message = "Please Help I'm in Danger!!\n"+"ADDRESS: "+Address;
                Log.i("message",message);
                if (!contactList.isEmpty()) {
                    for (String phoneNumber : contactList) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                    }
                }
            }
        }, 5000);
    }
    @Override
    protected void onStart() {
        super.onStart();

        checkSmsPermission();
        checkPhonePermission();
        checkLocationPermission();

    }
    public void checkSmsPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
    }
    public void checkPhonePermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSION_READ_PHONE_STATE);
            }
        }
    }
    public static void checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(MyApplication.getAppContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) MyApplication.getAppContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions((Activity) MyApplication.getAppContext(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_ACCESS_LOCATION);
            }
        }
    }


}