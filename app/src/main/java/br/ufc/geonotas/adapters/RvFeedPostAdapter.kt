package br.ufc.geonotas.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.ufc.geonotas.R
import br.ufc.geonotas.models.Note
import br.ufc.geonotas.myPalette.Icon
import br.ufc.geonotas.utils.Strings
import br.ufc.geonotas.views.MainActivity
import org.w3c.dom.Text
import kotlin.coroutines.coroutineContext

class RvFeedPostAdapter(val context: Context, val posts: List<Note>) : RecyclerView.Adapter<RvFeedPostAdapter.MyViewHolder>() {
    private val TAG = "ItemFeedAdapter"
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val iconFeed: Icon = view.findViewById(R.id.icn_feed)
        val txtFeed: TextView = view.findViewById(R.id.txt_feed)
        val txtDate: TextView = view.findViewById(R.id.txt_post_date)
        val llayContainer = view.findViewById<LinearLayout>(R.id.llay_container)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_post_feed, parent, false)

        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  posts.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.iconFeed.setImageBitmap(posts[position].avatar)
        holder.txtFeed.text = posts[position].getPost()
        holder.txtDate.text = Strings.getTimeStr1(posts[position].datetime)

        holder.llayContainer.setOnClickListener {
            if(context is MainActivity)
                context.openNote(position)
        }


    }

}