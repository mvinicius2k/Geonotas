package br.ufc.geonotas.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.util.TypedValue
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.ufc.geonotas.models.User
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

const val endChar = '\uf8ff'

fun toLocalDateTime(time: Long): LocalDateTime {
    val date = Date(time)

    return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

}

fun toLong(dateTime: LocalDateTime): Long{
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}


/**
 * @return Um par <latitude, longitude>
 */
@Throws(IllegalAccessException::class, IOException::class)
@SuppressLint("MissingPermission") //Verifica permissão sim
suspend  fun updateUserCoordinates(context: Context){
    var pair: Pair<Double, Double>? = null
    if(hasPermissionLocation(context)){

        val locationRequest = LocationRequest.create()
        locationRequest.interval = 20 * 1000;
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;

        val locationCallBack = object : LocationCallback() {

            override fun onLocationResult(p0: LocationResult) {
                p0.locations.forEach {
                    pair = Pair(it.latitude, it.longitude)

                }

                User.latitude = pair?.first
                User.longitude = pair?.second

            }
        }



        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        /**
        coroutineScope {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                pair = Pair(it.latitude,it.longitude)

            }.addOnFailureListener {
                throw IOException("Sem conexão")
            }.await()
        }

        */

        coroutineScope {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper())
                    .addOnSuccessListener {
                        Log.d("Location", "Sucesso")
                    }.addOnFailureListener {
                        launch { throw  IOException("Falha de conexão") }
                    }.await()
        }




        //fusedLocationClient.removeLocationUpdates(locationCallBack)
        //return pair

    } else{

    }








}


fun hasPermissionLocation(context: Context): Boolean {
    //Verificar permissões
    if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
    ) {
        //toastShow(context, "Não há permissão para obter sua localização")
        return false



    } else{
        Log.d("Permission", "Permissão para obeter localização com sucesso")
        return true
    }



}
fun toastShow(context: Context, string: CharSequence?, duration: Int = Toast.LENGTH_LONG){
    if(string.isNullOrBlank())
        Log.d(context.packageName!!, "string nula ou branca")
    else
        Toast.makeText(context, string, duration).show()

}

fun isNullOrBlank(editable: Editable): Boolean{
    return editable.toString().isNullOrBlank()

}

fun nullOrBlank(editText: EditText, context: Context) {
    if(editText.text.toString().isNullOrBlank()){
        editText.background.setTint(Color.RED)
    } else {
        editText.background.setTint(resolveColorAttr(context, android.R.attr.textColorPrimary))
    }
}

fun hasNullOrBlank(list: List<Editable>): Boolean{
    list.forEach {
        if(isNullOrBlank(it))
            return true
    }
    return false
}

fun resolveThemeAttr(context: Context, @AttrRes attrRes: Int): TypedValue {
    val theme = context.theme
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue
}

@ColorInt
fun resolveColorAttr(context: Context, @AttrRes colorAttr: Int): Int {
    val resolvedAttr = resolveThemeAttr(context, colorAttr)
    // resourceId is used if it's a ColorStateList, and data if it's a color reference or a hex color
    val colorRes = if (resolvedAttr.resourceId != 0)
        resolvedAttr.resourceId
    else
        resolvedAttr.data
    return ContextCompat.getColor(context, colorRes)
}

