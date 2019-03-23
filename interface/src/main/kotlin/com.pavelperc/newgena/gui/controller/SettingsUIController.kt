package com.pavelperc.newgena.gui.controller

import com.pavelperc.newgena.gui.model.SettingsModel
import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.loaders.pnml.PnmlLoader
import com.pavelperc.newgena.loaders.settings.JsonSettings
import com.pavelperc.newgena.loaders.settings.JsonSettingsBuilder
import com.pavelperc.newgena.loaders.settings.fromFilePath
import com.pavelperc.newgena.loaders.settings.toJson
import com.pavelperc.newgena.models.deleteAllInhibitorResetArcs
import com.pavelperc.newgena.models.markInhResetArcsByIds
import com.pavelperc.newgena.models.pnmlId
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
    
    val jsonSettings: JsonSettings
        get() = settingsModel.item!!
    
    var petrinet: ResetInhibitorNet? = null
        private set(value) {
            placeIds = value?.places?.map { it.pnmlId }?.toSet() ?: emptySet()
            inputEdgeIds = value?.transitions?.flatMap { value.getInEdges(it) }?.map { it.pnmlId }?.toSet() ?: emptySet()
            field = value
        }
    
    /** Place pnml ids of loaded petrinet, or empty set */
    var placeIds: Set<String> = emptySet()
        private set
    
    /** Arc pnml ids of loaded petrinet, or empty set */
    var inputEdgeIds: Set<String> = emptySet()
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
    val settingsModel = SettingsModel(JsonSettings()) // start from default jsonSettings.
    val petrinetSetupModel = settingsModel.petrinetSetupModel
    val markingModel = petrinetSetupModel.markingModel
    
    val allModelsAreValid: BooleanBinding
        get() = settingsModel.valid
                .and(petrinetSetupModel.valid)
                .and(markingModel.valid)
    
    val someModelIsDirty: BooleanBinding
        get() = settingsModel.dirty
                .or(petrinetSetupModel.dirty)
                .or(markingModel.dirty)
    
    
    init {
        // grephviz: speedup first draw
//        Graphviz.useDefaultEngines()
        
        petrinetSetupModel.petrinetFile.onChange { value ->
            isPetrinetUpdated.set(loadedPetrinetFilePath == value)
        }

        loadJsonSettingsFromPath("examples/petrinet/simpleExample/settings.json")
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
    
    
    /** Almost relative path.. Works only for inner folders. */
    private val File.relativePath: String
        get() {
            val cwd = File(System.getProperty("user.dir"))
            val absPath = absolutePath
            
            return if (absPath.startsWith(cwd.path + "\\"))
                absPath.substringAfter(cwd.path + "\\")
            else
                absPath
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
        
        val path = fileChooser.showOpenDialog(null)?.relativePath
        
        if (path != null) {
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
        val prev = File(jsonSettingsPath.value ?: "").parentFile
        
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = if (prev != null && prev.isDirectory) prev else cwd
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Settings in json", "*.json"))
        
        val path = fileChooser.showOpenDialog(null)?.relativePath
        
        if (path != null) {
            loadJsonSettingsFromPath(path)
        }
    }
    
    fun loadJsonSettingsFromPath(path: String) {
        settingsModel.itemProperty.value = JsonSettings.fromFilePath(path)
        
        jsonSettingsPath.value = path
        
    }
    
    /** @return if the dialog was not cancelled. */
    fun saveJsonSettingsAs(): Boolean {
        val cwd = File(System.getProperty("user.dir"))
        val prev = File(jsonSettingsPath.value ?: "").parentFile
        
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = if (prev != null && prev.isDirectory) prev else cwd
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Settings in json", "*.json"))
        
        val path = fileChooser.showSaveDialog(null)?.relativePath
        
        if (path != null) {
            jsonSettingsPath.value = path
            return saveJsonSettings(path)
        }
        // when canceled
        return false
    }
    
    fun saveJsonSettings(path: String): Boolean {
        if (!settingsModel.commit())
            throw IllegalStateException("Can not save. Model is not valid.")

//        petrinetSetupModel.commit(force = true)
//        markingModel.commit(force = true)
//        println("Check1 ===:  "+ (jsonSettings.petrinetSetup === petrinetSetupModel.item))
//        println("Dirty:  "+ (petrinetSetupModel.inhibitorArcIds.isDirty))
        
        val jsonString = jsonSettings.toJson()
        val file = File(path)
        
        file.writeText(jsonString)
        return true
    }
    
    
    fun makeNewSettings() {
        settingsModel.itemProperty.value = JsonSettings()
        jsonSettingsPath.value = null
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
    
    
    fun prepareGenerationKit(): PetrinetGenerators.GenerationKit<*> {
        if (!settingsModel.commit())
            throw IllegalStateException("Can not generate. Model is not valid.")
        if (petrinet == null) {
            loadPetrinet()
        }
        val petrinet = petrinet!!
        
        // just copy all actions with json settings.
        petrinet.deleteAllInhibitorResetArcs()
        with(jsonSettings.petrinetSetup) {
            petrinet.markInhResetArcsByIds(inhibitorArcIds, resetArcIds)
        }
        val builder = JsonSettingsBuilder(petrinet, jsonSettings)
        
        val generationDescription = builder.buildDescription()
        val (initialMarking, finalMarking) = builder.buildMarking()
        
        return PetrinetGenerators.GenerationKit(petrinet, initialMarking, finalMarking, generationDescription)
    }
    
}