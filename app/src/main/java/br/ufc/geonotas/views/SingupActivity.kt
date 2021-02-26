package br.ufc.geonotas.views

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import br.ufc.geonotas.R
import br.ufc.geonotas.db.PlacesDAO
import br.ufc.geonotas.utils.Constants
import java.lang.Exception

class SingupActivity : AppCompatActivity() {


    lateinit var btnAvatar: Button
    lateinit var btnFinalize: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singup)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "Criando uma conta"

        btnAvatar = findViewById(R.id.btn_singup_select_image)
        btnFinalize = findViewById(R.id.btn_singup_ok)

        btnAvatar.setOnClickListener {
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_AVATAR)

        }

        btnFinalize.setOnClickListener {
            Toast.makeText(this, "Conta criada", Toast.LENGTH_SHORT).show()

            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.REQUEST_CODE_SELECT_AVATAR && resultCode == Activity.RESULT_OK) {
            val selectedFile = data?.data //URI

            btnAvatar.text = selectedFile.toString()

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
}