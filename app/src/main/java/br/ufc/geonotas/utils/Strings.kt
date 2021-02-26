package br.ufc.geonotas.utils

import java.time.Duration
import java.time.LocalDateTime

class Strings {
    companion object{
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