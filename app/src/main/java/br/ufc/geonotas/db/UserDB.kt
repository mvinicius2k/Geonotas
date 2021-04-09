package br.ufc.geonotas.db

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import br.ufc.geonotas.exceptions.ArrayOverflowException
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.Constants
import br.ufc.geonotas.utils.Hash
import br.ufc.geonotas.utils.endChar
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UserDB() : Connection(Constants.TABLE_USERS) {

    private val storageInstance = FirebaseStorage.getInstance()
    private val storageRef = storageInstance.reference


    @Throws(IOException::class)
    suspend fun getUser(nick: String): User?{
        var user: User? = null

        coroutineScope {
            val gettingUser = async(Dispatchers.IO) {
                val arrayUsers = getUsers(arrayOf(nick))
                if(arrayUsers.isNotEmpty())
                    user = arrayUsers[0]
            }

            gettingUser.await()

        }

        return user
    }

    suspend fun getUsersStartsWith(string: String, max: Int = 5, withoutLoggedUser: Boolean = true): Array<User>{
        val array = LinkedList<User>()
        coroutineScope {
            reference.get()
                    .addOnSuccessListener {
                        val hs = it.value as HashMap<*,*>

                        hs.values.forEach {
                            val user = User.fromHashMap(it as HashMap<*, *>)
                            if(user.nick.startsWith(string) && array.size < max)
                                if(withoutLoggedUser){
                                    if(user.nick != User.loggedUser?.nick)
                                        array.add(user)
                                } else
                                    array.add(user)

                        }

                    }
                    .addOnFailureListener {
                        launch {
                            throw IOException("Falha de conexão")
                        }
                    }
                    .await()
        }
        return array.toTypedArray()
    }

    suspend fun getFollowers(nick: String): Array<User>{
        val followers = LinkedList<User>()
        coroutineScope {
            reference.get().addOnSuccessListener {

                val hs = it.value as HashMap<*,*>
                hs.values.forEach {
                    val user = User.fromHashMap(it as HashMap<*, *>)
                    if (nick in user.friends)
                        followers.add(user)

                }
            }.addOnFailureListener {
                launch {
                    throw IOException("Falha de conexão")
                }
            }.await()
        }

        return followers.toTypedArray()
    }

    suspend fun getUsers(nicks: Array<String>, context: Context): Array<User>{

        val linkedArray = LinkedList<User>()

        val deferred =  GlobalScope.async(Dispatchers.IO) {
            linkedArray.addAll(getUsers(nicks))
        }

        try {
            deferred.await()
        } catch (e: Exception){
            coroutineScope {
                launch { throw IOException() }
            }
        }

        linkedArray.forEach {
            val gettingAvatar = GlobalScope.async(Dispatchers.IO) {
                val bitmap = getAvatar(it.nick)
                it.makeAvatarBitmap(context, bitmap)
            }

            try {
                gettingAvatar.await()
            } catch (e: IOException){
                throw IOException("Sem conexão")
            }

        }

        return linkedArray.toTypedArray()


    }

    @Throws(IOException::class)
    suspend fun getUsers(nicks: Array<String>): Array<User> {


         val linkedArray = LinkedList<User>()

         coroutineScope {
             nicks.forEach { nick ->
                 reference.child(nick).get().addOnSuccessListener {
                     linkedArray.add(User.fromHashMap(it.value as HashMap<*, *>))
                 }.addOnFailureListener {
                     launch { throw IOException() }
                 }.await()


             }



         }
        return linkedArray.toTypedArray()

    }

    /**
     *
     * @param pass Passa por uma função hash
     *
     */
    @Throws(IOException::class)
    suspend fun login(nick: String, pass: String): Pair<ResultAuthKind, User?> {
        var user: User? = null
        var resultAuthKind = ResultAuthKind.CANCEL
        val hashpass: String = Hash.sha256(pass)

        val dsUser = reference.child(nick)
                .get()

        coroutineScope {
            dsUser.addOnSuccessListener {

                if(it.value != null){
                    val hs = it.value as HashMap<*, *>

                    if( hs[User::pass.name] != hashpass)
                        resultAuthKind = ResultAuthKind.AUTH_FAILED
                    else {
                        resultAuthKind = ResultAuthKind.AUTH_SUCESS
                        user = User.fromHashMap(hs)


                    }

                    





                } else {
                    resultAuthKind = ResultAuthKind.AUTH_FAILED
                }





            }.addOnFailureListener {
                launch {
                    throw IOException()
                }
            }.await()
        }


        return Pair(resultAuthKind, user)



    }


    @Throws(IOException::class)
    fun exists(username: String): Boolean{
        var exist = false
        if(username.isNullOrEmpty())
            return false
        runBlocking {

            reference.child(username).get().addOnSuccessListener {
                exist = it.value != null
            }.addOnFailureListener {
                launch {
                    throw IOException("Falha de conexão")
                }
            }.await()
        }

        return exist

    }

    @Throws(IOException::class)
    suspend fun updateFriends(friends: ArrayList<String>){


        coroutineScope {
            reference.child(User.loggedUser!!.nick)
                .child(User::friends.name)
                .setValue(friends)
                .addOnSuccessListener {
                    Log.d(TAG, "Lista de amigos atualizada")
                }
                .addOnFailureListener {
                    launch {
                        throw IOException("Falha de conexão")
                    }

                }.await()




        }


    }


    /**
    * Cria um usuário novo se o nick estiver disponível
     */
    @Throws(IOException::class)
    suspend fun createUser(user: User): ResultCreateKind{
        var resultDB = ResultCreateKind.UNCREATED
        user.pass = Hash.sha256(user.pass)


        val sendUser = GlobalScope.async(Dispatchers.IO){
            if(!exists(user.nick)){
                reference.child(user.nick).setValue(user).addOnSuccessListener {
                    resultDB = ResultCreateKind.CREATED
                }.addOnFailureListener {
                    launch {
                        throw IOException("Falha de conexão")
                    }
                }.await()
            } else{
                resultDB = ResultCreateKind.ALREADY_EXISTS
            }
        }

        try {
            sendUser.join()
        } catch (e: IOException){
            throw IOException()
        }

        return resultDB
    }


    @Throws(IOException::class, ArrayOverflowException::class)
    suspend fun sendAvatar(bitmap: Bitmap, nick: String): String{
        val url = Constants.DIR_AVATARS + nick + ".jpg"
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, baos)

        val megabytes = (baos.size().toDouble() / ONE_MEGABYTE.toDouble())
        if( megabytes > ONE_MEGABYTE.toDouble()){
            Log.d(TAG, "Tamanho da imagem: $megabytes MB")
            throw ArrayOverflowException("Imagem muito grande, selecione uma menor")
        }


        val avatarImageRef = storageRef.child(url)
        val data = baos.toByteArray()

        coroutineScope {


            avatarImageRef.putBytes(data).addOnFailureListener {
                launch {
                    throw IOException("Sem conexão")
                }
            }.addOnSuccessListener {

            }.await()
        }

        return url

    }

    suspend fun getAvatar(nick: String): Bitmap? = withContext(Dispatchers.IO){
        var bitmap: Bitmap? = null
        val avatarRef = storageRef.child(Constants.DIR_AVATARS + nick + ".jpg")


        coroutineScope{

            avatarRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            }.addOnFailureListener {
                launch {
                    throw IOException()
                }
            }.await()
        }

        return@withContext bitmap

    }



    companion object{
        private const val TAG = "UserDB"


    }
}