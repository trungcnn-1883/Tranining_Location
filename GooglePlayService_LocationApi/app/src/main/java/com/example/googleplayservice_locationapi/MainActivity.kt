package com.example.googleplayservice_locationapi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.common.GoogleApiAvailability
import java.util.*
import com.google.android.gms.location.LocationServices


class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {

    val TAG = MainActivity::class.java.simpleName
    val UPDATE_INTERVAL = 5000
    val FASTEST_INTERVAL = 5000
    val REQUEST_LOCATION_PERMISSION = 100

    var mGoogleApiClient: GoogleApiClient? = null
    var mLocationRequest: LocationRequest? = null
    var mLastLocation: Location? = null
    var mIsAutoUpdateLocation: Boolean = false

    var mTvCurrentLocation: TextView? = null
    var mBtnGetLocation: Button? = null
    var mMoveBtn: Button? = null
    var mSwAutoUpdateLocation: Switch? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate")
        initViews()

        // From Android 6.0, we need to check permission at runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestLocationPermissions()

        when (isPlayServiceAvailable()) {
            true -> {
                setUpLocationClientIfNeeded()
                buildLocationRequest()
            }
            false -> mTvCurrentLocation?.setText("Device does not support Google Play services");
        }

        mBtnGetLocation?.setOnClickListener { v ->
            Toast.makeText(this, "listener", Toast.LENGTH_SHORT).show()

            when (isGpsOn()) {
                true -> updateUi()
                false -> {
                    Toast.makeText(v.context, "GPS is OFF", Toast.LENGTH_SHORT).show()
                }
            }

        }

        mSwAutoUpdateLocation?.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {

            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                if (!isGpsOn()) {
                    Toast.makeText(
                        buttonView.context, "GPS is OFF",
                        Toast.LENGTH_SHORT
                    ).show();
                    mSwAutoUpdateLocation?.setChecked(false);
                    return;
                }
                mIsAutoUpdateLocation = isChecked;
                if (isChecked) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        })

        mMoveBtn?.setOnClickListener { v ->
            Intent(v.context, FusedProviderClientActivity::class.java).let {
                startActivity(it)
            }
        }

    }

    protected fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClient, mLocationRequest, this
        )
    }

    protected fun stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
    }

    private fun updateUi() {
        if (mLastLocation != null) {

            mTvCurrentLocation?.setText(
                String.format(
                    Locale.getDefault(), "%f, %f",
                    mLastLocation?.getLatitude(), mLastLocation?.getLongitude()
                )
            );
        } else {
            Toast.makeText(this, "updateUi + mLastLocation ==== null", Toast.LENGTH_SHORT).show()

        }
    }

    private fun isGpsOn(): Boolean {
        return (getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun buildLocationRequest() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest?.setInterval(UPDATE_INTERVAL.toLong())
        mLocationRequest?.setFastestInterval(FASTEST_INTERVAL.toLong())
    }

    private fun setUpLocationClientIfNeeded() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient =
                GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()
        }
    }

    private fun isPlayServiceAvailable() =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                for (result in grantResults) {
                    Log.d("Result", result.toString() + grantResults.size)
                }
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "Request successfull", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show()
                    requestLocationPermissions()
                }
            }
        }
    }

    private fun initViews() {
        mTvCurrentLocation = findViewById(R.id.tv_current_location)
        mBtnGetLocation = findViewById(R.id.btn_get_location)
        mSwAutoUpdateLocation = findViewById(R.id.sw_auto_update_location)
        mMoveBtn = findViewById(R.id.main_move_btn)
    }

    override fun onStart() {
        super.onStart()
        if (mGoogleApiClient != null) {
            mGoogleApiClient?.connect()
        }
    }

    override fun onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient?.disconnect()
        }
        super.onStop()

    }

    override fun onDestroy() {
        if (mGoogleApiClient != null
            && mGoogleApiClient!!.isConnected()
        ) {
            stopLocationUpdates();
            mGoogleApiClient?.disconnect()
            mGoogleApiClient = null
        }
        Log.d(TAG, "onDestroy LocationService");
        super.onDestroy();
    }

    override fun onConnected(p0: Bundle?) {
        Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "onConnected dafd", Toast.LENGTH_SHORT).show()
            return
        }

        val lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

        if (lastLocation != null) {
            mLastLocation = lastLocation
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient?.connect()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show()
    }

    override fun onLocationChanged(location: Location) {
        Toast.makeText(this, "onLocationChanged", Toast.LENGTH_SHORT).show()

        Log.d(
            TAG, String.format(
                Locale.getDefault(), "onLocationChanged : %f, %f",
                location.getLatitude(), location.getLongitude()
            )
        )
        mLastLocation = location;
        if (mIsAutoUpdateLocation) {
            updateUi();
        }
    }


}
