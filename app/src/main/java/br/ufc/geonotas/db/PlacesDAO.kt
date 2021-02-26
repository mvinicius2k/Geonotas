package br.ufc.geonotas.db

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import br.ufc.geonotas.models.Note
import br.ufc.geonotas.utils.Constants
import java.time.LocalDateTime

class PlacesDAO {

    companion object{
        val itemsListAdded: HashMap<String, Note> = HashMap()
        fun getLastPostsByUser(context: Context, nick: String, count: Int): ArrayList<Note> {
            if(Constants.TEST) {
                //-5.125757190548865, -39.73507826540769
                val array = ArrayList<Note>()
                itemsListAdded.values.forEach {
                    if (it.op.nick == "Nimguem2k") {
                        array.add(it)
                    }
                }

                return array
            }

            return ArrayList()
        }


        fun getLastMarkedPosts(context: Context, nick: String, count: Int): ArrayList<Note>{
            if(Constants.TEST){
                //-5.125757190548865, -39.73507826540769
                val array = ArrayList<Note>()
                itemsListAdded.values.forEach {
                    if(it.op.nick == "Nimguem2000"){
                        array.add(it)
                    }
                }

                return array
            }

            return ArrayList()
        }

        fun getLastPosts(context: Context, user: String, count: Int) : ArrayList<Note>{
            return ArrayList<Note>(itemsListAdded.values)
        }

        fun initTest(context: Context){
            val user1 = UsersDAO.getUser(context, "Nimguem2k")
            val user2 = UsersDAO.getUser(context, "Nimguem2000")
            user1?.makeBitmap(context)
            user2?.makeBitmap(context)

            if(user1 != null && user2 != null){
                val notes = arrayOf<Note>(
                        Note(
                                "0",
                                -5.125757190548865,
                                -39.735075,
                                user1,
                                "Achei um tesouro",
                                LocalDateTime.of(2021,10,20,10,5,23,5)),
                        Note(

                                "1",
                                -5.925757190548810,0
                                -39.73,
                                user2,
                                "Um enxame de muriçoca aqui perto, cudiado",
                                LocalDateTime.of(2021,10,20,12,50,26,25)),
                        Note(
                                "2",
                                -5.125757195,
                                -39.73507824,
                                user1,
                                "Está interditado   ",
                                LocalDateTime.of(2021,10,20,10,5,23,5)),
                        Note(

                                "3",
                                -5.925757199,0
                                -31.73,
                                user2,
                                "Tem um incêndio aqui perto",
                                LocalDateTime.of(2021,10,20,12,50,26,25))


                )

                notes.forEach {
                    it.makeLocationStrings(context)
                    itemsListAdded[it.placeId] = it
                }
            }
        }

        fun getNoteAtLatLgn(latitude: Double, longitude: Double): Note?{
            itemsListAdded.values.forEach {
                if(it.latitude == latitude && it.longitude == longitude)
                    return it
            }

            return null
        }

        fun getLastPostsByPoint(context: Context, x: Double, y: Double) : ArrayList<Note>{


            if(Constants.TEST){
                //-5.125757190548865, -39.73507826540769
                val user1 = UsersDAO.getUser(context, "Nimguem2k")
                val user2 = UsersDAO.getUser(context, "Nimguem2000")
                user1?.makeBitmap(context)
                user2?.makeBitmap(context)

                if(user1 != null && user2 != null){
                    val notes = arrayOf<Note>(
                            Note(
                                    "4",
                                    -5.125757190548865,
                                    -39.73507826540769,
                                    user1,
                                    "Está interditado",
                                    LocalDateTime.of(2021,10,20,10,5,23,5)),
                            Note(

                                    "5",
                                    -5.925757190548865,0
                                    -39.73,
                                    user2,
                                    "Tem um incêndio aqui perto",
                                    LocalDateTime.of(2021,10,20,12,50,26,25))

                    )

                    notes.forEach {
                        it.makeLocationStrings(context)
                    }


                    notes.forEach {
                        itemsListAdded[it.placeId] = it
                    }
                }
            }

            val array = ArrayList<Note>()
            itemsListAdded.values.forEach {
                if(it.op.nick == "Nimguem2000"){
                    array.add(it)
                }
            }

            return array
        }


        fun insertNote(note: Note){
            itemsListAdded[note.placeId] = note
        }

        fun updateNote(note: Note){

            itemsListAdded[note.placeId] = note

        }

        fun deleteNote(placeId: String){
            var i = 0
           itemsListAdded.remove(placeId)
        }


        fun insert(note: Note) : String {
            return ""
        }

        fun update(note: Note){


        }

        fun delete(id: String) {
            TODO("Not yet implemented")
        }

    }


}