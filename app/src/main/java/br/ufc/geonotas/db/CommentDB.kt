package br.ufc.geonotas.db

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import br.ufc.geonotas.adapters.RvCommentsAdapter
import br.ufc.geonotas.models.Comment
import br.ufc.geonotas.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class CommentDB : Connection(Constants.TABLE_COMMENTS) {


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

    private suspend fun getAvatars(adapter: RvCommentsAdapter){
       if(flagThread){

           val userDB = UserDB()
           flagThread = false

           while (stack.isNotEmpty()){
               val comments = stack.pop()
               stack.clear()
               comments.forEach {
                   val deferred = GlobalScope.async(Dispatchers.IO) {
                       it.makeAvatarBitmap(adapter.context, userDB.getAvatar(it.nick))
                   }

                   try {
                       deferred.await()
                   }catch (e: IOException){
                       it.makeAvatarBitmap(adapter.context)
                   }
               }

               adapter.comments.clear()
               adapter.comments.addAll(comments)

               GlobalScope.launch(Dispatchers.Main){
                   adapter.notifyDataSetChanged()
               }

           }

           flagThread = true

       }







    }

    fun listenerNote(noteId: String, adapter: RvCommentsAdapter){

        val listener = object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Escuta cancelada")

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.value ?: return
                val list = LinkedList<Comment>()
                val commentsHm = snapshot.value as HashMap<*,*>


                commentsHm.values.forEach {
                    val comment = Comment.fromHashMap(it as HashMap<*, *>)
                    list.add(comment)




                }
                val array = list.sortedByDescending { it.time }.toTypedArray()
                stack.push(array)
                GlobalScope.launch {
                    getAvatars(adapter)
                }







                //adapter.makeAvatars()



            }


        }


        reference.child(noteId).addValueEventListener(listener)

    }


    companion object{
        private const val TAG = "CommentDB"
        val stack: Stack<Array<Comment>> = Stack()
        var flagThread = true
    }
}