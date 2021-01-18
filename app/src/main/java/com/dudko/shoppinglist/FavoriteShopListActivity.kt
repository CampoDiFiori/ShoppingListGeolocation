package com.dudko.shoppinglist

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteShopListActivity : BaseActivity() {

    var provider: String? = null
    var currentLocation: Location? = null;
    lateinit var geoClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.favorite_shop_list)

        geoClient = LocationServices.getGeofencingClient(this)

        val database = FirebaseDatabase.getInstance()
        val userId = mAuth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "You need to sign in again", Toast.LENGTH_SHORT).show()
            return
        }

        val userShopListRef = database.getReference("Favorite Shops by $userId")

        val rv = findViewById<RecyclerView>(R.id.favoriteShopList)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = FavoriteShopListAdapter(this, userShopListRef)

        findViewById<FloatingActionButton>(R.id.favoriteShopFab).setOnClickListener{
            if (currentLocation != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val newShop = Shop(
                            name = "New Shop",
                            description = "Example description}",
                            radius = 100f,
                            inside = false,
                            latitude = currentLocation!!.latitude,
                            longitude = currentLocation!!.longitude
                    )
                    val newShopRef = userShopListRef.push()
                    newShopRef.setValue(newShop)
                    addShopGeofence(newShop, newShopRef.key!!)
                }
            } else {
                Toast.makeText(this, "Just a second, your location history is still empty", Toast.LENGTH_SHORT).show()
            }
        }

        val perms = arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val requestCode = 1
        val fineLocationPermission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val backgroundLocationPermission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission != PackageManager.PERMISSION_GRANTED &&
                backgroundLocationPermission != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(perms, requestCode)
            println("Requested permissions")
            return
        }

        // TODO move this to MainActivity, maybe? + call : locationManager.removeUpdates(ll)
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.isAltitudeRequired = true
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.NO_REQUIREMENT
        provider = locationManager.getBestProvider(criteria, false)
        val locationListener = object: LocationListener {
            override fun onLocationChanged(location: Location) {
                Toast.makeText(this@FavoriteShopListActivity, "New location",
                        Toast.LENGTH_SHORT).show()
                Log.i("location", "Change of location detected")
                currentLocation = location
            }
            override fun onProviderEnabled(provider: String) {
                super.onProviderEnabled(provider)
                Toast.makeText(this@FavoriteShopListActivity, "Provider enabled",
                        Toast.LENGTH_SHORT).show()
            }
            override fun onProviderDisabled(provider: String) {
                super.onProviderDisabled(provider)
                Toast.makeText(this@FavoriteShopListActivity, "Provider disabled",
                        Toast.LENGTH_SHORT).show()
            }
        }
        locationManager.requestLocationUpdates(provider.toString(), 10000L, 1F, locationListener)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 1F, locationListener)
    }

    private fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }
    private fun getGeofencePendingIntent(shop: Shop): PendingIntent {
        return PendingIntent.getBroadcast(
            this, 0,
            Intent(this, GeofenceReceiver::class.java)
                .putExtra("name", shop.name)
                .putExtra("description", shop.description),
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun addShopGeofence(shop: Shop, shopId: String) {
        val latLng = LatLng(shop.latitude, shop.longitude)
        val radius = shop.radius
        val geo = Geofence.Builder().setRequestId(shopId)
            .setCircularRegion(
                latLng.latitude,
                latLng.longitude,
                radius
            )
            .setExpirationDuration(1000*60*5)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER
                    or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("Doesn't have geolocation permissions")
            return
        }

        geoClient.addGeofences(
            getGeofencingRequest(geo),
            getGeofencePendingIntent(shop)
        ).addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Geofence added.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to add geofence.",
                    Toast.LENGTH_SHORT).show()
            }
    }
}
