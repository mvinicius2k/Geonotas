package br.ufc.geonotas.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import br.ufc.geonotas.R
import br.ufc.geonotas.utils.Strings
import br.ufc.geonotas.utils.toLocalDateTime
import com.google.firebase.database.Exclude
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period

class Comment (var id: String?, var noteId: String, var nick: String, var comment: String,@get:Exclude  var time: LocalDateTime) : Serializable {

    constructor(id: String?, nick: String, noteId: String, comment: String,timestampL: Long) : this (id,noteId, nick, comment, toLocalDateTime(timestampL))

    var timestamp: MutableMap<String, String>? = null

    @Exclude @Transient
    var avatar: Bitmap? = null
    @Exclude
    private var _avatarBitMapMatrix: ByteArray? = null



    @Exclude
    fun getTimeStr(): String{
        return Strings.getTimeStr1(time)

    }

    override fun equals(other: Any?): Boolean {
        if(other != null){
            if(other is Comment){
                return other.id == this.id
            }
        }

        return false
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + nick.hashCode()
        result = 31 * result + comment.hashCode()
        result = 31 * result + time.hashCode()
        return result
    }

    @Exclude
    fun makeAvatarBitmap(context: Context, bitmap: Bitmap? = null){

        if(bitmap != null)
            this.avatar = bitmap
        else if(_avatarBitMapMatrix != null) {
            avatar = BitmapFactory.decodeByteArray(_avatarBitMapMatrix, 0, _avatarBitMapMatrix!!.size)
            return
        } else
            this.avatar = context.resources.getDrawable(R.drawable.ic_account_box_24px, context.theme).toBitmap()



        val baos = ByteArrayOutputStream()

        avatar?.compress(Bitmap.CompressFormat.PNG, 100, baos)

        _avatarBitMapMatrix = baos.toByteArray()

    }

    companion object{
        fun fromHashMap(hm: HashMap<*,*>): Comment{
            return Comment(
                    id = hm[Comment::id.name] as String,
                    timestampL = hm[Comment::timestamp.name] as Long,
                    nick = hm[Comment::nick.name] as String,
                    comment = hm[Comment::comment.name] as String,
                    noteId = hm[Comment::noteId.name] as String
            )
        }
    }
}