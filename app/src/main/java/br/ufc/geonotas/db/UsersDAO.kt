package br.ufc.geonotas.db

import android.content.Context
import br.ufc.geonotas.models.Note
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.Constants

class UsersDAO {
    companion object{

        val markedList = HashMap<String, ArrayList<User>>()


        fun getAvatarSrc(nick: String): String {
            return ""
        }

        fun getUser(context: Context, nick: String): User? {
            if (Constants.TEST) {
                val keys = mapOf(
                        "Nimguem2k" to User("Nimguem2k", "Marcos", "Vinícius", ""),
                        "Nimguem2000" to User("Nimguem2000", "Lima", "Venâncio", "")
                )

                keys.forEach {
                    it.value.makeBitmap(context)
                }
                return keys[nick];
            }

            return null

        }

        fun getUsersMarkedsAtPlace(context: Context, placeId: String): ArrayList<User>?{
            val user1 = getUser(context, "Nimguem2k")
            val user2 = getUser(context, "Nimguem2000")



            markedList["0"] = arrayListOf(user1!!,user2!!)
            markedList["1"] = arrayListOf(user1!!)
            markedList["2"] = arrayListOf(user2!!)



            return  markedList[placeId]

        }

        fun insertUsersToPlace(placeId: String,users: ArrayList<User>){
            markedList[placeId] = users
        }

    }


}