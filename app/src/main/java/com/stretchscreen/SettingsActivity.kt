package com.stretchscreen

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SettingsActivity : AppCompatActivity() {

    private lateinit var widthRatioSeekBar: SeekBar
    private lateinit var heightRatioSeekBar: SeekBar
    private lateinit var widthRatioText: TextView
    private lateinit var heightRatioText: TextView
    private lateinit var savePresetButton: Button
    private lateinit var presetRecyclerView: RecyclerView
    private lateinit var presetAdapter: PresetAdapter
    private lateinit var presets: ArrayList<Preset>
    private lateinit var sharedPreferences: SharedPreferences

    private var currentWidthRatio = 100
    private var currentHeightRatio = 100

    companion object {
        const val PREF_NAME = "StretchScreenPrefs"
        const val KEY_WIDTH_RATIO = "width_ratio"
        const val KEY_HEIGHT_RATIO = "height_ratio"
        const val KEY_PRESETS = "presets"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)

        // Initialize UI components
        widthRatioSeekBar = findViewById(R.id.width_ratio_seekbar)
        heightRatioSeekBar = findViewById(R.id.height_ratio_seekbar)
        widthRatioText = findViewById(R.id.width_ratio_text)
        heightRatioText = findViewById(R.id.height_ratio_text)
        savePresetButton = findViewById(R.id.save_preset_button)
        presetRecyclerView = findViewById(R.id.preset_recycler_view)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        
        // Load saved settings
        currentWidthRatio = sharedPreferences.getInt(KEY_WIDTH_RATIO, 100)
        currentHeightRatio = sharedPreferences.getInt(KEY_HEIGHT_RATIO, 100)
        
        // Initialize seek bars with saved values
        widthRatioSeekBar.progress = currentWidthRatio - 50  // Range: 50-150
        heightRatioSeekBar.progress = currentHeightRatio - 50  // Range: 50-150
        updateRatioText()

        // Load presets
        loadPresets()
        setupPresetRecyclerView()

        // Set up seek bar listeners
        widthRatioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentWidthRatio = progress + 50  // Convert from 0-100 to 50-150 range
                updateRatioText()
                saveCurrentSettings()
                if (DisplayService.isRunning) {
                    // Update display service with new settings
                    DisplayService.updateRatio(currentWidthRatio, currentHeightRatio)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        heightRatioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentHeightRatio = progress + 50  // Convert from 0-100 to 50-150 range
                updateRatioText()
                saveCurrentSettings()
                if (DisplayService.isRunning) {
                    // Update display service with new settings
                    DisplayService.updateRatio(currentWidthRatio, currentHeightRatio)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Save preset button click
        savePresetButton.setOnClickListener {
            showSavePresetDialog()
        }
    }

    private fun loadPresets() {
        presets = ArrayList()
        
        // Add default presets first
        presets.add(Preset("4:3 Stretched", 133, 100, true))
        presets.add(Preset("16:10 Stretched", 120, 100, true))
        presets.add(Preset("5:4 Stretched", 125, 100, true))
        
        // Load custom presets from SharedPreferences
        val presetJson = sharedPreferences.getString(KEY_PRESETS, "")
        if (!presetJson.isNullOrEmpty()) {
            try {
                val customPresets = Preset.fromJsonList(presetJson)
                presets.addAll(customPresets)
            } catch (e: Exception) {
                Toast.makeText(this, "Error loading presets", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPresetRecyclerView() {
        presetRecyclerView.layoutManager = LinearLayoutManager(this)
        presetAdapter = PresetAdapter(presets, object : PresetAdapter.PresetClickListener {
            override fun onPresetClick(preset: Preset) {
                applyPreset(preset)
            }

            override fun onPresetDelete(preset: Preset) {
                if (!preset.isDefault) {
                    deletePreset(preset)
                } else {
                    Toast.makeText(
                        this@SettingsActivity,
                        "Cannot delete default presets",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
        presetRecyclerView.adapter = presetAdapter
    }

    private fun updateRatioText() {
        val widthPercent = currentWidthRatio
        val heightPercent = currentHeightRatio
        widthRatioText.text = getString(R.string.width_ratio_format, widthPercent)
        heightRatioText.text = getString(R.string.height_ratio_format, heightPercent)
    }

    private fun saveCurrentSettings() {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_WIDTH_RATIO, currentWidthRatio)
        editor.putInt(KEY_HEIGHT_RATIO, currentHeightRatio)
        editor.apply()
    }

    private fun showSavePresetDialog() {
        val editText = EditText(this)
        editText.hint = "Preset Name"

        AlertDialog.Builder(this)
            .setTitle("Save Preset")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val presetName = editText.text.toString().trim()
                if (presetName.isNotEmpty()) {
                    savePreset(presetName)
                } else {
                    Toast.makeText(this, "Preset name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun savePreset(name: String) {
        val newPreset = Preset(name, currentWidthRatio, currentHeightRatio, false)
        
        // Check if a custom preset with this name already exists
        val existingIndex = presets.indexOfFirst { 
            !it.isDefault && it.name.equals(name, ignoreCase = true) 
        }
        
        if (existingIndex >= 0) {
            // Replace existing preset
            presets[existingIndex] = newPreset
            Toast.makeText(this, "Updated preset: $name", Toast.LENGTH_SHORT).show()
        } else {
            // Add new preset
            presets.add(newPreset)
            Toast.makeText(this, "Saved preset: $name", Toast.LENGTH_SHORT).show()
        }
        
        savePresetsToPreferences()
        presetAdapter.notifyDataSetChanged()
    }

    private fun deletePreset(preset: Preset) {
        AlertDialog.Builder(this)
            .setTitle("Delete Preset")
            .setMessage("Are you sure you want to delete the preset '${preset.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                presets.remove(preset)
                savePresetsToPreferences()
                presetAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Preset deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyPreset(preset: Preset) {
        currentWidthRatio = preset.widthRatio
        currentHeightRatio = preset.heightRatio
        
        // Update UI
        widthRatioSeekBar.progress = currentWidthRatio - 50
        heightRatioSeekBar.progress = currentHeightRatio - 50
        updateRatioText()
        
        // Save settings
        saveCurrentSettings()
        
        // Update service if running
        if (DisplayService.isRunning) {
            DisplayService.updateRatio(currentWidthRatio, currentHeightRatio)
        }
        
        Toast.makeText(this, "Applied preset: ${preset.name}", Toast.LENGTH_SHORT).show()
    }

    private fun savePresetsToPreferences() {
        val customPresets = presets.filter { !it.isDefault }
        val presetJson = Preset.toJsonList(customPresets)
        
        val editor = sharedPreferences.edit()
        editor.putString(KEY_PRESETS, presetJson)
        editor.apply()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
