package com.example.viajes_desafio2dsm_mh220744

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class AddEditDestinosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var btnBack: ImageButton
    private lateinit var titulo: TextView
    private lateinit var nombreDestino: TextInputEditText
    private lateinit var pais: Spinner
    private lateinit var precio: TextInputEditText
    private lateinit var descripcion: TextInputEditText
    private lateinit var preview: ImageView
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var btnSave: MaterialButton

    private var imagenBase64: String = ""
    private var modo: String = "crear"
    private var idDestino: String = ""

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    preview.setImageURI(imageUri)
                    imagenBase64 = convertirImagenABase64(imageUri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_destinos)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        btnBack = findViewById(R.id.btnBack)
        titulo = findViewById(R.id.Titulo)
        nombreDestino = findViewById(R.id.NombreDestino)
        pais = findViewById(R.id.Pais)
        precio = findViewById(R.id.Precio)
        descripcion = findViewById(R.id.Descripcion)
        preview = findViewById(R.id.Preview)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSave = findViewById(R.id.btnSave)

        configurarSpinner()

        modo = intent.getStringExtra("modo") ?: "crear"

        if (modo == "editar") {
            titulo.text = "Editar destino"
            btnSave.text = "Actualizar"
            cargarDatosEdicion()
        }

        btnBack.setOnClickListener { finish() }

        btnSelectImage.setOnClickListener {
            seleccionarImagen()
        }

        btnSave.setOnClickListener {
            guardarDestino()
        }
    }

    private fun configurarSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.countries_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        pais.adapter = adapter
    }

    private fun cargarDatosEdicion() {
        idDestino = intent.getStringExtra("id") ?: ""
        nombreDestino.setText(intent.getStringExtra("nombre") ?: "")
        precio.setText((intent.getDoubleExtra("precio", 0.0)).toString())
        descripcion.setText(intent.getStringExtra("descripcion") ?: "")
        imagenBase64 = intent.getStringExtra("imagenBase64") ?: ""

        val paisActual = intent.getStringExtra("pais") ?: ""
        val adapter = pais.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == paisActual) {
                pais.setSelection(i)
                break
            }
        }

        if (imagenBase64.isNotEmpty()) {
            val bytes = Base64.decode(imagenBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            preview.setImageBitmap(bitmap)
        }
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePicker.launch(intent)
    }

    private fun guardarDestino() {
        val nombre = nombreDestino.text.toString().trim()
        val paisSeleccionado = pais.selectedItem.toString()
        val precioTexto = precio.text.toString().trim()
        val descripcionTexto = descripcion.text.toString().trim()
        val uid = auth.currentUser?.uid ?: ""

        if (nombre.isEmpty()) {
            nombreDestino.error = "Ingrese el nombre del destino"
            nombreDestino.requestFocus()
            return
        }

        if (paisSeleccionado == "Seleccione un país") {
            Toast.makeText(this, "Seleccione un país", Toast.LENGTH_SHORT).show()
            return
        }

        if (precioTexto.isEmpty()) {
            precio.error = "Ingrese el precio"
            precio.requestFocus()
            return
        }

        val precioValor = precioTexto.toDoubleOrNull()
        if (precioValor == null || precioValor <= 0) {
            precio.error = "El precio debe ser mayor a 0"
            precio.requestFocus()
            return
        }

        if (descripcionTexto.isEmpty()) {
            descripcion.error = "Ingrese la descripción"
            descripcion.requestFocus()
            return
        }

        if (descripcionTexto.length < 20) {
            descripcion.error = "Mínimo 20 caracteres"
            descripcion.requestFocus()
            return
        }

        if (imagenBase64.isEmpty()) {
            Toast.makeText(this, "Seleccione una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        val destino = hashMapOf(
            "nombre" to nombre,
            "pais" to paisSeleccionado,
            "precio" to precioValor,
            "descripcion" to descripcionTexto,
            "imagenBase64" to imagenBase64,
            "userId" to uid
        )

        if (modo == "editar" && idDestino.isNotEmpty()) {
            db.collection("destinos").document(idDestino)
                .update(destino as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Destino actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            db.collection("destinos")
                .add(destino)
                .addOnSuccessListener {
                    Toast.makeText(this, "Destino guardado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun convertirImagenABase64(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        val maxWidth = 800
        val maxHeight = 800

        val ratio = minOf(
            maxWidth.toFloat() / originalBitmap.width,
            maxHeight.toFloat() / originalBitmap.height
        )

        val width = (originalBitmap.width * ratio).toInt()
        val height = (originalBitmap.height * ratio).toInt()

        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)

        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}