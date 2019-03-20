package com.pavelperc.newgena.gui.controller

import com.pavelperc.newgena.gui.model.PetrinetSetupModel
import com.pavelperc.newgena.gui.model.SettingsModel
import com.pavelperc.newgena.loaders.settings.JsonSettings
import com.pavelperc.newgena.loaders.settings.JsonSettingsController
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import tornadofx.*
import java.io.File

class SettingsUIController : Controller() {
    
    val jsonSettingsController = JsonSettingsController.createFromFilePath("examples/petrinet/simpleExample/settings.json")
    
    var jsonSettings: JsonSettings
        get() = jsonSettingsController.jsonSettings
        set(value) {
            jsonSettingsController.jsonSettings = value
        }
    
    val petrinet: ResetInhibitorNet?
        get() = jsonSettingsController.petrinet
    
    
    val isPetrinetUpdated = SimpleBooleanProperty(false)
    val isPetrinetDirty = isPetrinetUpdated.not()
    private var loadedPetrinetFilePath: String? = null
    
    
    // --- MODELS:
    val settingsModel = SettingsModel(jsonSettings)
    val petrinetSetupModel = settingsModel.petrinetSetupModel
    val markingModel = petrinetSetupModel.markingModel
    
    val allModelsAreValid: BooleanBinding
        get() = settingsModel.valid
                .and(petrinetSetupModel.valid)
                .and(markingModel.valid)
    
    
    init {
        petrinetSetupModel.petrinetFile.onChange { value ->
            if (loadedPetrinetFilePath == value) {
                isPetrinetUpdated.set(false)
            }
        }
    }
    
    fun requestOutputFolderChooseDialog() {
        val cwd = File(System.getProperty("user.dir"))
//        println(cwd.absolutePath)
        val prev = File(settingsModel.outputFolder.value)
        
        val directoryChooser = DirectoryChooser()
        directoryChooser.initialDirectory = if (prev.isDirectory) prev else cwd
        
        var path = directoryChooser.showDialog(null)?.path
        if (path != null) {
            if (path.startsWith(cwd.path))
                path = path.substringAfter(cwd.path + "\\")
            
            settingsModel.outputFolder.value = path
        }
    }
    
    /** Returns if the dialog was not cancelled. */
    fun requestPetrinetFileChooseDialog(): Boolean {
        val cwd = File(System.getProperty("user.dir"))
//        println(cwd.absolutePath)
        val prev = File(petrinetSetupModel.petrinetFile.value).parentFile
        
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = if (prev != null && prev.isDirectory) prev else cwd
        
        fileChooser.extensionFilters.add(
                FileChooser.ExtensionFilter("Petrinet file format", "*.pnml")
        )
        
        var path = fileChooser.showOpenDialog(null)?.path
        if (path != null) {
            if (path.startsWith(cwd.path))
                path = path.substringAfter(cwd.path + "\\")
            
            petrinetSetupModel.petrinetFile.value = path
            return true
        }
        return false
    }
    
    fun loadPetrinet() {
        jsonSettingsController.loadPetrinet()
        loadedPetrinetFilePath = jsonSettings.petrinetSetup.petrinetFile
        isPetrinetUpdated.set(true)
    }
    
}