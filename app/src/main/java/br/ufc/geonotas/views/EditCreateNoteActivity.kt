package br.ufc.geonotas.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ufc.geonotas.R
import br.ufc.geonotas.adapters.RvMarkedListAdapter
import br.ufc.geonotas.db.PlacesDAO
import br.ufc.geonotas.db.UsersDAO
import br.ufc.geonotas.models.Note
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.Constants
import br.ufc.geonotas.utils.Strings
import com.google.android.gms.maps.MapView
import java.time.LocalDateTime
import kotlin.Exception

class EditCreateNoteActivity : AppCompatActivity() {

    lateinit var ivNoteAvatar: ImageView
    lateinit var txtNoteNick: TextView
    lateinit var txtNoteTime: TextView

    lateinit var txtNoteAddress: TextView
    lateinit var etNoteMessage: EditText
    lateinit var acMark: AutoCompleteTextView
    lateinit var rvMarked: RecyclerView
    lateinit var btnEditCreate: Button




    lateinit var markedList: ArrayList<User>
    lateinit var rvMarkedAdapter: RecyclerView.Adapter<*>
    private lateinit var rvMarkedManager: RecyclerView.LayoutManager

    private var _placeId: String? = null
    private var _editing: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_create_note)

        if(intent != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.title = intent.getStringExtra("title")
         }



        markedList = ArrayList()

        ivNoteAvatar = findViewById(R.id.iv_edit_create_note_avatar)
        txtNoteNick = findViewById(R.id.txt_edit_create_note_nick)
        txtNoteTime = findViewById(R.id.txt_edit_create_note_time)

        txtNoteAddress = findViewById(R.id.txt_editcreate_note_address)
        etNoteMessage = findViewById(R.id.et_editcreate_note_message)
        acMark = findViewById(R.id.ac_editcreate_note_mark)
        rvMarked = findViewById(R.id.rv_editcreate_note_maked)
        btnEditCreate = findViewById(R.id.btn_editcreate_note_create)



        rvMarkedManager = LinearLayoutManager(this)


        val markSuggestions = ArrayList<User>()


        ivNoteAvatar.setImageBitmap(User.loggedUser?.avatar)
        txtNoteNick.text = User.loggedUser?.nick



        rvMarkedAdapter = RvMarkedListAdapter(markedList, this)
        rvMarked.apply {
            setHasFixedSize(false)
            layoutManager = rvMarkedManager
            adapter = rvMarkedAdapter
        }



        editActions()


        acMark.setOnItemClickListener { parent, view, position, id ->

        }

        acMark.setOnKeyListener { _, keyCode, _ ->

            if(keyCode == KeyEvent.KEYCODE_ENTER){
                val user = UsersDAO.getUser(this, acMark.text.toString())
                if(user != null){

                    if(!markedList.contains(user)){
                        markedList.add(user)
                        rvMarkedAdapter.notifyDataSetChanged()
                        acMark.setText("")
                    }


                }



            }

            true
        }

        btnEditCreate.setOnClickListener {

            var id: String


            if(_editing){
                id = _placeId!!
            } else {
                if(Constants.TEST)
                    id = etNoteMessage.text.toString().hashCode().toString()
                else
                    id = ""
            }

            val note = Note(
                    id,
                    getLongitude(),
                    getLatitude(),
                    User.loggedUser!!,
                    etNoteMessage.text.toString(),
                    LocalDateTime.now()

            )

            try{

                note.makeLocationStrings(this)

                if(_editing){
                    PlacesDAO.updateNote(note)
                } else {
                    PlacesDAO.insertNote(note)
                    Toast.makeText(this, "Nota criada com sucesso", Toast.LENGTH_LONG).show()
                }





                finish()
            } catch (x: Exception){
                x.printStackTrace()
                throw x
            }


        }
    }

    fun editActions(){
        val note = intent.getSerializableExtra("note")

        if(note != null){
            if(note is Note){
                _editing = true
                var marked = UsersDAO.getUsersMarkedsAtPlace(this, note.placeId)

                if(marked == null)
                    marked = ArrayList<User>()

                _placeId = note.placeId
                txtNoteAddress.text = note.getAdress()
                etNoteMessage.setText(note.message)
                markedList.addAll(marked)
                txtNoteTime.text = Strings.getTimeStr1(note.timestamp)
                btnEditCreate.text = getString(R.string.str_editcreate_activity_finalize)


            }

        }


    }

    //-5.125757190548865, -39.73507826540769
    /**
     * Obtém o equivalente à coordenada X no mapa bidimensional
      */
    fun getLongitude(): Double{
        return -5.125757190548865
    }

    /**
     * Obtém o equivalente à coordenada Y no mapa bidimensional
     */
    fun getLatitude(): Double{
        return -39.73507826540769
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {
                finish()
            }


        }
        return super.onOptionsItemSelected(item)
    }
}