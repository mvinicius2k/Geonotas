package br.ufc.geonotas.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import br.ufc.geonotas.R
import br.ufc.geonotas.db.PlacesDAO
import br.ufc.geonotas.models.Note
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.Constants
import br.ufc.geonotas.utils.Preferences
import com.google.android.gms.common.api.internal.TaskUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import java.lang.Exception

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

        if(_action == Constants.ACTION_FULL_MAP)
            _notes = intent.getSerializableExtra("notes") as ArrayList<Note>
        else if (_action == Constants.ACTION_POINT_MAP)
            _notes = ArrayList()
        else
            _notes = ArrayList()

        _notesMarked = HashMap()
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
                    if (note != null) {


                        val intent = Intent(this, NoteActivity::class.java)
                        intent.putExtra("note", note)
                        startActivity(intent)
                    }
                }

                true


            }

        }
        fillPoints()









        // Add a marker in Sydney and move the camera
        /*val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        */







    }



    @SuppressLint("MissingPermission")
    fun  fillPoints(){


        permissionCheck()



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
                        val point = LatLng(it.latitude, it.longitude)

                        val noteLocation = Location("NoteLocation")
                        noteLocation.latitude = point.latitude
                        noteLocation.longitude = point.longitude

                        val distance = noteLocation.distanceTo(userLocation)


                        if (distance < Preferences.distance){
                            val markerOptions = MarkerOptions()
                                .position(point)
                                .title(it.op.nick)


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

    fun permissionCheck() {
        //Verificar permissões
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Chamar para aceitar permições

            throw NoSuchElementException()
            return
        }


    }
}