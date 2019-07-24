package com.example.googleplayservice_locationapi

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult


class FusedProviderClientActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName
    val UPDATE_INTERVAL = 5000
    val FASTEST_INTERVAL = 5000
    val REQUEST_LOCATION_PERMISSION = 101

    var mLocationRequest: LocationRequest? = null
    var mLastLocation: Location? = null
    var mLocationCallback: LocationCallback? = null

    var mTvCurrentLocation: TextView? = null
    var mBtnGetLocation: Button? = null

    var mFusedProvider: FusedLocationProviderClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fused_provider_client)
        initViews()

        // From Android 6.0, we need to check permission at runtime
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//            requestLocationPermissions()

        mFusedProvider = LocationServices.getFusedLocationProviderClient(this)


        mLocationRequest = LocationRequest.create()
        mLocationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest?.setInterval(UPDATE_INTERVAL.toLong())
//        mLocationRequest?.setFastestInterval(FASTEST_INTERVAL.toLong())


        mBtnGetLocation?.setOnClickListener { v ->
            Toast.makeText(this, "listener", Toast.LENGTH_SHORT).show()

            getLocationData()


        }
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    onToast("mLocationCallback 1")
                    return
                }
                onToast("mLocationCallback 2")

                for (location in locationResult.locations) {
                    if (location != null) {
                        mTvCurrentLocation?.setText(
                            String.format(
                                Locale.US,
                                "%s -- %s",
                                location.latitude,
                                location.longitude
                            )
                        )
                    }
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        mFusedProvider?.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
    }

    private fun onToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun getLocationData() {
        Toast.makeText(this, "Get from FuseProviderClient", Toast.LENGTH_SHORT).show()
        mFusedProvider?.lastLocation?.addOnSuccessListener { location: Location? ->
            run {
                if (location != null) {
                    mTvCurrentLocation?.setText(
                        String.format(
                            Locale.US,
                            "%s -- %s",
                            location.getLatitude(),
                            location.getLongitude()
                        )
                    );
                } else {
                    Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun initViews() {
        mTvCurrentLocation = findViewById(R.id.tv_current_location)
        mBtnGetLocation = findViewById(R.id.btn_get_location)
    }

    private fun requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )

        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//
//        when (requestCode) {
//            REQUEST_LOCATION_PERMISSION -> {
//                for (result in grantResults) {
//                    Log.d("Result", result.toString() + grantResults.size)
//                }
//                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
//                ) {
//                    Toast.makeText(this, "Request successfull", Toast.LENGTH_SHORT).show()
//                    mFusedProvider?.lastLocation?.addOnSuccessListener { location: Location? ->
//                        run {
//                            if (location != null) {
//                                mTvCurrentLocation?.setText(
//                                    String.format(
//                                        Locale.US,
//                                        "%s -- %s",
//                                        location.getLatitude(),
//                                        location.getLongitude()
//                                    )
//                                );
//                            } else {
//                                Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show()
//                                requestLocationPermissions()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    override fun onStop() {
        super.onStop()
        if (mFusedProvider != null) {
            mFusedProvider?.removeLocationUpdates(mLocationCallback)
        }
    }
}
