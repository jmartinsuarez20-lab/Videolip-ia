package com.ritsuai.launcher.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ritsuai.launcher.databinding.ItemAppBinding
import com.ritsuai.launcher.models.AppInfo

/**
 * Adaptador para mostrar la lista de aplicaciones instaladas.
 *
 * @property onAppClick Callback para manejar clics en las aplicaciones
 */
class AppListAdapter(private val onAppClick: (AppInfo) -> Unit) : 
    ListAdapter<AppInfo, AppListAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = getItem(position)
        holder.bind(app)
    }

    inner class AppViewHolder(private val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAppClick(getItem(position))
                }
            }
        }
        
        fun bind(app: AppInfo) {
            binding.appName.text = app.appName
            binding.appPackage.text = app.packageName
            binding.appIcon.setImageDrawable(app.icon)
        }
    }

    /**
     * DiffUtil.Callback para optimizar actualizaciones de la lista
     */
    class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}

