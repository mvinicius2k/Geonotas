package br.ufc.geonotas.views

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ufc.geonotas.R
import br.ufc.geonotas.adapters.RvFeedPostAdapter
import br.ufc.geonotas.db.PlacesDAO
import br.ufc.geonotas.models.Note
import br.ufc.geonotas.utils.Constants



class MainActivity : AppCompatActivity() {

    lateinit var notes : ArrayList<Note>



    lateinit var rvFeedAdapter : RecyclerView.Adapter<*>
    lateinit var rvFeedManager: RecyclerView.LayoutManager
    lateinit var rvFeed: RecyclerView

    private var _actualAction = MAIN
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notes = ArrayList()

        rvFeed = findViewById(R.id.rv_main_feed)
        rvFeedManager = LinearLayoutManager(this);





        fillActivity()



        rvFeedAdapter = RvFeedPostAdapter(this,notes)

        rvFeed.apply {
            setHasFixedSize(false)
            layoutManager = rvFeedManager
            adapter = rvFeedAdapter
        }



    }

    private fun fillActivity(){

        notes.clear()

        when(intent.getIntExtra("action", MAIN) ){
            MAIN -> {
                notes.addAll(PlacesDAO.getLastPosts(this, Constants.USER_NICK_SECTION,5))

                supportActionBar?.title = getString(R.string.str_main_activity_title_main)
                _actualAction = MAIN
            }

            MY_NOTES -> {
                notes.addAll(PlacesDAO.getLastPostsByUser(this, Constants.USER_NICK_SECTION, 5))
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setHomeButtonEnabled(true)
                _actualAction = MY_NOTES
                supportActionBar?.title = getString(R.string.str_main_activity_title_mynotes)
            }
            MARKED -> {

                notes.addAll(PlacesDAO.getLastMarkedPosts(this,Constants.USER_NICK_SECTION, 5))
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setHomeButtonEnabled(true)
                _actualAction = MARKED
                supportActionBar?.title = getString(R.string.str_main_activity_title_marked)
            }


        }

        notes.toString()
    }


    fun openNote(position: Int){

        val intent = Intent(this, NoteActivity::class.java)
        intent.putExtra("note", notes[position])
        Log.d("TAG","Iniciando intent")
        startActivityForResult(intent, Constants.REQUEST_CODE_OPEN_NOTE)

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(_actualAction == MAIN)
            menuInflater.inflate(R.menu.menu_master, menu)


        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fillActivity()
        Log.d(TAG,"Reaparecendo na main")
        rvFeedAdapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.menu_master_add -> {
                val intent = Intent(this, EditCreateNoteActivity::class.java)
                intent.putExtra("title",getString(R.string.str_main_activity_title_addnote))
                startActivityForResult(intent, Constants.REQUEST_CODE_NEW_NOTE)
            }

            R.id.menu_master_my_notes -> {

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("action", MainActivity.MY_NOTES)
                startActivity(intent)


            }

            R.id.menu_master_marked_notes -> {

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("action", MainActivity.MARKED)
                startActivity(intent)


            }

            R.id.menu_master_map -> {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("notes",notes)
                startActivity(intent)

                
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

    companion object{
        const val MAIN = 0
        const val MY_NOTES = 1
        const val MARKED = 2

    }
}