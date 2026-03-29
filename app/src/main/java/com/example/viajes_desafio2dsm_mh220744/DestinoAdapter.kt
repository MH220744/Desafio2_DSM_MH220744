package com.example.viajes_desafio2dsm_mh220744

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class DestinoAdapter(
    private val lista: List<Destino>,
    private val onEditClick: (Destino) -> Unit,
    private val onDeleteClick: (Destino) -> Unit
) : RecyclerView.Adapter<DestinoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.Destino)
        val nombre: TextView = view.findViewById(R.id.NombreDestino)
        val pais: TextView = view.findViewById(R.id.Pais)
        val precio: TextView = view.findViewById(R.id.Precio)
        val descripcion: TextView = view.findViewById(R.id.Descripcion)
        val btnEdit: MaterialButton = view.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destino, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.nombre.text = item.nombre
        holder.pais.text = item.pais
        holder.precio.text = "$${item.precio}"
        holder.descripcion.text = item.descripcion

        if (item.imagenBase64.isNotEmpty()) {
            val bytes = Base64.decode(item.imagenBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            holder.imagen.setImageBitmap(bitmap)
        } else {
            holder.imagen.setImageResource(R.mipmap.ic_launcher)
        }

        holder.btnEdit.setOnClickListener { onEditClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }
}