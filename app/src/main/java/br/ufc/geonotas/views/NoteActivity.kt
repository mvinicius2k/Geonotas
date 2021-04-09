package br.ufc.geonotas.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ufc.geonotas.R
import br.ufc.geonotas.adapters.RvCommentsAdapter
import br.ufc.geonotas.db.*
import br.ufc.geonotas.models.Comment
import br.ufc.geonotas.models.Note
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.Constants
import br.ufc.geonotas.utils.Strings
import br.ufc.geonotas.utils.toastShow
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Exception
import java.time.LocalDateTime

class NoteActivity : AppCompatActivity() {

    lateinit var ivNoteAvatar: ImageView
    lateinit var txtNoteNick: TextView
    lateinit var txtNoteTime: TextView
    lateinit var txtNoteMessage: TextView
    lateinit var txtNoteAddress: TextView
    lateinit var icnNoteCommentAvatar: br.ufc.geonotas.myPalette.Icon
    lateinit var rvComments: RecyclerView
    lateinit var btnSend: Button
    lateinit var etComment: EditText

    private lateinit var _rvCommentsAdapter: RecyclerView.Adapter<*>
    private lateinit var _rvCommentsManager: RecyclerView.LayoutManager
    private lateinit var _commentsList: ArrayList<Comment>
    private lateinit var _note: Note

    private var listenerRef: ValueEventListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        if(intent != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.title = getString(R.string.str_note_activity_title)

        }

        _commentsList = ArrayList()

        ivNoteAvatar = findViewById(R.id.iv_note_avatar)
        txtNoteNick = findViewById(R.id.txt_note_nick)
        txtNoteTime = findViewById(R.id.txt_note_time)
        txtNoteMessage = findViewById(R.id.txt_note_message)
        txtNoteAddress = findViewById(R.id.txt_note_address)
        icnNoteCommentAvatar = findViewById(R.id.icn_note_comment_avatar)
        rvComments = findViewById(R.id.rv_note_comments)
        btnSend = findViewById(R.id.btn_note_send_comment)
        etComment = findViewById(R.id.et_note_comment)


        _note = intent.getSerializableExtra("note") as Note
        _note.makeAvatarBitmap(this)



        fillFields()

        fillComments()

        btnSend.setOnClickListener {
            GlobalScope.launch {
                sendComment()
            }

        }







        
    }

    fun fillFields(){




        icnNoteCommentAvatar.setImageBitmap(User.loggedUser?.avatar)
        ivNoteAvatar.setImageBitmap(_note.avatar)
        txtNoteNick.text = _note.op
        txtNoteTime.text = Strings.getTimeStr1(_note.datetime)
        txtNoteMessage.text = _note.message
        txtNoteAddress.text = _note.getAdress()



        _rvCommentsManager = LinearLayoutManager(this)
        _rvCommentsAdapter = RvCommentsAdapter(_commentsList, this)
        rvComments.apply {
            setHasFixedSize(false)
            layoutManager = _rvCommentsManager
            adapter = _rvCommentsAdapter
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("NoteActivity","Parando")
        val commentDB = CommentDB()
        commentDB.removeListener(_note.id!!, listenerRef!!)
    }



    private fun fillComments(){
        val commentDB = CommentDB()
        commentDB.listenerNote(_note.id!!, this)
        this.listenerRef = commentDB.listener

    }

    public fun updateCommentsUI(array: Array<Comment>?){
        if(array != null){
            _commentsList.clear()
            _commentsList.addAll(array)
            _rvCommentsAdapter.notifyDataSetChanged()
        }

    }

    private suspend fun sendComment(){

        val commentDB = CommentDB()

        val commentText = etComment.text.toString()

        if(commentText.isNullOrBlank()){
            Toast.makeText(this,getString(R.string.str_note_activity_empty_message), Toast.LENGTH_SHORT).show()
            return
        }


        val comment = Comment(
            null,
                _note.id!!,
            User.loggedUser?.nick!!,
            commentText,
            LocalDateTime.now()
        )

        val deferred = GlobalScope.async (Dispatchers.IO){
            commentDB.sendComment(comment)
        }

        try {
            deferred.await()
        } catch (e: IOException){
            toastShow(this,e.message)
        }








        runOnUiThread { etComment.setText("") }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note, menu)

        if(User.loggedUser?.nick != _note.op){
            menu?.findItem(R.id.menu_my_notes_remove)!!.isVisible = false
            menu?.findItem(R.id.menu_my_notes_edit)!!.isVisible = false
        }

        return true
    }

    private suspend fun removeNote(){
        val noteDB = NoteDB()

        val deferred = GlobalScope.async(Dispatchers.IO){
            noteDB.removeNote(_note.id!!,_note.op)
        }
        try {
            deferred.await()
        } catch (e: IOException){
            runOnUiThread { toastShow(this, e.message) }
        }

        runOnUiThread {
            toastShow(this, "Nota apagada")
        }
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {



        when (item.itemId){
            android.R.id.home -> {
                finish()
            }

            R.id.menu_my_notes_remove -> {
                GlobalScope.launch {
                    removeNote()
                }

            }

            R.id.menu_my_notes_edit -> {
                val intent = Intent(this, EditCreateNoteActivity::class.java)
                intent.putExtra("note", _note)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}