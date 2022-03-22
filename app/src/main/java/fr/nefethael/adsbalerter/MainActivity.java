package fr.nefethael.adsbalerter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private final int REQUEST_ADSB_PERMISSIONS = 4;
    private final String ADSB_TASK_TAG = "ADSB";
    private final String[] ADSB_TILES = {"0016","6384","6505"};

    private final String ADSB_BASE_URL = "https://globe.adsbexchange.com/";
    private final String ADSB_GLOBE_FMT = ADSB_BASE_URL + "data/globe_%s.binCraft";
    private final String ADSB_RATES_URL = ADSB_BASE_URL + "globeRates.json";

    private Handler mainHandler = new Handler();
    private Runnable backgroundTask = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {

            FusedLocationProviderClient locationProviderClient = LocationServices.
                    getFusedLocationProviderClient(MainActivity.this);
            locationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        public void onSuccess(Location location) {
                            List<ADSBRequest> reqTiles = new ArrayList<>();
                            for(String e: ADSB_TILES) {
                                String url = String.format(ADSB_GLOBE_FMT, e);

                                Map<String,String> headers = new HashMap<String,String>();
                                headers.put("Referer",ADSB_BASE_URL);

                                reqTiles.add(new ADSBRequest(url, headers, location,
                                        new Response.Listener<CraftHolder>() {
                                            @Override
                                            public void onResponse(CraftHolder response) {
                                                // Display the first 500 characters of the response string.

                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("onErrorResponse", error.toString());
                                    }
                                }));
                            }

                            StringRequest initAdsb = new StringRequest(ADSB_RATES_URL,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            for (ADSBRequest req : reqTiles) {
                                                HTTPHelper.getInstance(MainActivity.this).addToRequestQueue(req);
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("onErrorResponse", error.toString());
                                }
                            });
                            HTTPHelper.getInstance(MainActivity.this).addToRequestQueue(initAdsb);
                        }
                    });

            mainHandler.postDelayed(this, 10000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestADSBPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_ADSB_PERMISSIONS)
    public void requestADSBPermissions() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET
        };
        if(EasyPermissions.hasPermissions(this, perms)) {
            initialiseADSB();
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the ADSB permissions", REQUEST_ADSB_PERMISSIONS, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        initialiseADSB();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    private void initialiseADSB(){
        Log.d("test","initialiseADSB");

        CookieManager manager = new CookieManager();
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        manager.getCookieStore().add(null, generateADSBCookie());
        CookieHandler.setDefault(manager);

        mainHandler.postDelayed(backgroundTask, 10000);
    }

    private String JSMathRandomToString36(double input) {
        final String array = "0123456789abcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < 11 ; i++){
            double tempVal = (input * 36);
            String tempValStr = String.valueOf(tempVal);
            int decIndex = tempValStr.indexOf(".");
            if (decIndex > 0) {
                Short topVal = Short.parseShort(tempValStr.substring(0, decIndex));
                result.append(array.charAt(topVal));
                input = tempVal - topVal;
            }
        }
        return result.toString();
    }

    private HttpCookie generateADSBCookie() {
        long now = System.currentTimeMillis();
        long ts = now + 2*86400*1000;
        long nextDate = now + (2*24*60*60*1000);
        String tmp = JSMathRandomToString36(new Random().nextDouble());
        String cookId = String.format("%d_%s",ts, tmp);

        HttpCookie cc = new HttpCookie("adsbx_sid", cookId);
        cc.setMaxAge(nextDate);
        cc.setDomain("globe.adsbexchange.com");
        cc.setPath("/");
        cc.setHttpOnly(false);
        cc.setSecure(false);
        return cc;
    }


}