package br.ufc.geonotas.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.toBitmap
import br.ufc.geonotas.R
import br.ufc.geonotas.utils.toLocalDateTime
import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.time.LocalDateTime
import kotlin.collections.HashMap

open class Note(var id: String?, var latitude: Double?, var longitude: Double?, var op: String, var message: String, var visibility: Int,@get:Exclude var datetime: LocalDateTime): Serializable {


    var timestamp: MutableMap<String, String>? = null

    @Exclude
    var address: String? = null


    @Exclude @Transient
    var avatar: Bitmap? = null

    @Exclude
    private var _avatarBitMapMatrix: ByteArray? = null


    constructor(id: String?, latitude: Double?,  longitude: Double?,  op: String,  message: String,  visibility: Int, timestampL: Long) : this(id,latitude,longitude,op,message,visibility, toLocalDateTime(timestampL))

    constructor(note: Note) : this(note.id, note.latitude, note.longitude, note.op, note.message, note.visibility, note.datetime){

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


    @Exclude
    fun getPost() : String{
        return "$op Fez uma nova nota na ${getAdress()}"
    }

    /**
     * @return vazio se endereço for nulo
     */
    @Exclude
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
                return other.id == this.id
        return false
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + op.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + visibility
        result = 31 * result + datetime.hashCode()
        return result
    }

    companion object{
        fun fromHashMap(hs: HashMap<*,*>): Note {
            return Note(
                    id = hs[Note::id.name] as String,
                    latitude = hs[Note::latitude.name] as Double,
                    longitude = hs[Note::longitude.name] as Double,
                    message = hs[Note::message.name] as String,
                    op = hs[Note::op.name] as String,
                    timestampL = hs[Note::timestamp.name] as Long,
                    visibility = (hs[Note::visibility.name] as Long).toInt()

            )
        }

        const val VISIBILITY_FOR_ALL = 0
        const val VISIBILITY_FOLLOWERS = 1
        const val VISIBILITY_FRIENDS = 2
        private const val TAG: String = "Note"
    }
}