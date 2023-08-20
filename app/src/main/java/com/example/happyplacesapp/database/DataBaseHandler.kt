package com.example.happyplacesapp.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.happyplacesapp.models.HappyPlaceModel

class DataBaseHandler(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "HappyPlaces.db"
        private const val HAPPY_PLACES_TABLE = "HappyPlacesTable"

        private const val COLUMN_ID = "_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_IMAGE = "image"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_LOCATION = "location"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_HAPPY_PLACES_TABLE = ("CREATE TABLE " + HAPPY_PLACES_TABLE
                + "(" + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_IMAGE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_LOCATION + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_LATITUDE + " TEXT,"
                + COLUMN_LONGITUDE + " TEXT)")

        db?.execSQL(CREATE_HAPPY_PLACES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + HAPPY_PLACES_TABLE)
        onCreate(db)
    }

    fun addHappyPlace(happyPlace: HappyPlaceModel
    ): Long{
        val values = ContentValues()
        values.put(COLUMN_TITLE, happyPlace.title)
        values.put(COLUMN_IMAGE, happyPlace.image)
        values.put(COLUMN_DESCRIPTION, happyPlace.description)
        values.put(COLUMN_DATE, happyPlace.date)
        values.put(COLUMN_LOCATION, happyPlace.location)
        values.put(COLUMN_LATITUDE, happyPlace.latitude)
        values.put(COLUMN_LONGITUDE, happyPlace.longitude)

        val db = this.writableDatabase

        val result = db.insert(HAPPY_PLACES_TABLE, null, values)
        db.close()

        return result
    }
    fun updateHappyPlace(happyPlace: HappyPlaceModel): Int{
        val values = ContentValues()
        values.put(COLUMN_TITLE, happyPlace.title)
        values.put(COLUMN_IMAGE, happyPlace.image)
        values.put(COLUMN_DESCRIPTION, happyPlace.description)
        values.put(COLUMN_DATE, happyPlace.date)
        values.put(COLUMN_LOCATION, happyPlace.location)
        values.put(COLUMN_LATITUDE, happyPlace.latitude)
        values.put(COLUMN_LONGITUDE, happyPlace.longitude)

        val db = this.writableDatabase

        val success = db.update(
            HAPPY_PLACES_TABLE,
            values,
            COLUMN_ID + "=" + happyPlace.id,
            null)

        db.close()
        return success
    }
    fun deleteHappyPlace(happyPlace: HappyPlaceModel): Int{
        val db = this.writableDatabase
        val sucess = db.delete(
            HAPPY_PLACES_TABLE,
            COLUMN_ID + "=" + happyPlace.id,
            null)
        db.close()
        return sucess
    }
    fun getHappyPlacesList(): ArrayList<HappyPlaceModel>{
        val happyPlaceList = ArrayList<HappyPlaceModel>()
        val selectQuery = "SELECT * FROM $HAPPY_PLACES_TABLE"
        val db = this.readableDatabase

        try {
            val cursor: Cursor = db.rawQuery(selectQuery, null)

            if(cursor.moveToFirst()){
                do{
                    val place = HappyPlaceModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))
                    )
                    happyPlaceList.add(place)
                }while(cursor.moveToNext())
            }
            cursor.close()
        }catch (e: SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()
        }

        return happyPlaceList
    }
}