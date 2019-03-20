package com.pavelperc.newgena.gui.controller

import com.pavelperc.newgena.gui.model.SettingsModel
import com.pavelperc.newgena.loaders.pnml.PnmlLoader
import com.pavelperc.newgena.loaders.settings.JsonSettings
import com.pavelperc.newgena.loaders.settings.JsonSettingsBuilder
import com.pavelperc.newgena.loaders.settings.fromFilePath
import com.pavelperc.newgena.models.deleteAllInhibitorResetArcs
import com.pavelperc.newgena.models.markInhResetArcsByIds
import guru.nidi.graphviz.engine.Graphviz
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.semantics.petrinet.Marking
import tornadofx.*
import java.io.File

class SettingsUIController : Controller() {

//    val jsonSettingsController = JsonSettingsController.createFromFilePath("examples/petrinet/simpleExample/settings.json")
    
    var jsonSettings = JsonSettings.fromFilePath("examples/petrinet/simpleExample/settings.json")
    
    var petrinet: ResetInhibitorNet? = null
        private set
    
    private var pnmlMarking: Marking = Marking()
    
    val markings: Pair<Marking, Marking>
        get() {
            markingModel.commit(true, false, markingModel.initialPlaceIds)
            markingModel.commit(true, false, markingModel.finalPlaceIds)
            
            val fromSettings = petrinet?.let { petrinet ->
                JsonSettingsBuilder(petrinet, jsonSettings).buildMarking()
            } ?: return Marking() to Marking()
            
            if (markingModel.isUsingInitialMarkingFromPnml.value)
                return pnmlMarking to fromSettings.second
            
            return fromSettings
        }
    
    // --- javafx properties:
    val isPetrinetUpdated = SimpleBooleanProperty(false)
    val isPetrinetDirty = isPetrinetUpdated.not()
    
    private var loadedPetrinetFilePath: String? = null
    
    
    val jsonSettingsPath = SimpleStringProperty(null)
    
    // --- MODELS:
    val settingsModel = SettingsModel(jsonSettings)
    val petrinetSetupModel = settingsModel.petrinetSetupModel
    val markingModel = petrinetSetupModel.markingModel
    
    val allModelsAreValid: BooleanBinding
        get() = settingsModel.valid
                .and(petrinetSetupModel.valid)
                .and(markingModel.valid)
    
    
    init {
        // grephviz: speedup first draw
        Graphviz.useDefaultEngines()
        
        petrinetSetupModel.petrinetFile.onChange { value ->
            isPetrinetUpdated.set(loadedPetrinetFilePath == value)
        }
    }
    
    fun requestOutputFolderChooseDialog() {
        val cwd = File(System.getProperty("user.dir"))
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
    
    /** @return if the dialog was not cancelled. */
    fun requestPetrinetFileChooseDialog(): Boolean {
        val cwd = File(System.getProperty("user.dir"))
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
        PnmlLoader.loadPetrinetWithOwnParser(petrinetSetupModel.petrinetFile.value).also { result ->
            petrinet = result.first
            pnmlMarking = result.second
        }
        
        loadedPetrinetFilePath = petrinetSetupModel.petrinetFile.value
        isPetrinetUpdated.set(true)
    }
    
    fun loadJsonSettings() {
        val cwd = File(System.getProperty("user.dir"))
        val prev = File(petrinetSetupModel.petrinetFile.value).parentFile
        
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = if (prev != null && prev.isDirectory) prev else cwd
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Settings in json", "*.json"))
        
        val file = fileChooser.showOpenDialog(null)
        
        
    }
    
    fun updateInhResetArcsFromModel() {
        petrinet?.also { petrinet ->
            val resetArcIds = petrinetSetupModel.resetArcIds.value.toList()
            val inhibitorArcIds = petrinetSetupModel.inhibitorArcIds.value.toList()
            
            // what if we fail after deleting?
            petrinet.deleteAllInhibitorResetArcs()
            petrinet.markInhResetArcsByIds(inhibitorArcIds, resetArcIds)
        } ?: IllegalStateException("Petrinet is not loaded.")
    }
    
}