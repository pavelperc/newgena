package com.pavelperc.newgena.gui.model

import com.pavelperc.newgena.loaders.settings.JsonPetrinetSetup
import com.pavelperc.newgena.loaders.settings.JsonSettings
import tornadofx.*


class SettingsModel(jsonSettings: JsonSettings) : ItemViewModel<JsonSettings>(jsonSettings) {
    
    val outputFolder = bind(JsonSettings::outputFolder)
    val petrinetSetup = bind(JsonSettings::petrinetSetup)
    
    val numberOfLogs = bind(JsonSettings::numberOfLogs)
    val numberOfTraces = bind(JsonSettings::numberOfTraces)
    val maxNumberOfSteps = bind(JsonSettings::maxNumberOfSteps)
    
    val isRemovingEmptyTraces = bind(JsonSettings::isRemovingEmptyTraces)
    val isRemovingUnfinishedTraces = bind(JsonSettings::isRemovingUnfinishedTraces)
    
    val isUsingNoise = bind(JsonSettings::isUsingNoise)
    val noiseDescription = bind(JsonSettings::noiseDescription)
    
    val isUsingStaticPriorities = bind(JsonSettings::isUsingStaticPriorities)
    val staticPriorities = bind(JsonSettings::staticPriorities)
    
    val isUsingTime = bind(JsonSettings::isUsingTime)
    val timeDescription = bind(JsonSettings::timeDescription)
}

//class PetrinetSetupModel(petrinetSetup: JsonPetrinetSetup)