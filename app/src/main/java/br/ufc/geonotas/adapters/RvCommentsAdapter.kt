package br.ufc.geonotas.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.ufc.geonotas.R
import br.ufc.geonotas.db.UserDB
import br.ufc.geonotas.models.Comment
import br.ufc.geonotas.myPalette.Icon
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Exception

class RvCommentsAdapter (val comments: ArrayList<Comment>, val context: Context): RecyclerView.Adapter<RvCommentsAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        val avatar = view.findViewById<Icon>(R.id.icn_note_comments_avatar)
        val nick = view.findViewById<TextView>(R.id.txt_note_comments_nick)
        val time = view.findViewById<TextView>(R.id.txt_note_comments_time)
        val comment = view.findViewById<TextView>(R.id.txt_note_comments_comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_note_comments, parent, false)
        return  MyViewHolder(view)


    }

    override fun getItemCount(): Int {
        return comments.size

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {



        holder.avatar.setImageBitmap(comments[position].avatar)
        holder.nick.text = comments[position].nick
        holder.time.text = comments[position].getTimeStr()
        holder.comment.text = comments[position].comment
    }

    fun makeAvatars() {
        val userDB = UserDB()

            comments.forEach {
                try {
                    var bitmap: Bitmap? = null
                    runBlocking {
                        bitmap = userDB.getAvatar(it.nick)

                    }
                    it.makeAvatarBitmap(context, bitmap)
                } catch (e: Exception) {
                    it.avatar = null
                }


            }



        this@RvCommentsAdapter.notifyDataSetChanged()





    }


}