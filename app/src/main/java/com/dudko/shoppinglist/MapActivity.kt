package com.dudko.shoppinglist

import android.os.Bundle
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class MapShop(val name: String, val desc: String, val latLng: LatLng)

class MapActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val database = FirebaseDatabase.getInstance()
        val userId = mAuth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "You need to sign in again", Toast.LENGTH_SHORT).show()
            return
        }

        val userShopListRef = database.getReference("Favorite Shops by $userId")

        userShopListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (productSnapshot in dataSnapshot.children) {
                    val shopEntry = productSnapshot.value as Map<String, Object>
                    val shop = MapShop(
                            name = shopEntry["name"] as String,
                            desc = shopEntry["description"] as String,
                            latLng = LatLng(shopEntry["latitude"] as Double, shopEntry["longitude"] as Double)
                    )
                    mMap.addMarker(
                            MarkerOptions()
                                    .position(shop.latLng)
                                    .title(shop.name)
                                    .snippet(shop.desc)
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(shop.latLng))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }
        })
    }
}