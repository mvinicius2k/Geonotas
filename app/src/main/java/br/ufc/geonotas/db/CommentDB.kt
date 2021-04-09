package br.ufc.geonotas.db

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import br.ufc.geonotas.adapters.RvCommentsAdapter
import br.ufc.geonotas.models.Comment
import br.ufc.geonotas.utils.Constants
import br.ufc.geonotas.views.NoteActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CommentDB : Connection(Constants.TABLE_COMMENTS) {

    var context: Context? = null

    val listener = object : ValueEventListener{
        override fun onCancelled(error: DatabaseError) {
            Log.d(TAG, "Escuta cancelada")

        }

        override fun onDataChange(snapshot: DataSnapshot) {
            snapshot.value ?: return
            val list = ArrayList<Comment>()
            val commentsHm = snapshot.value as HashMap<*,*>


            commentsHm.values.forEach {
                val comment = Comment.fromHashMap(it as HashMap<*, *>)
                list.add(comment)




            }
            val array = list.sortedByDescending { it.time }.toTypedArray()
            stack.push(array)
            GlobalScope.launch {
                getAvatars(context!!)
            }







            //adapter.makeAvatars()



        }


    }

    fun removeListener(noteId: String, eventListener: ValueEventListener){
        reference.child(noteId).removeEventListener(eventListener)
    }

    suspend fun sendComment(comment: Comment){
        coroutineScope {

            comment.id = reference.push().key
            comment.timestamp = ServerValue.TIMESTAMP

            reference.child(comment.noteId)
                    .child(comment.id!!)
                    .setValue(comment)
                    .addOnSuccessListener {
                        Log.d(TAG, "Enviado com sucesso")
                    }.addOnFailureListener {
                        launch { throw IOException("Falha de conexão") }
                    }.await()
        }
    }

    private suspend fun getAvatars(context: Context){
       if(flagThread){
           flagThread = false
           val userDB = UserDB()


           while (stack.isNotEmpty()){
               val comments = stack.pop()
               stack.clear()

               val avatars = HashMap<String, Bitmap?>()
                Log.d(TAG,"Passa aqui")
               comments.forEach {
                   val deferred = GlobalScope.async(Dispatchers.IO) {
                       if(it.nick !in avatars.keys)
                           avatars[it.nick] = userDB.getAvatar(it.nick)
                   }

                   try {
                       deferred.await()
                   }catch (e: IOException){
                       Log.d(TAG,"Avatar de ${it.nick} não foi baixado, usando padrão")
                   }
                   it.makeAvatarBitmap(context, avatars[it.nick])

               }

               //adapter.comments.clear()
               //adapter.comments.addAll(comments)

               /*GlobalScope.launch(Dispatchers.Main){
                   Log.d(TAG,"Noticando comentários")
                   adapter.notifyDataSetChanged()
                   Log.d(TAG,"Feito")
               }*/
               GlobalScope.launch(Dispatchers.Main){
                   if(context is NoteActivity){
                       context.updateCommentsUI(comments)
                   }
               }

           }

           //GlobalScope.launch(Dispatchers.Main){
           //    Log.d(TAG,"Noticando comentários")
           //    adapter.notifyDataSetChanged()
           //    Log.d(TAG,"Feito")
           //}

           flagThread = true

       }







    }

    fun listenerNote(noteId: String, context: Context){

        this.context = context


        reference.child(noteId).addValueEventListener(listener)


    }




    companion object{
        private const val TAG = "CommentDB"
        val stack: Stack<Array<Comment>> = Stack()
        var flagThread = true
    }
}