package br.ufc.geonotas.models

import android.content.Context
import android.graphics.ImageDecoder
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import br.ufc.geonotas.R
import br.ufc.geonotas.db.UsersDAO
import br.ufc.geonotas.utils.Constants
import java.io.IOException
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*
import kotlin.IllegalArgumentException
import kotlin.system.exitProcess

open class Note(var placeId: String, var latitude: Double, var longitude: Double, var op: User, var message: String, var timestamp: LocalDateTime): Serializable {
    private val TAG: String = "Note"

    var address: String? = null

    @Transient
    private var geocoder: Geocoder? = null

    constructor(note: Note) : this(note.placeId, note.latitude, note.longitude, note.op, note.message,note.timestamp)



    @Throws(IOException::class, IllegalArgumentException::class)
    fun makeLocationStrings(context: Context){
        try{
            if(this.geocoder == null)
                this.geocoder = Geocoder(context, Locale.getDefault())

            if(geocoder == null){
                Log.d(TAG, "Geocoder nulo")
                return
            }



            val addresses = geocoder!!.getFromLocation(latitude, longitude, Constants.MAX_LOCATION)


            this.address = addresses[0].getAddressLine(0)



            /*


            this.street = "26 de Junho"
            this.city = "Boa Viagem"
            this.knowName = "Praça central"

             */
        } catch (e: IOException){
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.exp_no_conection), Toast.LENGTH_LONG).show()
            exitProcess(-1)
        }



    }



    fun getPost() : String{
        return "${op.nick} Fez uma nova nota na ${getAdress()}"
    }


    fun getAdress(): String {
        if(address != null){
            return address as String
        } else {
            return ""
        }


    }

    override fun equals(other: Any?): Boolean {
        if(other != null)
            if(other is Note)
                return other.placeId == this.placeId
        return false
    }
}