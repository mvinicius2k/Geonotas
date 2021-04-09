package br.ufc.geonotas.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.ufc.geonotas.R
import br.ufc.geonotas.models.User
import br.ufc.geonotas.myPalette.Icon
import br.ufc.geonotas.views.EditCreateNoteActivity
import br.ufc.geonotas.views.ProfileActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RvMarkedListAdapter (val users: ArrayList<User>, val context: Context, val removable: Boolean = true):
    RecyclerView.Adapter<RvMarkedListAdapter.MyViewHolder>() {

    private val TAG = "RvMarkedListAdapter"



    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val icon: Icon = view.findViewById(R.id.icn_marked_item_avatar)
        val nick: TextView = view.findViewById(R.id.txt_marked_item_nick)
        val fullname: TextView = view.findViewById(R.id.txt_marked_item_fullname)
        val ibRemove: ImageButton = view.findViewById(R.id.ib_marked_item_remove)



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvMarkedListAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_marked_item, parent, false)

        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  users.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val user = users[position]

        holder.icon.setImageBitmap(user.avatar)
        holder.nick.text = user.nick
        holder.fullname.text = user.getFullname()

        if(!removable)
            holder.ibRemove.visibility = View.GONE

        holder.ibRemove.setOnClickListener {

            GlobalScope.launch {
                if(context is ProfileActivity){

                    context.removeFriend(user.nick)
                }
            }


        }


    }
}