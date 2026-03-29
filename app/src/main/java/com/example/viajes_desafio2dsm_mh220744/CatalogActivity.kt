package com.example.viajes_desafio2dsm_mh220744

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CatalogActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtEmpty: TextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var search: TextInputEditText

    private val listaDestinos = mutableListOf<Destino>()
    private val listaFiltrada = mutableListOf<Destino>()
    private lateinit var adapter: DestinoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.Destinos)
        txtEmpty = findViewById(R.id.Empty)
        btnLogout = findViewById(R.id.btnLogout)
        btnAdd = findViewById(R.id.fabAdd)
        search = findViewById(R.id.Search)

        adapter = DestinoAdapter(listaFiltrada,
            onEditClick = { destino ->
                val intent = Intent(this, AddEditDestinosActivity::class.java)
                intent.putExtra("modo", "editar")
                intent.putExtra("id", destino.id)
                intent.putExtra("nombre", destino.nombre)
                intent.putExtra("pais", destino.pais)
                intent.putExtra("precio", destino.precio)
                intent.putExtra("descripcion", destino.descripcion)
                intent.putExtra("imagenBase64", destino.imagenBase64)
                startActivity(intent)
            },
            onDeleteClick = { destino ->
                confirmarEliminacion(destino)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddEditDestinosActivity::class.java)
            intent.putExtra("modo", "crear")
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filtrar(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onResume() {
        super.onResume()
        obtenerDestinos()
    }

    private fun obtenerDestinos() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("destinos")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { result ->
                listaDestinos.clear()
                for (doc in result) {
                    val destino = doc.toObject(Destino::class.java)
                    destino.id = doc.id
                    listaDestinos.add(destino)
                }
                filtrar(search.text.toString())
            }
    }

    private fun filtrar(texto: String) {
        listaFiltrada.clear()

        if (texto.isBlank()) {
            listaFiltrada.addAll(listaDestinos)
        } else {
            listaFiltrada.addAll(
                listaDestinos.filter {
                    it.nombre.contains(texto, true) || it.pais.contains(texto, true)
                }
            )
        }

        adapter.notifyDataSetChanged()
        txtEmpty.visibility = if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun confirmarEliminacion(destino: Destino) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar destino")
            .setMessage("¿Deseas eliminar ${destino.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                db.collection("destinos").document(destino.id)
                    .delete()
                    .addOnSuccessListener { obtenerDestinos() }
            }
            .setNegativeButton("No", null)
            .show()
    }
}