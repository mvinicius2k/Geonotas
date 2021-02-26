package br.ufc.geonotas.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.location.Geocoder
import android.location.Location
import android.location.LocationProvider
import android.media.Image
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import br.ufc.geonotas.R
import br.ufc.geonotas.db.UsersDAO
import br.ufc.geonotas.utils.Constants
import java.io.*
import java.util.*
import kotlin.system.exitProcess

class User (var nick: String, var name: String, var surname: String, var iconSrc: String): Serializable{


    @Transient
    var avatar : Bitmap? = null
    private val TAG: String = "User"

    private var _avatarBitMapMatrix: ByteArray? = null




    fun makeBitmap(context: Context){

        val uri = Uri.parse(UsersDAO.getAvatarSrc(nick))

        if(_avatarBitMapMatrix != null){
            avatar = BitmapFactory.decodeByteArray(_avatarBitMapMatrix, 0, _avatarBitMapMatrix!!.size)
        }

        if(Build.VERSION.SDK_INT >= 29){
            val src = ImageDecoder.createSource(context.contentResolver, uri)
            try {
                avatar = ImageDecoder.decodeBitmap(src)
            } catch (e: IOException){


                avatar = context.resources.getDrawable(R.drawable.ic_account_box_24px, context.theme).toBitmap()

            }
            //
        } else {
            try {
                avatar = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } catch (e: IOException){
                avatar = context.resources.getDrawable(R.drawable.ic_account_box_24px, context.theme).toBitmap()
            }
        }

        val byteArray = ByteArrayOutputStream()

        avatar?.compress(Bitmap.CompressFormat.PNG, 100, byteArray)

        _avatarBitMapMatrix = byteArrayOf()

    }



    fun getFullname(): String {
        return "${name} ${surname}"
    }

    override fun equals(other: Any?): Boolean {
        if(other != null){
            if(other is User){
                return other.nick == this.nick
            }
        }

        return false

    }

    companion object{
        var latitude: Double? = null
        var longitude: Double? = null

        var loggedUser: User? = null

        private val TAG: String = "User"


    }




}