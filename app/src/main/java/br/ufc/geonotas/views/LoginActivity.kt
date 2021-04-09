package br.ufc.geonotas.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import br.ufc.geonotas.R
import br.ufc.geonotas.db.ResultAuthKind
import br.ufc.geonotas.db.UserDB
import br.ufc.geonotas.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    lateinit var txtSingup : TextView
    lateinit var btnLogin : Button
    lateinit var etLogin : EditText
    lateinit var etPass : EditText
    lateinit var txtLoginStatus : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


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

            btnLogin.isEnabled = false;
            Log.d(THREAD_TAG, "Thread de login = ${Thread.currentThread().name}")
            GlobalScope.launch (Dispatchers.IO) {
                val user = login()
                Log.d(THREAD_TAG, "Thread de login = ${Thread.currentThread().name}")
                runOnUiThread {
                    btnLogin.isEnabled = true;
                }

                if(user != null){
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }



            }

        }
    }

    /**
     * Autentica o login (nickname e senha), caso a senha ou username esteja incorretos,
     * um Toast é exibido, o mesmo vale para algum erro; nesses casos null é retornado.
     * Em caso de sucesso, um objeto avatar é retornado já com avatar carregado em memória
     */
    private suspend fun login(): User? {

        val loginStr = etLogin.text.toString()
        val passStr = etPass.text.toString()

        val userDB = UserDB()
        var user: User? = null
        var resultPair: Pair<ResultAuthKind, User?>? = null


        //Autenticando login
        val loginDeferred = GlobalScope.async(Dispatchers.IO) {
            Log.d(THREAD_TAG,"Thread requisição de login = ${Thread.currentThread().name}")
            resultPair = userDB.login(loginStr, passStr)


        }

        try {
            loginDeferred.await()
        } catch (e: IOException) {
            runOnUiThread {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }

            return null
        }


        when (resultPair?.first) {
            ResultAuthKind.AUTH_FAILED -> {
                runOnUiThread {
                    Toast.makeText(this, "Usuário ou senha incorretos", Toast.LENGTH_LONG).show()
                }
                return null
            }

            ResultAuthKind.CANCEL -> {
                Log.d(TAG,"Login cancelado")
                return null
            }

            ResultAuthKind.AUTH_SUCESS -> {
                Log.d(TAG, "Login feito, olá ${user?.nick}")
                user = resultPair?.second
            }
        }


        //Obetendo avatar
        val avatarDeferred = GlobalScope.async(Dispatchers.IO){
            val bitmap = userDB.getAvatar(user?.nick!!)
            user.makeAvatarBitmap(this@LoginActivity, bitmap)

        }

        try {
            avatarDeferred.await()
        } catch (e: IOException){
            Log.d(TAG, "Falha de conexão ao obter o avatar, usando padrão")
            e.printStackTrace()

            user?.makeAvatarBitmap(this)

        }


        User.loggedUser = user

        return user
    }

    companion object{
        private const val TAG = "LoginActivity"
        private const val THREAD_TAG = "Coroutine thread"
    }
}