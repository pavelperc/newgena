package com.pavelperc.newgena.gui.controller

import com.pavelperc.newgena.gui.app.ArtificialIntelligence
import com.pavelperc.newgena.gui.customfields.confirmIf
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
import com.pavelperc.newgena.utils.common.emptyMarking
import com.pavelperc.newgena.utils.common.profile
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.semantics.petrinet.Marking
import tornadofx.*
import java.io.File


class SettingsUIController : Controller() {
    
    val prefController by inject<PreferencesController>()
    
    val jsonSettings: JsonSettings
        get() = settingsModel.item!!
    
    var petrinet: ResetInhibitorNet? = null
        private set(value) {
            placeIds = value?.places?.map { it.pnmlId }?.toSet() ?: emptySet()
            transitionIds = value?.transitions?.map { it.pnmlId }?.toSet() ?: emptySet()
            inputEdgeIds = value?.transitions?.flatMap { value.getInEdges(it) }?.map { it.pnmlId }?.toSet() ?: emptySet()
            field = value
        }
    
    /** Place pnml ids of loaded petrinet, or empty set */
    var placeIds: Set<String> = emptySet()
        private set
    
    /** Arc pnml ids of loaded petrinet, or empty set */
    var inputEdgeIds: Set<String> = emptySet()
        private set
    
    /** Transition pnml ids of loaded petrinet, or empty set */
    var transitionIds: Set<String> = emptySet()
        private set
    
    
    private var pnmlMarking = emptyMarking()
    
    val markings: Pair<Marking, Marking>
        get() {
            val fromSettings = petrinet?.let { petrinet ->
                markingModel.commit()
                JsonSettingsBuilder.buildMarkingOnly(markingModel.item, petrinet)
            } ?: return emptyMarking() to emptyMarking()
            
            if (markingModel.isUsingInitialMarkingFromPnml.value)
                return pnmlMarking to fromSettings.second
            
            return fromSettings
        }
    
    // --- javafx properties:
    /** True if the loaded petrinet is from settings file path.*/
    val isPetrinetUpdated = SimpleBooleanProperty(false)
    val isPetrinetDirty = isPetrinetUpdated.not()
    
    private var loadedPetrinetFilePath: String? = null
    
    /** Currently loaded json settings. */
    val jsonSettingsPath = SimpleStringProperty(null)
    
    val hasNewSettings = jsonSettingsPath.isNull
    
    // --- MODELS:
    val settingsModel = SettingsModel(JsonSettings()) // start from default jsonSettings.
    val petrinetSetupModel = settingsModel.petrinetSetupModel
    val markingModel = petrinetSetupModel.markingModel
    val staticPrioritiesModel = settingsModel.staticPrioritiesModel
    val noiseModel = settingsModel.noiseDescription
    
    val allModelsAreValid: BooleanBinding =
            settingsModel.valid
                    .and(petrinetSetupModel.valid)
                    .and(markingModel.valid)
                    .and(staticPrioritiesModel.valid)
                    .and(noiseModel.valid)
    
    
    /** Warning! not dirty callback doesn't mean, that the settings are saved. */
    private fun onSomeModelGetsDirty(onDirty: () -> Unit) {
        // boolean binding doesn't work for some reason
        settingsModel.dirty.onChange { dirty -> if (dirty) onDirty() }
        petrinetSetupModel.dirty.onChange { dirty -> if (dirty) onDirty() }
        markingModel.dirty.onChange { dirty -> if (dirty) onDirty() }
        staticPrioritiesModel.dirty.onChange { dirty -> if (dirty) onDirty() }
        noiseModel.dirty.onChange { dirty -> if (dirty) onDirty() }
    }
    
    private fun loadInitialSettings() {
//        loadJsonSettingsFromPath("examples/petrinet/simpleExample/settings.json")
//        loadJsonSettingsFromPath("examples/petrinet/complex1/settings.json")
        
        val lastSettingsPath = prefController.loadLastSettingsPath()
        if (lastSettingsPath != null) {
            loadJsonSettingsFromPath(lastSettingsPath)
        }
    }
    
    /** It becomes true when we just loaded new settings or saved them or created new settings.
     * (Default settings are considered as saved).
     * It becomes false when we change some settings.*/
    val settingsAreSaved = SimpleBooleanProperty(true)
    
    /** See [settingsAreSaved] */
    val settingsAreNotSaved = settingsAreSaved.not()
    
    
    init {
        // grephviz: speedup first draw
//        Graphviz.useDefaultEngines()
        
        // check if the entered file path is synchronized with the model.
        petrinetSetupModel.petrinetFile.onChange { enteredFile ->
            isPetrinetUpdated.set(loadedPetrinetFilePath == enteredFile)
        }
        
        
        // handle exit
//        Platform.setImplicitExit(false);
        primaryStage.onCloseRequest = EventHandler { event ->
            confirmIf(settingsAreNotSaved.value,
                    "Are you sure you want to exit?", "You have unsaved settings.") {
                prefController.saveLastSettingsPath(jsonSettingsPath.value)
                
                ArtificialIntelligence.goodbye(primaryStage)
            }
            event.consume() // cancels closing
        }
        
        
        loadInitialSettings()
        
        
        // setup good settings dirtiness property:
        // models could be not dirty after generation, but settings are still not saved.
        onSomeModelGetsDirty {
            settingsAreSaved.set(false)
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
    
    fun loadPetrinet(): ResetInhibitorNet {
        profile("Loading petrinet") {
            PnmlLoader.loadPetrinetWithOwnParser(petrinetSetupModel.petrinetFile.value).also { result ->
                petrinet = result.first
                pnmlMarking = result.second
            }
        }
        loadedPetrinetFilePath = petrinetSetupModel.petrinetFile.value
        isPetrinetUpdated.set(true)
        
        return petrinet!!
    }
    
    /** @return true if the fileChooser dialog was not canceled and everything is ok. */
    fun loadJsonSettings(): Boolean {
        val cwd = File(System.getProperty("user.dir"))
        val prev = File(jsonSettingsPath.value ?: "").parentFile
        
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = if (prev != null && prev.isDirectory) prev else cwd
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Settings in json", "*.json"))
        
        val path = fileChooser.showOpenDialog(null)?.relativePath ?: return false
        
        loadJsonSettingsFromPath(path)
        return true
    }
    
    fun loadJsonSettingsFromPath(path: String) {
        profile("Loading json settings") {
            settingsModel.itemProperty.value = JsonSettings.fromFilePath(path)
        }
        
        jsonSettingsPath.value = path
        settingsAreSaved.set(true)
        
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
        val jsonString = jsonSettings.toJson()
        val file = File(path)
        
        file.writeText(jsonString)
        settingsAreSaved.set(true)
        return true
    }
    
    
    fun makeNewSettings() {
        settingsModel.itemProperty.value = JsonSettings()
        jsonSettingsPath.value = null
        settingsAreSaved.set(true)
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
        
        val petrinet = petrinet ?: loadPetrinet()
        updateInhResetArcsFromModel()
        
        val builder = JsonSettingsBuilder(petrinet, jsonSettings)
        
        val generationDescription = builder.buildDescription()
        val (initialMarking, finalMarking) = builder.buildMarking()
        
        return PetrinetGenerators.GenerationKit(petrinet, initialMarking, finalMarking, generationDescription)
    }
    
}