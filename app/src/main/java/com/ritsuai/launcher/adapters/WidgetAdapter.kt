package com.ritsuai.launcher.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ritsuai.launcher.databinding.ItemWidgetBinding
import com.ritsuai.launcher.models.WidgetInfo

/**
 * Adaptador para mostrar widgets en la pantalla principal.
 */
class WidgetAdapter(private val onWidgetClick: (WidgetInfo) -> Unit) : 
    ListAdapter<WidgetInfo, WidgetAdapter.WidgetViewHolder>(WidgetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
        val binding = ItemWidgetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WidgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        val widget = getItem(position)
        holder.bind(widget)
    }

    inner class WidgetViewHolder(private val binding: ItemWidgetBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val widget = getItem(position)
                    onWidgetClick(widget)
                }
            }
        }
        
        fun bind(widget: WidgetInfo) {
            binding.widgetTitle.text = widget.name
            
            // Configurar contenido específico según el tipo de widget
            when (widget.type) {
                "weather" -> {
                    setupWeatherWidget(widget)
                }
                "calendar" -> {
                    setupCalendarWidget(widget)
                }
                "music" -> {
                    setupMusicWidget(widget)
                }
                "notes" -> {
                    setupNotesWidget(widget)
                }
                else -> {
                    // Widget genérico
                    binding.widgetContent.text = "Widget ${widget.name}"
                }
            }
        }
        
        private fun setupWeatherWidget(widget: WidgetInfo) {
            // En una implementación real, se cargarían datos del clima
            binding.widgetContent.text = "23°C - Soleado"
            binding.widgetIcon.setImageResource(android.R.drawable.ic_menu_compass)
        }
        
        private fun setupCalendarWidget(widget: WidgetInfo) {
            // En una implementación real, se cargarían eventos del calendario
            binding.widgetContent.text = "Reunión a las 15:00"
            binding.widgetIcon.setImageResource(android.R.drawable.ic_menu_my_calendar)
        }
        
        private fun setupMusicWidget(widget: WidgetInfo) {
            // En una implementación real, se cargaría información del reproductor de música
            binding.widgetContent.text = "Reproduciendo: Canción actual"
            binding.widgetIcon.setImageResource(android.R.drawable.ic_media_play)
        }
        
        private fun setupNotesWidget(widget: WidgetInfo) {
            // En una implementación real, se cargarían notas del usuario
            binding.widgetContent.text = "Comprar leche y pan"
            binding.widgetIcon.setImageResource(android.R.drawable.ic_menu_edit)
        }
    }

    /**
     * DiffUtil para optimizar actualizaciones de la lista
     */
    class WidgetDiffCallback : DiffUtil.ItemCallback<WidgetInfo>() {
        override fun areItemsTheSame(oldItem: WidgetInfo, newItem: WidgetInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WidgetInfo, newItem: WidgetInfo): Boolean {
            return oldItem == newItem
        }
    }
}

