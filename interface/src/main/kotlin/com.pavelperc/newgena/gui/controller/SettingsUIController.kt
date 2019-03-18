package com.pavelperc.newgena.gui.controller

import com.pavelperc.newgena.gui.model.PetrinetSetupModel
import com.pavelperc.newgena.gui.model.SettingsModel
import com.pavelperc.newgena.loaders.settings.JsonSettings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.BooleanProperty
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File

class SettingsUIController : Controller() {
    val settingsModel = SettingsModel(JsonSettings())
    val petrinetSetupModel = settingsModel.petrinetSetupModel
    val markingModel = petrinetSetupModel.markingModel
    
    val allModelsAreValid: BooleanBinding
    get() = settingsModel.valid
            .and(petrinetSetupModel.valid)
            .and(markingModel.valid)
    
    fun requestOutputFolderChooseDialog() {
        val cwd = File(System.getProperty("user.dir"))
        println(cwd.absolutePath)
    
        val directoryChooser = DirectoryChooser()
        directoryChooser.initialDirectory = cwd
    
        var path = directoryChooser.showDialog(null)?.path
        if (path != null) {
            if (path.startsWith(cwd.path))
                path = path.substringAfter(cwd.path + "\\")
        
            settingsModel.outputFolder.value = path
        }
    }
    
    fun requestPetrinetFileChooseDialog() {
        val cwd = File(System.getProperty("user.dir"))
        println(cwd.absolutePath)
        
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = cwd
        fileChooser.extensionFilters.add(
                FileChooser.ExtensionFilter("Petrinet file format", "*.pnml")
        )
        
        var path = fileChooser.showOpenDialog(null)?.path
        if (path != null) {
            if (path.startsWith(cwd.path))
                path = path.substringAfter(cwd.path + "\\")
            
            petrinetSetupModel.petrinetFile.value = path
        }
    }
    
}