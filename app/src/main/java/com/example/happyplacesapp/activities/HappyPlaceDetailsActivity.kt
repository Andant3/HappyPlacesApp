package com.example.happyplacesapp.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.happyplacesapp.databinding.ActivityHappyPlaceDetailsBinding
import com.example.happyplacesapp.models.HappyPlaceModel

class HappyPlaceDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHappyPlaceDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbarHappyPlaceDetails)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.toolbarHappyPlaceDetails.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        var happyPlaceDetailModel: HappyPlaceModel? = null
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailModel = intent.getParcelableExtra(
                MainActivity.EXTRA_PLACE_DETAILS)
        }
        if (happyPlaceDetailModel != null){
            binding.toolbarHappyPlaceDetails.title = happyPlaceDetailModel.title

            binding.ivPlaceImage.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            binding.tvDescription.text = happyPlaceDetailModel.description
            binding.tvLocation.text = happyPlaceDetailModel.location
        }else{
            Toast.makeText(
                this,
                "Something went wrong!",
                Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.btnMap.setOnClickListener {
            val intent = Intent(this@HappyPlaceDetailsActivity,
                MapActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, happyPlaceDetailModel)

            startActivity(intent)
        }
    }
}