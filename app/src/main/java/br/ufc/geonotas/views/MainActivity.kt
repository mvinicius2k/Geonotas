package br.ufc.geonotas.views

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ufc.geonotas.R
import br.ufc.geonotas.adapters.RvFeedPostAdapter
import br.ufc.geonotas.db.NoteDB
import br.ufc.geonotas.db.UserDB
import br.ufc.geonotas.models.Note
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.Constants
import br.ufc.geonotas.utils.Strings
import br.ufc.geonotas.utils.toastShow
import br.ufc.geonotas.utils.updateUserCoordinates
import kotlinx.coroutines.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.lang.Exception
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.LinkedHashMap





class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    lateinit var notes : LinkedList<Note>



    lateinit var rvFeedAdapter : RecyclerView.Adapter<*>
    lateinit var rvFeedManager: RecyclerView.LayoutManager
    lateinit var rvFeed: RecyclerView

    private var _actualAction = MAIN


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this._actualAction = intent.getIntExtra("action", MAIN)


        notes = LinkedList()

        rvFeed = findViewById(R.id.rv_main_feed)
        rvFeedManager = LinearLayoutManager(this);









        rvFeedAdapter = RvFeedPostAdapter(this,notes)

        rvFeed.apply {
            setHasFixedSize(false)
            layoutManager = rvFeedManager
            adapter = rvFeedAdapter
        }

        GlobalScope.launch {
            fillActivity()
        }



    }



    private suspend fun fillActivity(){

        fun updateUI(){
            when(_actualAction){
                MAIN -> {

                    supportActionBar?.title = getString(R.string.str_main_activity_title_main)
                }

                MY_NOTES -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.setHomeButtonEnabled(true)
                    supportActionBar?.title = getString(R.string.str_main_activity_title_mynotes)
                }
                MARKED -> {

                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.setHomeButtonEnabled(true)
                    supportActionBar?.title = getString(R.string.str_main_activity_title_marked)
                }

                FRIENDS -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.setHomeButtonEnabled(true)
                    supportActionBar?.title = "Notas de amigos"
                }


            }

            rvFeedAdapter.notifyDataSetChanged()
        }


        val noteDB = NoteDB()
        val userDB = UserDB()




        var notesArray: Array<Note>? = null
        val deferred = GlobalScope.async(Dispatchers.IO){
            notesArray = noteDB.getNotes(User.loggedUser!!, _actualAction)

        }

        try {
            deferred.await()
        } catch (e: IOException){
            toastShow(this, e.message)
            return
        }

        val avatarsToGet = HashSet<String>()

        avatarsToGet.addAll(notesArray?.map { it.op }!!)

        val avatars = LinkedHashMap<String, Bitmap?>()
        val gettingAvatars = GlobalScope.async(Dispatchers.IO){
            avatarsToGet.forEach {
                avatars[it] = userDB.getAvatar(it)
            }
        }

        try {
            gettingAvatars.await()
        } catch (e: IOException){
            toastShow(this, e.message)
            return
        }

        notesArray!!.forEach {
            it.makeAvatarBitmap(this, avatars[it.op])
            try {
                kotlin.runCatching {
                    it.address = Strings.makeLocationStrings(this, it.latitude!!,it.longitude!!)
                }
            } catch (e: IOException){
                it.address = ""
            } catch (e: IllegalAccessException){
                it.address = ""
            }
        }




        notes.clear()
        notes.addAll(notesArray!!)

        runOnUiThread {
            updateUI()
        }




        notes.toString()

        }



    private suspend fun fillLoggedUser(){
        val userDB = UserDB()
        val bitmap = User.loggedUser?.avatar
        val deferred = GlobalScope.async (Dispatchers.IO){
            User.loggedUser = userDB.getUser(User.loggedUser?.nick!!)

        }

        try {
            deferred.await()
        } catch (e: IOException){
            toastShow(this, e.message)
        }

        if(bitmap != null)
            User.loggedUser?.makeAvatarBitmap(this,bitmap)
    }


    fun openNote(position: Int){

        val intent = Intent(this, NoteActivity::class.java)
        intent.putExtra("note", notes[position])
        Log.d("TAG","Iniciando intent")
        startActivityForResult(intent, Constants.REQUEST_CODE_OPEN_NOTE)

    }

    override fun onResume() {
        super.onResume()
        try {
            GlobalScope.launch(Dispatchers.IO) {
                updateUserCoordinates(this@MainActivity)
            }
        } catch (e: Exception){
            toastShow(this, e.message)
        }


    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(_actualAction == MAIN)
            menuInflater.inflate(R.menu.menu_master, menu)


        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        GlobalScope.launch(Dispatchers.IO) {
            runBlocking {
                fillLoggedUser()
            }

            fillActivity()
        }




    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){


            R.id.menu_master_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("user", User.loggedUser!!)
                startActivityForResult(intent, Constants.REQUEST_CODE_PROFILE)
            }

            R.id.menu_master_add -> {
                val intent = Intent(this, EditCreateNoteActivity::class.java)
                intent.putExtra("title",getString(R.string.str_main_activity_title_addnote))
                startActivityForResult(intent, Constants.REQUEST_CODE_NEW_NOTE)
            }

            R.id.menu_master_my_notes -> {

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("action", MainActivity.MY_NOTES)
                startActivityForResult(intent, MainActivity.MY_NOTES)


            }

            R.id.menu_master_marked_notes -> {

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("action", MainActivity.MARKED)
                startActivityForResult(intent, MARKED)


            }

            R.id.menu_master_map -> {


                val intent = Intent(this, MapActivity::class.java)
                startActivityForResult(intent, MAP)


            }

            R.id.menu_master_preferences -> {

            }

            R.id.menu_master_logout -> {
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("logout", true)
                startActivity(intent)
                finish()
            }

            android.R.id.home ->{
                finish()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    @AfterPermissionGranted(RC_LOCATION)
    private fun methodRequiresTwoPermission() {
        val perms =
            arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                this, getString(R.string.global_request_permission_loc),
                RC_LOCATION, *perms
            )
        }
    }



    companion object{
        const val MAIN = 0
        const val MY_NOTES = 1
        const val MARKED = 2
        const val FRIENDS = 3
        const val MAP = 4
        private const val TAG = "MainActivity"
        const val RC_LOCATION = 123

    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            AppSettingsDialog.Builder(this).build().show()
        }
    }
}