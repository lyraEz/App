package com.stretchscreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PresetAdapter(
    private val presets: List<Preset>,
    private val listener: PresetClickListener
) : RecyclerView.Adapter<PresetAdapter.PresetViewHolder>() {

    interface PresetClickListener {
        fun onPresetClick(preset: Preset)
        fun onPresetDelete(preset: Preset)
    }

    class PresetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val presetName: TextView = view.findViewById(R.id.preset_name)
        val presetDescription: TextView = view.findViewById(R.id.preset_description)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.preset_item, parent, false)
        return PresetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {
        val preset = presets[position]
        
        holder.presetName.text = preset.name
        holder.presetDescription.text = String.format(
            "%d%% width, %d%% height",
            preset.widthRatio,
            preset.heightRatio
        )
        
        // Hide delete button for default presets
        if (preset.isDefault) {
            holder.deleteButton.visibility = View.GONE
        } else {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                listener.onPresetDelete(preset)
            }
        }
        
        // Set click listener for the whole item
        holder.itemView.setOnClickListener {
            listener.onPresetClick(preset)
        }
    }

    override fun getItemCount() = presets.size
}
