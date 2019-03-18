package com.pavelperc.newgena.gui.controller

import com.pavelperc.newgena.gui.model.SettingsModel
import com.pavelperc.newgena.loaders.settings.JsonSettings
import javafx.stage.DirectoryChooser
import tornadofx.*
import java.io.File

class SettingsUIController : Controller() {
    val settingsModel = SettingsModel(JsonSettings())
    
    
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
    
}