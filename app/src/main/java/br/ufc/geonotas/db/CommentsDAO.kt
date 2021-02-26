package br.ufc.geonotas.db

import android.content.Context
import br.ufc.geonotas.models.Comment
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.Constants
import java.time.LocalDateTime

class CommentsDAO {
    companion object{

        val commentsList = ArrayList<Comment>()


        fun getLastCommentsOfNote(context: Context, noteId: String): ArrayList<Comment>{

            if(Constants.TEST){
                val user1 = UsersDAO.getUser(context, "Nimguem2k")
                val user2 = UsersDAO.getUser(context, "Nimguem2000")
                val comment1 = Comment("0",user1!!,"Que loucura, jamais pisei aí", LocalDateTime.of(2021,1,21,12,10,40))
                val comment2 = Comment("1",user2!!,"Pois é, cuidado", LocalDateTime.of(2021,1,21,12,10,55))
                val comment3 = Comment("2",user1!!,"Já cheguei perto", LocalDateTime.of(2021,1,21,12,11,2))

                val array = arrayOf(comment1, comment2, comment3)

                array.forEach {
                    if(!commentsList.contains(it))
                        commentsList.add(it)
                }

            }

            return commentsList

        }



    }
}