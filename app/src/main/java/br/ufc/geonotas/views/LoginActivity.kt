package br.ufc.geonotas.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import br.ufc.geonotas.R
import br.ufc.geonotas.db.PlacesDAO
import br.ufc.geonotas.db.UsersDAO
import br.ufc.geonotas.models.User
import br.ufc.geonotas.utils.Constants

class LoginActivity : AppCompatActivity() {

    lateinit var txtSingup : TextView
    lateinit var btnLogin : Button
    lateinit var etLogin : EditText
    lateinit var etPass : EditText
    lateinit var txtLoginStatus : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if(Constants.TEST){
            PlacesDAO.initTest(this)
        }

        txtSingup = findViewById(R.id.txt_to_singup)
        btnLogin = findViewById(R.id.btn_login)
        etLogin = findViewById(R.id.et_login)
        etPass = findViewById(R.id.et_pass)
        txtLoginStatus = findViewById(R.id.txt_login_status)

        txtSingup.setOnClickListener {
            val intent = Intent(this, SingupActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val loginStr = etLogin.text.toString()
            val passStr = etPass.text.toString()

            if(login(loginStr, passStr)){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, resources.getText(R.string.txt_login_status_error0), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login(user : String, pass : String) : Boolean{


        User.loggedUser = UsersDAO.getUser(this, Constants.USER_NICK_SECTION)

        return true
    }
}