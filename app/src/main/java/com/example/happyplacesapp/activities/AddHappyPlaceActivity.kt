package com.example.happyplacesapp.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.happyplacesapp.databinding.ActivityAddHappyPlaceBinding
import com.karumi.dexter.Dexter
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.example.happyplacesapp.R
import com.example.happyplacesapp.database.DataBaseHandler
import com.example.happyplacesapp.models.HappyPlaceModel
import com.example.happyplacesapp.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener{

    private lateinit var binding: ActivityAddHappyPlaceBinding

    private val calendar = Calendar.getInstance()
    private lateinit var dateSetListener: OnDateSetListener

    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var mHappyPlaceDetails: HappyPlaceModel? = null

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarAddPlace.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this@AddHappyPlaceActivity)

        if(!Places.isInitialized()){
            Places.initialize(this@AddHappyPlaceActivity,
                resources.getString(R.string.API_KEY))
        }

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getParcelableExtra(
                MainActivity.EXTRA_PLACE_DETAILS)
        }

        dateSetListener = OnDateSetListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateDateInView()
        }
        updateDateInView()

        if(mHappyPlaceDetails != null){
            supportActionBar?.title = "Edit Happy Place"

            binding.etTitle.setText(mHappyPlaceDetails!!.title)
            binding.etDescription.setText(mHappyPlaceDetails!!.description)
            binding.etDate.setText(mHappyPlaceDetails!!.date)
            binding.etLocation.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)
            binding.ivPlaceImage.setImageURI(saveImageToInternalStorage)

            binding.btnSave.text = "UPDATE"
        }
        binding.etDate.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.etLocation.setOnClickListener(this)
        binding.tvSelectCurrentLocation.setOnClickListener(this)
    }
    companion object{
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.etDate ->{
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.tvAddImage ->{
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery", "Capture photo from Camera")
                pictureDialog.setItems(pictureDialogItems){
                    dialog, which ->
                    when(which){
                        0 ->choosePhotoFromGallery()
                        1 ->takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btnSave ->{
                when{
                    binding.etTitle.text.isNullOrEmpty() ->{
                        Toast.makeText(
                            this@AddHappyPlaceActivity,
                            "Please, enter complete information!",
                            Toast.LENGTH_SHORT).show()
                    }
                    binding.etDescription.text.isNullOrEmpty() ->{
                        Toast.makeText(
                            this@AddHappyPlaceActivity,
                            "Please, enter complete information!",
                            Toast.LENGTH_SHORT).show()
                    }
                    binding.etLocation.text.isNullOrEmpty() ->{
                        Toast.makeText(
                            this@AddHappyPlaceActivity,
                            "Please, enter complete information!",
                            Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null ->{
                        Toast.makeText(
                            this@AddHappyPlaceActivity,
                            "Please, enter complete information!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else ->{
                        val happyPlaceModel = HappyPlaceModel(
                            if(mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                            binding.etTitle.text.toString(),
                            saveImageToInternalStorage.toString(),
                            binding.etDescription.text.toString(),
                            binding.etDate.text.toString(),
                            binding.etLocation.text.toString(),
                            mLatitude,
                            mLongitude)

                        val dbHandler = DataBaseHandler(this)

                        if (mHappyPlaceDetails == null){
                            val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                            if (addHappyPlace > 0){
                                setResult(Activity.RESULT_OK);
                                finish()
                            }
                        }else{
                            val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)

                            if (updateHappyPlace > 0){
                                setResult(Activity.RESULT_OK);
                                finish()
                            }
                        }
                    }
                }
            }
            R.id.etLocation ->{
                try {
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME,
                        Place.Field.LAT_LNG, Place.Field.ADDRESS
                    )
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this@AddHappyPlaceActivity)

                    placeResultLauncher.launch(intent)
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
            R.id.tvSelectCurrentLocation->{
                if(!isLocationEnabled()){
                    Toast.makeText(
                        this,
                        "Your Location Provider turned OFF. Please, turn in ON",
                        Toast.LENGTH_LONG).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }else{
                    getCurrentLocation()
                }
            }
        }
    }
    var placeResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val place: Place = Autocomplete.getPlaceFromIntent(data!!)
            binding.etLocation.setText(place.address)
            mLatitude = place.latLng.latitude
            mLongitude = place.latLng.longitude
        }
    }
    private fun getCurrentLocation(){
        Dexter.withContext(this).withPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    requestNewLocationData()
                }else{
                    Toast.makeText(
                        this@AddHappyPlaceActivity,
                        "Something went wrong." +
                                "Check the Permission Settings",
                        Toast.LENGTH_LONG).show()
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationaleDialogForPermissions()
            }
        }).onSameThread().check()
    }
    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        val timeInterval:Long = 0
        val minimalDistance = 50f
        val mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, timeInterval).apply {
            setMinUpdateDistanceMeters(minimalDistance)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback, Looper.myLooper())
    }
    private val mLocationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation as Location
            mLatitude = mLastLocation.latitude
            mLongitude = mLastLocation.longitude
            Log.i("Current latitude", "$mLatitude")
            Log.i("Current longitude", "$mLongitude")

            val addressTask = GetAddressFromLatLng(
                this@AddHappyPlaceActivity, mLatitude, mLongitude)
            addressTask.setAddressListener(object : GetAddressFromLatLng.AddressListener{
                override fun onAddressFound(address: String?) {
                    binding.etLocation.setText(address)
                }
                override fun onError() {
                    Log.e("get Address:", "Something went wrong")
                }
            })
            addressTask.getAddress()
        }
    }
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)

        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            var opStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, opStream)
            opStream.flush()
            opStream.close()
        }catch (e: IOException){
            e.printStackTrace()
            Toast.makeText(
                this,
                "Something went wrong! File isn't saved",
                Toast.LENGTH_LONG).show()
        }
        return Uri.parse(file.absolutePath)
    }
    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(calendar.time).toString())
    }
    private fun choosePhotoFromGallery(){
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)
            {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryResultLauncher.launch(galleryIntent)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken)
            {
                showRationaleDialogForPermissions()
            }
        }).onSameThread().check()
    }
    var galleryResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if(data != null){
                val contentURI = data.data
                try {
                    contentURI?.let {
                        if(Build.VERSION.SDK_INT < 28){
                            val bitmap = MediaStore.Images.Media.getBitmap(
                                this.contentResolver, contentURI)

                            saveImageToInternalStorage = saveImageToInternalStorage(bitmap)
                            Log.i("Saved image", "Path:: $saveImageToInternalStorage")

                            binding.ivPlaceImage.setImageBitmap(bitmap)
                        }else{
                            val source = ImageDecoder.createSource(this.contentResolver, contentURI)
                            val bitmap = ImageDecoder.decodeBitmap(source)

                            saveImageToInternalStorage = saveImageToInternalStorage(bitmap)
                            Log.i("Saved image", "Path:: $saveImageToInternalStorage")

                            binding.ivPlaceImage.setImageBitmap(bitmap)
                        }
                    }
                }catch (e: IOException){
                    e.printStackTrace()
                    Toast.makeText(
                        this,
                        "Failed to load the picture from the gallery!",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun takePhotoFromCamera(){
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)
            {
                if(report!!.areAllPermissionsGranted()){
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraResultLauncher.launch(cameraIntent)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken)
            {
                showRationaleDialogForPermissions()
            }
        }).onSameThread().check()
    }
    var cameraResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val thumbNail: Bitmap = data!!.extras!!.get("data") as Bitmap

            saveImageToInternalStorage = saveImageToInternalStorage(thumbNail)
            Log.i("Saved image", "Path:: $saveImageToInternalStorage")

            binding.ivPlaceImage.setImageBitmap(thumbNail)
        }
    }
    private fun showRationaleDialogForPermissions(){
        AlertDialog.Builder(this).setMessage(
            "It looks like you turned off permissions" +
                    "required for this feature. " +
                    "It can be enabled in Application Settings"
        ).setPositiveButton("GO TO SETTINGS")
        {_, _ ->
            try{
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }catch(e: ActivityNotFoundException){
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel"){dialog, _ ->
            dialog.dismiss()
        }.show()
    }
}