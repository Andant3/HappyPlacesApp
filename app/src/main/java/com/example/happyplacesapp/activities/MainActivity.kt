package com.example.happyplacesapp.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.happyplacesapp.R
import com.example.happyplacesapp.adapters.HappyPlacesAdapter
import com.example.happyplacesapp.database.DataBaseHandler
import com.example.happyplacesapp.databinding.ActivityMainBinding
import com.example.happyplacesapp.models.HappyPlaceModel
import com.example.happyplacesapp.utils.SwipeToDeleteCallback
import com.example.happyplacesapp.utils.SwipeToEditCallback

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.fabAddHappyPlace.setOnClickListener{
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            happyPlaceResultLauncher.launch(intent)
        }
        getHappyPlacesListFromLocalDB()
    }
    companion object{
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }
    private var happyPlaceResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            getHappyPlacesListFromLocalDB()
        }else{
            Log.i("Activity", "Canceled or Back pressed")
        }
    }
    private fun setUpHappyPlacesRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>){
        binding.rvHappyPlacesList.layoutManager = LinearLayoutManager(this)
        binding.rvHappyPlacesList.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, happyPlaceList)
        binding.rvHappyPlacesList.adapter = placesAdapter

        placesAdapter.setOnClickListener(object: HappyPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity,
                    HappyPlaceDetailsActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object: SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                val adapter = binding.rvHappyPlacesList.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(happyPlaceResultLauncher, viewHolder.absoluteAdapterPosition)
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding.rvHappyPlacesList)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                /*val adapter = binding.rvHappyPlacesList.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.absoluteAdapterPosition)

                getHappyPlacesListFromLocalDB()*/
                customDialogDelete(viewHolder)
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding.rvHappyPlacesList)
    }
    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = DataBaseHandler(this)
        val happyPlaceList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if(happyPlaceList.size > 0){
            binding.rvHappyPlacesList.visibility = View.VISIBLE
            binding.tvNoRecords.visibility = View.GONE
            setUpHappyPlacesRecyclerView(happyPlaceList)
        }else{
            binding.rvHappyPlacesList.visibility = View.GONE
            binding.tvNoRecords.visibility = View.VISIBLE
        }
    }
    private fun customDialogDelete(viewHolder: ViewHolder){
        val customDialog = Dialog(this)
        customDialog.setContentView(R.layout.dialog_custom_delete_confirmation)

        customDialog.findViewById<Button>(R.id.btnYes).setOnClickListener {
            val adapter = binding.rvHappyPlacesList.adapter as HappyPlacesAdapter
            adapter.removeAt(viewHolder.absoluteAdapterPosition)

            getHappyPlacesListFromLocalDB()
            customDialog.dismiss()
        }
        customDialog.findViewById<Button>(R.id.btnNo).setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.setOnDismissListener {
            binding.rvHappyPlacesList.adapter?.notifyItemChanged(
                viewHolder.absoluteAdapterPosition)
        }
        val transparentBackgroundDrawable = ColorDrawable(Color.TRANSPARENT)
        customDialog.window!!.setBackgroundDrawable(transparentBackgroundDrawable)
        customDialog.show()
    }
}