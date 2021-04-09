package br.ufc.geonotas.views

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.addTextChangedListener
import br.ufc.geonotas.R
import br.ufc.geonotas.db.ResultCreateKind
import br.ufc.geonotas.db.UserDB
import br.ufc.geonotas.models.User
import br.ufc.geonotas.myPalette.Icon
import br.ufc.geonotas.utils.*
import kotlinx.coroutines.*
import java.io.FileNotFoundException
import java.io.IOException

class SingupActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etNick: EditText
    private lateinit var etMail: EditText
    private lateinit var etPass0: EditText
    private lateinit var etPass1: EditText
    private lateinit var icnAvatar: Icon
    private lateinit var btnAvatar: Button
    private lateinit var btnFinalize: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singup)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "Criando uma conta"

        btnAvatar = findViewById(R.id.btn_singup_select_image)
        btnFinalize = findViewById(R.id.btn_singup_ok)
        etName = findViewById(R.id.et_singup_name)
        etSurname = findViewById(R.id.et_singup_surname)
        etPass0 = findViewById(R.id.et_singup_pass)
        etPass1 = findViewById(R.id.et_singup_confirm_pass)
        icnAvatar = findViewById(R.id.icn_singup_avatar)
        etNick = findViewById(R.id.et_singup_nick)
        etMail = findViewById(R.id.et_singup_mail)


        etPass0.addTextChangedListener {
            verifyPassEquals()
        }

        etPass1.addTextChangedListener {
            verifyPassEquals()
        }


        btnAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

            startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_AVATAR)

        }

        btnFinalize.setOnClickListener {

            GlobalScope.launch {
                sendUser()
            }

        }
    }


    private suspend fun sendUser(){
        val incomplete = hasNullOrBlank(arrayListOf(
                etName.text,
                etSurname.text,
                etNick.text,
                etMail.text,
                etPass0.text,
                etPass1.text
        ))

        if(incomplete)
            runOnUiThread { Toast.makeText(this, getString(R.string.str_incomplete_fields_err), Toast.LENGTH_LONG).show() }

        else if(etPass0.text.toString() != etPass1.text.toString())
            runOnUiThread { Toast.makeText(this, getString(R.string.str_equal_pass_err), Toast.LENGTH_LONG).show()}
        else {
            val userDB = UserDB()
            var resultKind = ResultCreateKind.UNCREATED



            val createUser = GlobalScope.async(Dispatchers.IO){
                val imageSrc = userDB.sendAvatar(icnAvatar.drawable.toBitmap(), etNick.text.toString())
                val user = User(
                    nick = etNick.text.toString(),
                    email = etMail.text.toString(),
                    iconSrc = imageSrc,
                    name = etName.text.toString(),
                    surname = etSurname.text.toString(),
                    pass = etPass0.text.toString(),
                    friends = ArrayList()

                )

                resultKind = userDB.createUser(user)
            }

            try {
                createUser.await()
            } catch (e: IOException) {
                Log.d(TAG, "Sem conexão")
                e.printStackTrace()
                resultKind = ResultCreateKind.UNCREATED
            }

            if(resultKind == ResultCreateKind.CREATED){
                setResult(RESULT_CODE_ACCOUNT_CREATED)
                finish()
            }

        }

    }

    private fun verifyPassEquals(){
        if(isNullOrBlank(etPass0.text) || isNullOrBlank(etPass1.text)){
            etPass0.background.setTint(resolveColorAttr(this, android.R.attr.textColorPrimary))
            etPass1.background.setTint(resolveColorAttr(this, android.R.attr.textColorPrimary))
            return
        }

        else{
            if(etPass0.text.toString() != etPass1.text.toString()){
                etPass0.background.setTint(Color.RED)
                etPass1.background.setTint(Color.RED)
            } else {
                etPass0.background.setTint(resolveColorAttr(this, android.R.attr.textColorPrimary))
                etPass1.background.setTint(resolveColorAttr(this, android.R.attr.textColorPrimary))
            }
        }

    }

    private suspend fun resizeAvatar(uri: Uri): Bitmap?{
        var bitmap: Bitmap? = null
        withContext(Dispatchers.IO){

            try {
                bitmap = Image.fromUri(this@SingupActivity,uri)
                bitmap = Image.cropToSquare(bitmap!!)
            } catch (e: FileNotFoundException){
                runOnUiThread {
                    Toast.makeText(this@SingupActivity, getString(R.string.str_name_image_err), Toast.LENGTH_LONG).show()
                }
            } catch (e: IllegalArgumentException){
                runOnUiThread {
                    Toast.makeText(this@SingupActivity, getString(R.string.str_invalid_image_err), Toast.LENGTH_LONG).show()
                }
            }

        }

        return bitmap
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == Constants.REQUEST_CODE_SELECT_AVATAR && resultCode == Activity.RESULT_OK) {
            val selectedFile = data?.data //URI

            if(selectedFile == null){
                Toast.makeText(this, getString(R.string.str_name_image_err), Toast.LENGTH_LONG).show()
                return
            }

            btnAvatar.isEnabled = false
            GlobalScope.launch {
                val bitmap = resizeAvatar(selectedFile)


                runOnUiThread {
                    if(bitmap != null)
                        icnAvatar.setImageBitmap(bitmap)



                    btnAvatar.isEnabled = true
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
        val TAG = this::class.simpleName!!

        const val RESULT_CODE_ACCOUNT_CREATED = 1
        const val RESULT_CODE_ACCOUNT_NOT_CREATED = 2
        const val RESULT_CODE_CANCEL = 3
    }




}