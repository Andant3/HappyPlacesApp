package com.example.happyplacesapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplacesapp.R
import com.example.happyplacesapp.databinding.ActivityMapBinding
import com.example.happyplacesapp.models.HappyPlaceModel
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityMapBinding

    private var mHappyPlaceDetail: HappyPlaceModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetail = intent.getParcelableExtra(
                MainActivity.EXTRA_PLACE_DETAILS)
        }
        if(mHappyPlaceDetail != null){
            setSupportActionBar(binding.toolbarMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mHappyPlaceDetail!!.title
            binding.toolbarMap.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        val supportMapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(
            mHappyPlaceDetail!!.latitude,
            mHappyPlaceDetail!!.longitude)

        googleMap.addMarker(MarkerOptions().position(position)
            .title(mHappyPlaceDetail!!.location)
        )

        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap.animateCamera(newLatLngZoom)
    }
}