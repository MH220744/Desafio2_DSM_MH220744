package com.example.viajes_desafio2dsm_mh220744

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var email: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var register: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        email = findViewById(R.id.Email)
        password = findViewById(R.id.Password)
        btnLogin = findViewById(R.id.btnLogin)
        register = findViewById(R.id.Register)

        btnLogin.setOnClickListener {
            iniciarSesion()
        }

        register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startActivity(Intent(this, CatalogActivity::class.java))
            finish()
        }
    }

    private fun iniciarSesion() {
        val correo = email.text.toString().trim()
        val clave = password.text.toString().trim()

        if (correo.isEmpty()) {
            email.error = "Ingrese el correo"
            email.requestFocus()
            return
        }

        if (clave.isEmpty()) {
            password.error = "Ingrese la contraseña"
            password.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(correo, clave)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, CatalogActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.message ?: "Error al iniciar sesión",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}