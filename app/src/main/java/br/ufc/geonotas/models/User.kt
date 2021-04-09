package br.ufc.geonotas.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.toBitmap
import br.ufc.geonotas.R
import com.google.firebase.database.Exclude
import java.io.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class User (var nick: String, var email: String, var pass: String, var name: String, var surname: String, var iconSrc: String, var friends: ArrayList<String>): Serializable{



    @Transient
    var avatar : Bitmap? = null

    @Exclude
    private var _avatarBitMapMatrix: ByteArray? = null




    /**
     * Seta um avatar para o usuário no objeto e o armazena em [_avatarBitMapMatrix].
     * Caso o bitmap repassado seja null, o avatar será recuperado se
     * antes armazenado em [_avatarBitMapMatrix], ou o avatar padrão será setado
     */
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

        private val TAG: String = this::class.simpleName!!

        fun fromHashMap(hs: HashMap<*,*>): User{
            return User(
                    nick = hs[User::nick.name] as String,
                    friends = if (hs[User::friends.name] == null) ArrayList() else hs[User::friends.name] as ArrayList<String>,
                    pass = hs[User::pass.name] as String,
                    name = hs[User::name.name] as String,
                    surname = hs[User::surname.name] as String,
                    email = hs[User::email.name] as String,
                    iconSrc = hs[User::iconSrc.name] as String


            )
        }

    }




}