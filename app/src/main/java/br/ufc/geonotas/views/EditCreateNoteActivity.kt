package br.ufc.geonotas.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import br.ufc.geonotas.R
import br.ufc.geonotas.db.NoteDB
import br.ufc.geonotas.models.Note
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.Strings
import br.ufc.geonotas.utils.toastShow
import kotlinx.coroutines.*
import java.io.IOException
import java.time.LocalDateTime

class EditCreateNoteActivity : AppCompatActivity() {

    lateinit var ivNoteAvatar: ImageView
    lateinit var txtNoteNick: TextView
    lateinit var txtNoteTime: TextView

    lateinit var txtNoteAddress: TextView
    lateinit var etNoteMessage: EditText
    lateinit var btnEditCreate: Button

    private lateinit var rgVisibility: RadioGroup
    private lateinit var radAll: RadioButton
    private lateinit var radFollowers: RadioButton
    private lateinit var radFriends: RadioButton



    private var _noteId: String? = null
    private var _editing: Boolean = false
    private var _note: Note? = null

    private var latitude: Double? = null
    private var longitude: Double? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_create_note)

        if(intent != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.title = intent.getStringExtra("title")
         }

        this._note = intent.getSerializableExtra("note") as Note?





        ivNoteAvatar = findViewById(R.id.iv_edit_create_note_avatar)
        txtNoteNick = findViewById(R.id.txt_edit_create_note_nick)
        txtNoteTime = findViewById(R.id.txt_edit_create_note_time)

        txtNoteAddress = findViewById(R.id.txt_editcreate_note_address)
        etNoteMessage = findViewById(R.id.et_editcreate_note_message)

        btnEditCreate = findViewById(R.id.btn_editcreate_note_create)



        this.rgVisibility = findViewById(R.id.rg_edit_create_note_visibility)
        this.radAll = findViewById(R.id.rad_editcreate_note_visibility_all)
        this.radFollowers = findViewById(R.id.rad_editcreate_note_visibility_followers)
        this.radFriends = findViewById(R.id.rad_editcreate_note_visibility_friends)




        ivNoteAvatar.setImageBitmap(User.loggedUser?.avatar)
        txtNoteNick.text = User.loggedUser?.nick
        txtNoteTime.text = Strings.getTimeStr1(LocalDateTime.now())

        editActions()

        GlobalScope.launch {
            updateAddress()
        }


        btnEditCreate.setOnClickListener {

            GlobalScope.launch {
                create()
            }


        }
    }

    private suspend fun updateAddress(){

        /**
        var coordinates: Pair<Double,Double>? = null
        val deferred = GlobalScope.async(Dispatchers.IO){
            coordinates = getUserCoordinates(this@EditCreateNoteActivity)
        }

        try {
            deferred.await()
        } catch (e: IOException){
            runOnUiThread { toastShow(this, e.message) }
            return
        } catch (e: IllegalAccessException){
            runOnUiThread { toastShow(this, e.message) }

            return
        }

        //latitude = coordinates?.first
        //longitude = coordinates?.second
        */
        latitude = User.latitude
        longitude = User.longitude


        if(latitude != null && longitude != null){
            try {
                kotlin.runCatching {
                    val address = Strings.makeLocationStrings(this,latitude!!,longitude!!)
                    runOnUiThread {
                        txtNoteAddress.text = address
                    }
                }
            } catch (e: IOException){
                runOnUiThread { toastShow(this, e.message) }
                return
            } catch (e: IllegalAccessException){
                runOnUiThread { toastShow(this, e.message) }
                return
            }

        } else {
            Log.d(TAG,"latitude ou longitude nulos")
        }




    }

    private suspend fun create(){

        var id: String? = null
        val noteDB = NoteDB()

        if(_editing){
            id = _noteId!!
            latitude = _note?.latitude
            longitude = _note?.longitude
        } else if(latitude == null || longitude == null){



            //var pair: Pair<Double,Double>? = null


/**
            val deferred = GlobalScope.async(Dispatchers.IO){
                pair = getUserCoordinates(this@EditCreateNoteActivity)
            }

            try {
                deferred.await()
            } catch (e: IOException){
                toastShow(this,e.message)
                return
            } catch (e: IllegalAccessException){
                toastShow(this, e.message)
                return
            }

            if(pair != null){
                latitude = pair?.first
                longitude = pair?.second
            }

            */
        }

        latitude = User.latitude
        longitude = User.longitude
        val visibility = when (rgVisibility.checkedRadioButtonId){
            radAll.id -> Note.VISIBILITY_FOR_ALL
            radFollowers.id -> Note.VISIBILITY_FOLLOWERS
            radFriends.id -> Note.VISIBILITY_FRIENDS
            else -> {
                Log.d("wtf","wtf")
                return
            }
        }

        val note = Note(
                id,
                latitude,
                longitude,
                User.loggedUser!!.nick,
                etNoteMessage.text.toString(),
                visibility,
                LocalDateTime.now()

        )





         val deferred = GlobalScope.async(Dispatchers.IO){

             noteDB.sendNote(note)
         }

         try {
             deferred.await()
         }catch (e: IOException){
             toastShow(this,"Falha de conexão")
             return
         }


        runOnUiThread {
            if(_editing){
                Toast.makeText(this, "Nota editada com sucesso", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Nota criada com sucesso", Toast.LENGTH_LONG).show()
            }
        }



         finish()




    }

    private fun editActions(){


        if(_note != null && _note is Note){

            _editing = true



            _noteId = _note!!.id
            txtNoteAddress.text = _note!!.getAdress()
            etNoteMessage.setText(_note!!.message)
            txtNoteTime.text = Strings.getTimeStr1(_note!!.datetime)
            btnEditCreate.text = getString(R.string.str_editcreate_activity_finalize)

            when(_note?.visibility){
                Note.VISIBILITY_FOR_ALL -> {
                    radAll.isChecked = true
                }

                Note.VISIBILITY_FRIENDS -> {
                    radFriends.isChecked = true
                }

                Note.VISIBILITY_FOLLOWERS -> {
                    radFollowers.isChecked = true
                }



            }




        }


    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {
                finish()
            }


        }
        return super.onOptionsItemSelected(item)
    }

    companion object{
        private  const val TAG = "EditCreateNote"
    }
}