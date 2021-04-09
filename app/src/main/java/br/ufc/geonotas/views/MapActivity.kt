package br.ufc.geonotas.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import br.ufc.geonotas.R
import br.ufc.geonotas.db.NoteDB
import br.ufc.geonotas.db.UserDB
import br.ufc.geonotas.models.Note
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.HashSet

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "MapActivity"

    private lateinit var mMap: GoogleMap

    private var _action = Constants.ACTION_FULL_MAP
    private lateinit var _notes: ArrayList<Note>
    private lateinit var _fusedLocationClient: FusedLocationProviderClient

    private lateinit var _notesMarked: HashMap<String, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if(intent != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.title = getString(R.string.str_map_activity_title)

        }

        if(_action == Constants.ACTION_FULL_MAP){
            _notes = ArrayList()
        }  else if (_action == Constants.ACTION_POINT_MAP)
            _notes = ArrayList()
        else
            _notes = ArrayList()

        _notesMarked = HashMap()

        GlobalScope.launch {
            getPoints()
        }




    }

    suspend fun getPoints(){


        val noteDB = NoteDB()
        val userDB = UserDB()




        var notesArray: Array<Note>? = null
        val deferred = GlobalScope.async(Dispatchers.IO){
            notesArray = noteDB.getNotes(User.loggedUser!!, MainActivity.MAIN)

        }

        try {
            deferred.await()
        } catch (e: IOException){
            toastShow(this, e.message)
            return
        }

        val avatarsToGet = HashSet<String>()

        avatarsToGet.addAll(notesArray?.map { it.op }!!)

        val avatars = LinkedHashMap<String, Bitmap?>()
        val gettingAvatars = GlobalScope.async(Dispatchers.IO){
            avatarsToGet.forEach {
                avatars[it] = userDB.getAvatar(it)
            }
        }

        try {
            gettingAvatars.await()
        } catch (e: IOException){
            toastShow(this, e.message)
            return
        }

        notesArray!!.forEach {
            it.makeAvatarBitmap(this, avatars[it.op])
            try {
                kotlin.runCatching {
                    it.address = Strings.makeLocationStrings(this, it.latitude!!,it.longitude!!)
                }
            } catch (e: IOException){
                it.address = ""
            } catch (e: IllegalAccessException){
                it.address = ""
            }
        }




        _notes.addAll(notesArray!!)

        runOnUiThread {
            val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }



    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {



        when (item.itemId){
            android.R.id.home -> {
                finish()
            }


        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        if(_action == Constants.ACTION_FULL_MAP) {
            mMap.setOnMarkerClickListener {

                Log.d(TAG, "Marcação clicada: ${it.toString()}")

                val i = _notesMarked[it.id]

                if (i != null) {
                    val note = _notes[i]


                    val intent = Intent(this, NoteActivity::class.java)
                    intent.putExtra("note", note)
                    startActivity(intent)
                }

                true


            }

        }
        fillPoints()







    }




    @SuppressLint("MissingPermission")
    private fun  fillPoints(){


        if(!hasPermissionLocation(this)) {
            Log.d(TAG,"Sem permissão para obter localização")
            return
        }



        _fusedLocationClient.lastLocation.addOnSuccessListener {location: Location? ->
            Log.d(TAG, "Pegando localização")
            User.latitude = location?.latitude
            User.longitude = location?.longitude

            Log.d(TAG, "${User.latitude} ${User.longitude}")

            if(User.latitude != null && User.longitude != null) {

                val userpoint = LatLng(User.latitude!!, User.longitude!!)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userpoint))

                val userLocation = Location("UserPoint")
                userLocation.latitude = userpoint.latitude
                userLocation.longitude = userpoint.longitude


                if(Constants.ACTION_FULL_MAP == _action){
                    var i = 0


                    _notes.forEach {
                        val point = LatLng(it.latitude!!, it.longitude!!)

                        val noteLocation = Location("NoteLocation")
                        noteLocation.latitude = point.latitude
                        noteLocation.longitude = point.longitude

                        val distance = noteLocation.distanceTo(userLocation)


                        if (distance < Preferences.distance){
                            val markerOptions = MarkerOptions()
                                .position(point)
                                .title(it.op)


                            val id = mMap.addMarker(markerOptions).id
                            _notesMarked[id] = i++
                        }


                    }
                } else {
                    val markerOptions = MarkerOptions()
                        .position(userpoint)
                        .title(User.loggedUser?.nick)

                    mMap.addMarker(markerOptions)

                }

            }

        }



    }


}