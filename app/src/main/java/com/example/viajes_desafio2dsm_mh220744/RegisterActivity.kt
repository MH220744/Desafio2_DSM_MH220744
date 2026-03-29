package com.example.viajes_desafio2dsm_mh220744

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var name: TextInputEditText
    private lateinit var email: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var confirmPassword: TextInputEditText
    private lateinit var btnRegistrar: MaterialButton
    private lateinit var irLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        name = findViewById(R.id.Name)
        email = findViewById(R.id.Email)
        password = findViewById(R.id.Password)
        confirmPassword = findViewById(R.id.ConfirmPassword)
        btnRegistrar = findViewById(R.id.btnRegister)
        irLogin = findViewById(R.id.IRLogin)

        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }

        irLogin.setOnClickListener {
            finish()
        }
    }

    private fun registrarUsuario() {
        val nombre = name.text.toString().trim()
        val correo = email.text.toString().trim()
        val clave = password.text.toString().trim()
        val confirmar = confirmPassword.text.toString().trim()

        if (nombre.isEmpty()) {
            name.error = "Ingrese su nombre"
            name.requestFocus()
            return
        }

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

        if (confirmar.isEmpty()) {
            confirmPassword.error = "Confirme la contraseña"
            confirmPassword.requestFocus()
            return
        }

        if (clave != confirmar) {
            confirmPassword.error = "Las contraseñas no coinciden"
            confirmPassword.requestFocus()
            return
        }

        if (clave.length < 6) {
            password.error = "Mínimo 6 caracteres"
            password.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(correo, clave)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, CatalogActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.message ?: "Error al registrar",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}