package com.stretchscreen

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Data class representing a screen stretch preset
 */
data class Preset(
    val name: String,
    val widthRatio: Int,
    val heightRatio: Int,
    val isDefault: Boolean
) {
    companion object {
        /**
         * Converts a list of Presets to a JSON string
         */
        fun toJsonList(presets: List<Preset>): String {
            val jsonArray = JSONArray()
            for (preset in presets) {
                val jsonObject = JSONObject()
                jsonObject.put("name", preset.name)
                jsonObject.put("widthRatio", preset.widthRatio)
                jsonObject.put("heightRatio", preset.heightRatio)
                jsonObject.put("isDefault", preset.isDefault)
                jsonArray.put(jsonObject)
            }
            return jsonArray.toString()
        }

        /**
         * Creates a list of Presets from a JSON string
         */
        fun fromJsonList(jsonString: String): List<Preset> {
            val presets = ArrayList<Preset>()
            try {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val preset = Preset(
                        jsonObject.getString("name"),
                        jsonObject.getInt("widthRatio"),
                        jsonObject.getInt("heightRatio"),
                        jsonObject.getBoolean("isDefault")
                    )
                    presets.add(preset)
                }
            } catch (e: JSONException) {
                // Return empty list if there's an error parsing JSON
            }
            return presets
        }
    }
}
