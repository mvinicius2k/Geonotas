package br.ufc.geonotas.views

import android.content.ClipData
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ufc.geonotas.R
import br.ufc.geonotas.adapters.RvCommentsAdapter
import br.ufc.geonotas.db.CommentsDAO
import br.ufc.geonotas.db.PlacesDAO
import br.ufc.geonotas.db.UsersDAO
import br.ufc.geonotas.models.Comment
import br.ufc.geonotas.models.Note
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.Constants
import br.ufc.geonotas.utils.Strings
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
        _note.op.makeBitmap(this)


        fillFields()

        btnSend.setOnClickListener {

            sendComment()
        }







        
    }

    fun fillFields(){

        _commentsList = CommentsDAO.getLastCommentsOfNote(this,_note.placeId)

        ivNoteAvatar.setImageBitmap(_note.op.avatar)
        txtNoteNick.text = _note.op.nick
        txtNoteTime.text = Strings.getTimeStr1(_note.timestamp)
        txtNoteMessage.text = _note.message
        txtNoteAddress.text = _note.getAdress()



        _rvCommentsManager = LinearLayoutManager(this)
        _rvCommentsAdapter = RvCommentsAdapter(_commentsList)
        rvComments.apply {
            setHasFixedSize(false)
            layoutManager = _rvCommentsManager
            adapter = _rvCommentsAdapter
        }
    }

    private fun sendComment(){



        val commentText = etComment.text.toString()

        if(commentText.isBlank()){
            Toast.makeText(this,getString(R.string.str_note_activity_empty_message), Toast.LENGTH_SHORT).show()
            return
        }

        if(Constants.TEST){
            val comment = Comment(
                LocalDateTime.now().hashCode().toString(),
                UsersDAO.getUser(this, Constants.USER_NICK_SECTION)!!,
                commentText,
                LocalDateTime.now()



            )

            if(!CommentsDAO.commentsList.contains(comment))
                CommentsDAO.commentsList.add(comment)

            _rvCommentsAdapter.notifyDataSetChanged()

        }




        etComment.setText("")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note, menu)

        if(User.loggedUser != _note.op){
            menu?.findItem(R.id.menu_my_notes_remove)!!.isVisible = false
            menu?.findItem(R.id.menu_my_notes_edit)!!.isVisible = false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {



        when (item.itemId){
            android.R.id.home -> {
                finish()
            }

            R.id.menu_my_notes_remove -> {
                try{
                    PlacesDAO.deleteNote(_note.placeId)
                    Toast.makeText(this, getString(R.string.str_note_activity_remove_sucess), Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception){

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