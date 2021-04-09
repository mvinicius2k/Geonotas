package br.ufc.geonotas.db

import android.util.Log
import br.ufc.geonotas.models.Note
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.Constants
import br.ufc.geonotas.utils.toLong
import br.ufc.geonotas.views.MainActivity
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.IOException
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

class NoteDB: Connection(Constants.TABLE_NOTES) {

    suspend fun removeNote(id: String, nick: String){

        coroutineScope {
            reference.child(nick).child(id).removeValue().addOnSuccessListener {
                val comment = CommentDB()
                comment.reference.child(id).removeValue().addOnSuccessListener {
                    Log.d(TAG,"Nota e comentários associados removidos")
                }.addOnFailureListener {
                    launch { throw IOException("Falha de conexão") }
                }
            }.addOnFailureListener {
                launch { throw IOException("Falha de conexão") }
            }
        }

    }

    suspend fun sendNote(note: Note){



        if (note.id == null){
            note.id = reference.push().key
            note.timestamp = ServerValue.TIMESTAMP
        }




        coroutineScope {
            if(note.id == null){
                launch {
                    throw IOException("Erro de conexão")
                }
            }

            reference.child(note.op)
                    .child(note.id!!)
                    .setValue(note)
                    .addOnSuccessListener {

                        Log.d(TAG, "Nota enviada com sucesso")
                    }.addOnFailureListener {
                        throw IOException("Erro de conexão")
                    }.await()

            if(note.timestamp == null){
                reference.child(note.op)
                        .child(note.id!!)
                        .child(Note::timestamp.name)
                        .setValue(toLong(note.datetime))
                        .addOnSuccessListener {
                            Log.d(TAG, "Tempo recuperado com sucesso")
                        }.addOnFailureListener {
                            throw IOException("Erro de conexão")
                        }.await()
            }



        }

    }

    /**
     * Ordenado pelo tempo
     */
    suspend fun getNotes(user: User, target: Int): Array<Note>{
        val notes = LinkedHashSet<Note>()



        coroutineScope {

            when(target){
                MainActivity.MY_NOTES -> {
                    reference.child(user.nick)
                            .get()
                            .addOnSuccessListener {
                                if(it.value == null)
                                    return@addOnSuccessListener
                                val hs = it.value as HashMap<*,*>
                                hs.values.forEach {
                                    notes.add(Note.fromHashMap(it as HashMap<*, *>))
                                }

                            }.addOnFailureListener {
                                launch { throw IOException("Falha de conexão") }
                            }.await()

                }

                MainActivity.MAIN -> {
                    reference.get().addOnSuccessListener {
                        if(it.value == null)
                            return@addOnSuccessListener


                        val usersHm = it.value as HashMap<*,*>
                        usersHm.values.forEach {
                            val notesHm = it as HashMap<*,*>
                            notesHm.values.forEach {
                                val note = Note.fromHashMap(it as HashMap<*,*>)
                                if(note.visibility == Note.VISIBILITY_FOR_ALL || note.op in user.friends || note.op == user.nick)
                                    notes.add(note)
                            }
                        }

                    }.addOnFailureListener {
                        launch { throw IOException("Falha de conexão") }
                    }.await()
                }

                MainActivity.MARKED -> {
                    user.friends.forEach {
                        reference.child(it)
                                .get()
                                .addOnSuccessListener {
                                    if(it.value == null)
                                        return@addOnSuccessListener
                                    val notesHm = it.value as HashMap<*,*>
                                    notesHm.values.forEach {
                                        val note = Note.fromHashMap(it as HashMap<*, *>)
                                        if(note.visibility != Note.VISIBILITY_FOR_ALL)
                                            notes.add(note)


                                    }

                                }.await()
                    }

                }

                MainActivity.FRIENDS -> {

                    val userDB = UserDB()


                    user.friends.forEach {
                        reference.child(it)
                                .get()
                                .addOnSuccessListener {
                                    if(it.value == null)
                                        return@addOnSuccessListener
                                    val notesHm = it.value as HashMap<*,*>
                                    notesHm.values.forEach {
                                        val note = Note.fromHashMap(it as HashMap<*, *>)
                                        var isFriend = false
                                        runBlocking(Dispatchers.IO) {
                                            isFriend = userDB.getUser(note.op)?.friends?.contains(user.nick)!!
                                        }

                                        if(isFriend)
                                            notes.add(note)
                                    }

                                }
                                .addOnFailureListener {
                                    launch {
                                        throw IOException("Sem conexão")
                                    }
                                }.await()
                    }
                }


                else -> {
                    Log.d(TAG,"$target não existe")

                }
            }



        }



        return notes.sortedBy { it.datetime }.toTypedArray()

    }

    companion object{
        const val TAG = "NoteDB"
    }


}