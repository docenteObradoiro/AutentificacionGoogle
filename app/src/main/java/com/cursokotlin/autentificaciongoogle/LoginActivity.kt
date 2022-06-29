package com.cursokotlin.autentificaciongoogle

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.Date
import kotlin.properties.Delegates
import java.text.SimpleDateFormat
import androidx.core.widget.*

class LoginActivity : AppCompatActivity() {

    companion object{
        lateinit var useremail: String
        lateinit var providerSession: String
    }

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var lyTerms: LinearLayout

    private lateinit var login: Button

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        lyTerms = findViewById(R.id.linearTerms)
        lyTerms.visibility = View.INVISIBLE

        etEmail = findViewById(R.id.editTextEmail)
        etPassword = findViewById(R.id.editTextPassword)

        mAuth = FirebaseAuth.getInstance()

        login = findViewById(R.id.login)
        login.setOnClickListener{
            login(login)
        }

        manageButtonLogin()
        etEmail.doOnTextChanged{text, start, before, count -> manageButtonLogin() }
        etPassword.doOnTextChanged{text, start, before, count -> manageButtonLogin() }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null) goHome(currentUser.email.toString(), currentUser.providerId)
    }

    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    private fun manageButtonLogin(){
        login = findViewById(R.id.login)
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        if(TextUtils.isEmpty(password) || !ValidateEmail.isEmail(email)){
            login.setBackgroundColor(ContextCompat.getColor(this, R.color.grey))
            login.isEnabled = false
        }else{
            login.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            login.isEnabled = true
        }
    }

    fun login(view: View) {
        loginUser()
    }

    private fun loginUser() {

        email = etEmail.text.toString()
        password = etPassword.text.toString()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) goHome(email, "email") //Si se cumple esta tarea accedemos a la página pincipal
                else {
                    if (lyTerms.visibility == View.INVISIBLE) {
                        lyTerms.visibility = View.VISIBLE
                    } else {
                        var chAccept = findViewById<CheckBox>(R.id.checkBox)
                        if (chAccept.isChecked) register() //Si se marca el checkBox accede a la función registro
                    }
                }
            }
    }

    private fun goHome(email: String, provider: String){

        useremail = email
        providerSession = provider

        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }



    private fun register(){

        email = etEmail.text.toString()
        password = etPassword.text.toString()

        mAuth.createUserWithEmailAndPassword(email, password) //Creación de un nuevo usuario
            .addOnCompleteListener(this) {
                if (it.isSuccessful){

                    var dateRegister = SimpleDateFormat("dd/MM/yyyy").format(java.util.Date())

                    var dbRegister = FirebaseFirestore.getInstance() //Vincular con la bd FireStore

                    dbRegister.collection("Users").document(email).set(hashMapOf( //Añadir un documento a la coleccion users de la base de datos
                        "user" to email, //correo usuario
                        "dateRegister" to dateRegister)) //fecha de registro

                    goHome(email, "email")
                }
                else Toast.makeText(this, "Error, algo foi mal", Toast.LENGTH_SHORT).show()
            }
    }

    fun goTerms(view: View){
        val intent = Intent(this, TermsActivity::class.java)
        startActivity(intent)
    }

    fun forgotPassword(view: View){
        //startActivity(Intent(this, ForgotPasswordActivity::class.java))
        var e = etEmail.text.toString()

        if(!TextUtils.isEmpty(e)){
            mAuth.sendPasswordResetEmail(e)
                .addOnCompleteListener{ task ->
                    if(task.isSuccessful) Toast.makeText(this, "Email enviado a $e", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "El usuario con este correo no se ha encontrado", Toast.LENGTH_SHORT).show()
                }
        }
        else Toast.makeText(this, "Introduce un email", Toast.LENGTH_SHORT).show()
    }

}