package com.pavelperc.newgena.gui.model

import com.pavelperc.newgena.loaders.settings.JsonSettings
import tornadofx.*

class SettingsModel(jsonSettings: JsonSettings) : ItemViewModel<JsonSettings>(jsonSettings) {
    
    val outputFolder = bind(JsonSettings::outputFolder)
    
    
}