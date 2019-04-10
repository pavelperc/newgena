package com.pavelperc.newgena.gui.controller

import tornadofx.*

class PreferencesController : Controller() {
    
    fun saveLastSettingsPath(settingsPath: String?) {
        config.put("last_settings", settingsPath ?: "null")
        config.save()
        println("saved $settingsPath as last settings path.")
    }
    
    fun loadLastSettingsPath(): String? {
        val path = config.get("last_settings") as String?
        println("restored last settings from path $path.")
        if (path == "null") return null
        return path
    }
}