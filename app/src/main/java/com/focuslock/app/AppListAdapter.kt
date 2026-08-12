package com.focuslock.app

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.focuslock.app.databinding.ItemAppBinding

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable
)

class AppListAdapter(
    private val apps: List<AppInfo>,
    private val selected: MutableSet<String>
) : RecyclerView.Adapter<AppListAdapter.VH>() {

    inner class VH(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = apps[position]
        holder.binding.ivIcon.setImageDrawable(app.icon)
        holder.binding.tvLabel.text = app.label
        holder.binding.checkbox.setOnCheckedChangeListener(null)
        holder.binding.checkbox.isChecked = selected.contains(app.packageName)
        holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selected.add(app.packageName) else selected.remove(app.packageName)
        }
        holder.binding.root.setOnClickListener {
            holder.binding.checkbox.isChecked = !holder.binding.checkbox.isChecked
        }
    }

    override fun getItemCount(): Int = apps.size
}
