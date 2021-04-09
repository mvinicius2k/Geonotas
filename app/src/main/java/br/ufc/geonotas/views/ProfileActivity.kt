package br.ufc.geonotas.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.ufc.geonotas.R
import br.ufc.geonotas.adapters.RvMarkedListAdapter
import br.ufc.geonotas.db.UserDB
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.toastShow
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ProfileActivity : AppCompatActivity() {

    private lateinit var user: User

    private lateinit var ivAvatar: ImageView
    private lateinit var txtNick: TextView
    private lateinit var txtFullname: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtFriends: TextView
    private lateinit var txtFollowers: TextView
    private lateinit var rvFriends: RecyclerView
    private lateinit var rvManager: RecyclerView.LayoutManager
    private lateinit var rvAdapter: RecyclerView.Adapter<*>
    private lateinit var rvFollowers: RecyclerView
    private lateinit var rvFollowersManager: RecyclerView.LayoutManager
    private lateinit var rvFollowersAdapter: RecyclerView.Adapter<*>
    private lateinit var acAddFriends: AutoCompleteTextView
    private lateinit var ibAddFriends: ImageButton
    private lateinit var acAdapter: ArrayAdapter<String>

    private lateinit var usersList: LinkedList<String>
    private lateinit var friedsList: ArrayList<User>
    private lateinit var followersList: ArrayList<User>







    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        this.user = intent.getSerializableExtra("user") as User
        this.user.makeAvatarBitmap(this)

        if(intent != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.title = user.nick
        }

        this.ivAvatar = findViewById(R.id.iv_profile_avatar)
        this.txtNick = findViewById(R.id.txt_profile_nick)
        this.txtFullname = findViewById(R.id.txt_profile_fullname)
        this.txtEmail = findViewById(R.id.txt_profile_email)
        this.txtFriends = findViewById(R.id.txt_profile_friends)
        this.txtFollowers = findViewById(R.id.txt_profile_followers)
        this.acAddFriends = findViewById(R.id.ac_profile_addfriends)
        this.ibAddFriends = findViewById(R.id.ib_profile_addfriends)
        this.rvFriends = findViewById(R.id.rv_profile_friends)
        this.rvFollowers = findViewById(R.id.rv_profile_followers)

        this.followersList = ArrayList()


        this.friedsList = ArrayList()
        this.usersList = LinkedList()


        this.rvManager = LinearLayoutManager(this)
        this.rvAdapter = RvMarkedListAdapter(friedsList, this)
        this.acAdapter = ArrayAdapter(this,android.R.layout.simple_dropdown_item_1line, usersList)

        rvFriends.apply {
            setHasFixedSize(false)
            layoutManager = rvManager
            adapter = rvAdapter
        }

        this.rvFollowersManager = LinearLayoutManager(this)
        this.rvFollowersAdapter = RvMarkedListAdapter(followersList, this, false)
        this.rvFollowers.apply {
            setHasFixedSize(false)
            layoutManager = rvFollowersManager
            adapter = rvFollowersAdapter
        }

        acAddFriends.setAdapter(acAdapter)

        ivAvatar.setImageBitmap(user.avatar)
        txtNick.text = user.nick
        txtFullname.text = user.getFullname()
        txtEmail.text = user.email

        GlobalScope.launch {
            fillFriends()
            fillFollowers()

        }

        ibAddFriends.setOnClickListener {
            GlobalScope.launch {
                addFriend(acAddFriends.text.toString())
            }
        }

        acAddFriends.setOnKeyListener { v, keyCode, event ->

            Log.d(TAG, getString(R.string.str_teclado))

            GlobalScope.launch {
                fillAutoComplete()
            }

            return@setOnKeyListener true
        }

        acAddFriends.setOnItemClickListener { parent, view, position, id ->
            acAddFriends.setText(acAdapter.getItem(position))
        }

    }

    private suspend fun fillFollowers(){
        val userDB = UserDB()
        val deferred = GlobalScope.async(Dispatchers.IO){
            followersList.addAll(userDB.getFollowers(user.nick))

        }

        try {
            deferred.await()
        } catch (e: IOException){
            toastShow(this, e.message)
            return
        }

        val gettingAvatars = GlobalScope.async(Dispatchers.IO) {
            followersList.forEach {
                it.makeAvatarBitmap(this@ProfileActivity, userDB.getAvatar(it.nick))
            }

        }

        try {
            gettingAvatars.await()
        } catch (e: IOException){
            Log.d(TAG,"${e.message}, usando avatares padrão")
            followersList.forEach {
                it.makeAvatarBitmap(this)
            }
        }


        runOnUiThread {
            rvFollowersAdapter.notifyDataSetChanged()
            if(followersList.isNotEmpty())
                txtFollowers.text = getString(R.string.str_profile_followers)

        }

    }

    private suspend fun fillAutoComplete(){
        val nick = acAddFriends.text.toString()
        if(nick.length < 3)
            return
        val userDB = UserDB()

        var array: Array<User>? = null

        val deferred = GlobalScope.async(Dispatchers.IO) {

            array = userDB.getUsersStartsWith(nick)


        }

        try {
            deferred.await()
            var arrayStr = array?.map { it.nick }!!.filter { it !in user.friends }



            runOnUiThread {
                acAdapter.clear()
                acAdapter.addAll(arrayStr)
                acAdapter.notifyDataSetChanged()

            }

        } catch (e: IOException){
            toastShow(this, e.message)
        } catch (e: NullPointerException){
            Log.d(TAG, "Nenhum usuário corresponde a $nick")
        }
    }

    private suspend fun addFriend(nick: String){

        if(nick in user.friends){
            runOnUiThread {
                toastShow(this, "Você já está seguindo $nick")

            }
            return

        }

        var exist = false
        val userDB = UserDB()

        val undo = user.friends.toTypedArray()

        val verify = GlobalScope.async(Dispatchers.IO){
            exist = userDB.exists(nick)
        }

        try {
            verify.await()
        } catch (e:IOException){
            runOnUiThread {
                toastShow(this, "$nick não existe")
            }
        }

        if(!exist)
            return

        val deferred = GlobalScope.async (Dispatchers.IO) {
           user.friends.add(acAddFriends.text.toString())
           userDB.updateFriends(user.friends)


        }

        try {
            deferred.await()
            runOnUiThread {
                acAddFriends.text.clear()
                toastShow(this@ProfileActivity, "Você está agora seguindo $nick")
                this.txtFriends.text = getString(R.string.txt_profile_friends)

            }
        } catch (e: IOException){
            user.friends.clear()
            user.friends.addAll(undo)
            runOnUiThread {
                toastShow(this, e.message)
            }

        }

        fillFriends()



    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    private suspend fun fillFriends(){

        val userDB = UserDB()

        friedsList.clear()
        val deferred = GlobalScope.async(Dispatchers.IO) {
            friedsList.addAll(userDB.getUsers(user.friends.toTypedArray(), this@ProfileActivity))

        }

        try {
            deferred.await()

        } catch (e: IOException){
            toastShow(this, e.message)
            return
        }



        if(friedsList.isEmpty()){
            runOnUiThread {
                this.txtFriends.text = getString(R.string.str_profile_notfriends)
            }
        }

        runOnUiThread {

            rvAdapter.notifyDataSetChanged()
        }


    }

    suspend fun removeFriend(nick: String){

        val index = user.friends.indexOf(nick)

        val undo = user.friends.toList()

        user.friends.removeAt(index)

        val userDB = UserDB()

        val deferred = GlobalScope.async(Dispatchers.IO) {
            userDB.updateFriends(user.friends)
        }

        try {
            deferred.await()
            friedsList.removeAt(index)
            runOnUiThread {
                rvAdapter.notifyItemRemoved(index)
            }
        } catch (e: IOException){

            runOnUiThread {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }

            user.friends = undo as ArrayList<String>
            return
        }

    }

    companion object{
        const val TAG = "ProfileActivity"
    }
}