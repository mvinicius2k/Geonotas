package br.ufc.geonotas.models

import android.os.Build
import androidx.annotation.RequiresApi
import br.ufc.geonotas.utils.Strings
import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period

class Comment (val commentId: String, val user: User, val comment: String, val time: LocalDateTime) : Serializable {

    fun getTimeStr(): String{
        return Strings.getTimeStr1(time)

    }

    override fun equals(other: Any?): Boolean {
        if(other != null){
            if(other is Comment){
                return other.commentId == commentId
            }
        }

        return false
    }
}