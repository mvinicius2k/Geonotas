package br.ufc.geonotas.utils

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import br.ufc.geonotas.R
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.system.exitProcess

class Strings {
    companion object{

        @Throws(IOException::class, IllegalArgumentException::class)
        fun makeLocationStrings(context: Context, latitude: Double, longitude: Double): String? {


            val geocoder = Geocoder(context, Locale.getDefault())

            val addresses = geocoder.getFromLocation(latitude, longitude, Constants.MAX_LOCATION)


            return addresses[0].getAddressLine(0)


        }

        /**
         * Retorna num padrão "Agora", "Há 1 minuto", "Há x minutos" ou "dd/MM/yyyy hh:mm"
         */
        private fun isToday(time: LocalDateTime): Boolean{
            val now = LocalDateTime.now()
            return time.dayOfYear == now.dayOfYear && time.year == now.year
        }

        fun getTimeStr1(time: LocalDateTime): String{
            val now = LocalDateTime.now()



            if(isToday(time)){
                val difference = Duration.between(time, now)
                if(difference.toMinutes() == 0L){
                    return "Agora"
                } else {
                    if (difference.toMinutes() == 1L){
                        return "Há 1 minuto"
                    } else if(difference.toMinutes() < 60L) {
                        return "Há ${difference.toMinutes()} minutos"
                    } else{
                        if(difference.toHours() == 1L)
                            return "Há 1 hora"
                         else
                            return "Há ${difference.toHours()} horas"
                    }
                }
            } else {
                return "${time.dayOfMonth}/${time.monthValue}/${time.year} ${time.hour}:${minuteToStr(time.minute)}"
            }

        }

        fun minuteToStr(value: Int): String{
            if(value < 10)
                return "0" + value.toString()
            else
                return value.toString()
        }


    }
}