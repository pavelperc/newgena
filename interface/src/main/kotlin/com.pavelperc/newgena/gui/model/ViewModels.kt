package com.pavelperc.newgena.gui.model

import com.pavelperc.newgena.loaders.settings.JsonSettings
import tornadofx.*


class SettingsModel(jsonSettings: JsonSettings) : ItemViewModel<JsonSettings>(jsonSettings) {
    
    val outputFolder = bind(JsonSettings::outputFolder)
    
    val numberOfLogs = bind(JsonSettings::numberOfLogs)
    val numberOfTraces = bind(JsonSettings::numberOfTraces)
    val maxNumberOfSteps = bind(JsonSettings::maxNumberOfSteps)
    
    val isRemovingEmptyTraces = bind(JsonSettings::isRemovingEmptyTraces)
    val isRemovingUnfinishedTraces = bind(JsonSettings::isRemovingUnfinishedTraces)
    
}