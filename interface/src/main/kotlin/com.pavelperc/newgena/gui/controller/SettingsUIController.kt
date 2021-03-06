package com.pavelperc.newgena.gui.controller

import com.pavelperc.newgena.gui.app.ArtificialIntelligence
import com.pavelperc.newgena.gui.customfields.confirmIf
import com.pavelperc.newgena.gui.model.SettingsModel
import com.pavelperc.newgena.gui.views.PetrinetDrawProvider
import com.pavelperc.newgena.launchers.PetrinetGenerators
import com.pavelperc.newgena.loaders.settings.JsonSettingsBuilder
import com.pavelperc.newgena.loaders.settings.fromFilePath
import com.pavelperc.newgena.loaders.settings.jsonSettings.JsonSettings
import com.pavelperc.newgena.loaders.settings.toJson
import com.pavelperc.newgena.petrinet.output.makePnmlStr
import com.pavelperc.newgena.petrinet.petrinetExtensions.pnmlId
import com.pavelperc.newgena.utils.common.emptyMarking
import com.pavelperc.newgena.utils.common.getCwd
import com.pavelperc.newgena.utils.common.profile
import guru.nidi.graphviz.engine.Graphviz
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.semantics.petrinet.Marking
import tornadofx.*
import java.io.File


class SettingsUIController : Controller(), PetrinetDrawProvider {
    
    val prefController by inject<PreferencesController>()
    
    val petrinetController = PetrinetUIController()
    
    val jsonSettings: JsonSettings
        get() = settingsModel.item!!
    
    private fun setPetrinetOnChange() {
        petrinetController.petrinetProp.onChange { value ->
            placeIdsWithHints = value?.places
                    ?.map { it.pnmlId to (it.label ?: "") }
                    ?.toMap()
                    ?: emptyMap()
            
            transitionIdsWithHints = value?.transitions
                    ?.map { it.pnmlId to (it.label ?: "") }
                    ?.toMap()
                    ?: emptyMap()
            
            inputEdgeIdsWithHints = value?.transitions
                    ?.flatMap { value.getInEdges(it) }
                    ?.map { it.pnmlId to "${it.source.pnmlId}->${it.target.pnmlId}" }
                    ?.toMap()
                    ?: emptyMap()
            
            // restart validation 
            // validate this and all inner models.
            settingsModel.allModels.forEach { it.validate() }
        }
    }
    
    override val petrinet: ResetInhibitorNet?
        get() = petrinetController.petrinet
    
    val petrinetProp: SimpleObjectProperty<ResetInhibitorNet?>
        get() = petrinetController.petrinetProp
    
    /** Place pnml ids of loaded petrinet, or empty map. Hints are labels. */
    var placeIdsWithHints: Map<String, String> = emptyMap()
        private set
    
    /** Arc pnml ids of loaded petrinet, or empty map. Hints are "sourceId->targetId" */
    var inputEdgeIdsWithHints: Map<String, String> = emptyMap()
        private set
    
    /** Transition pnml ids of loaded petrinet, or empty map. Hints are labels. */
    var transitionIdsWithHints: Map<String, String> = emptyMap()
        private set
    
    // for petrinetImageView
    override fun requestPetrinetUpdate() {
        
    }
    
    override val pnmlLocationForDrawing: String?
        get() = petrinetSetupModel.petrinetFile.value
    
    override val markings: Pair<Marking, Marking>
        get() {
            val fromSettings = petrinet?.let { petrinet ->
                markingModel.commit()
                JsonSettingsBuilder.buildMarkingOnly(markingModel.item, petrinet)
            } ?: return emptyMarking() to emptyMarking()
            
            if (markingModel.isUsingInitialMarkingFromPnml.value)
                return petrinetController.pnmlMarking to fromSettings.second
            
            return fromSettings
        }
    
    
    // --- javafx properties:
    /** True if the loaded petrinet path corresponds with settings file path. */
    val isPetrinetUpdated = SimpleBooleanProperty(false)
    val isPetrinetDirty = isPetrinetUpdated.not()
    
    /** It is false when the petrinet is not null and was updated. */
    val isPetrinetSaved = petrinetController.isPetrinetSaved
    val isPetrinetNotSaved = petrinetController.isPetrinetNotSaved
    
    /** Is petrinet not null. (Created or loaded.) */
    val isPetrinetLoaded = petrinetProp.isNotNull
    
    /** Currently loaded json settings. */
    val jsonSettingsPath = SimpleStringProperty(null)
    
    /** Completely new, unsaved settings. */
    val haveNewSettings = jsonSettingsPath.isNull!!
    
    // --- MODELS---:
    val settingsModel = SettingsModel(JsonSettings()) // start from default jsonSettings.
    val petrinetSetupModel = settingsModel.petrinetSetupModel
    val markingModel = petrinetSetupModel.markingModel
    val staticPrioritiesModel = settingsModel.staticPrioritiesModel
    val noiseModel = settingsModel.noiseModel
    val timeModel = settingsModel.timeModel
    val timeNoiseModel = timeModel.timeDrivenNoise
    
    val allModelsAreValid: BooleanBinding = settingsModel.allModels
            .map { it.valid }
            .let { validProps ->
                Bindings.createBooleanBinding(
                        { validProps.all { it.value } },
                        validProps.toTypedArray()
                )
            }
    
    
    /** Setup good settings dirtiness property.
     * Warning! not dirty callback doesn't mean, that the settings are saved. */
    private fun onSomeModelGetsDirty(onDirty: () -> Unit) {
        // boolean binding doesn't work for some reason
        settingsModel.allModels.forEach { model ->
            model.dirty.onChange { dirty -> if (dirty) onDirty() }
        }
    }
    
    private fun loadInitialSettings() {
//        loadJsonSettingsFromPath("examples/petrinet/simpleExample/settings.json")
//        loadJsonSettingsFromPath("examples/petrinet/complex1/settings.json")
        
        val lastSettingsPath = prefController.loadLastSettingsPath()
        if (lastSettingsPath != null) {
            try {
                loadJsonSettingsFromPath(lastSettingsPath)
            } catch (e: Exception) {
                error("Failed loading settings from $lastSettingsPath:", e.message)
                e.printStackTrace()
            }
        }
    }
    
    /** It becomes true when we just loaded new settings or saved them or created new settings.
     * (Default settings are considered as saved).
     * It becomes false when we change some settings.*/
    val settingsAreSaved = SimpleBooleanProperty(true)
    
    /** See [settingsAreSaved] */
    val settingsAreNotSaved = settingsAreSaved.not()
    
    
    init {
        setPetrinetOnChange()
        profile("Graphviz, loading engine:") {
            // graphviz: speedup first draw
            Graphviz.useDefaultEngines()
        }
        
        // check if the entered file path is synchronized with the model.
        petrinetSetupModel.petrinetFile.onChange { enteredFile ->
            isPetrinetUpdated.set(petrinetController.loadedPetrinetFilePath == enteredFile)
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
            return this.toRelativeString(cwd).replace("\\", "/")
        }
    
    
    /** @return if the dialog was not cancelled. */
    fun requestPetrinetFileChooseDialog(): Boolean {
        val cwd = File(getCwd())
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
    
    /** Loading petrinet from petrinetFile in a text field. */
    fun loadPetrinet(): ResetInhibitorNet {
        return petrinetController.loadPetrinet(petrinetSetupModel.petrinetFile.value).also {
            isPetrinetUpdated.set(true)
        }
    }
    
    /** Load new petrinet from editor. */
    fun loadUpdatedPetrinet(updatedPetrinet: ResetInhibitorNet) {
        petrinetController.loadUpdatedPetrinet(updatedPetrinet)
    }
    
    fun savePetrinet(path: String) {
        if (petrinet == null) {
            return
        }
        
        val file = File(path)
        val pnml = makePnmlStr(petrinet!!)
        file.writeText(pnml)
        
        petrinetSetupModel.petrinetFile.value = path
        isPetrinetUpdated.set(true)
        
    }
    
    /** Returns saving path or null in case of cancel. */
    fun savePetrinetAs(): String? {
        val cwd = File(getCwd())
        val prev = File(jsonSettingsPath.value ?: "").parentFile
        
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = if (prev != null && prev.isDirectory) prev else cwd
        
        val path = fileChooser.showSaveDialog(null)?.relativePath ?: return null
        
        savePetrinet(path)
        return path
    }
    
    
    /** In case of unsuccessful petrinet loading. Removes the petrinet and sets [isPetrinetUpdated] as false. */
    fun unloadPetrinet() {
        petrinetController.unloadPetrinet()
        isPetrinetUpdated.set(false)
    }
    
    /** @return true if the fileChooser dialog was not canceled and everything is ok. */
    fun loadJsonSettings(): Boolean {
        val cwd = File(getCwd())
        val prev = File(jsonSettingsPath.value ?: "").parentFile
        
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = if (prev != null && prev.isDirectory) prev else cwd
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Settings in json", "*.json"))
        
        val path = fileChooser.showOpenDialog(null)?.relativePath ?: return false
        
        loadJsonSettingsFromPath(path)
        return true
    }
    
    fun loadJsonSettingsFromPath(path: String) {
        profile("Loading json settings:") {
            settingsModel.itemProperty.value = JsonSettings.fromFilePath(path) { migrationMessage ->
                information("Successfully migrated settings.", migrationMessage)
                println("Migration:")
                println(migrationMessage)
            }
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
    
    fun prepareGenerationKit(): PetrinetGenerators.GenerationKit {
        if (!settingsModel.commit())
            throw IllegalStateException("Can not generate. Model is not valid.")
        
        val petrinet = petrinet ?: loadPetrinet()
        
        val builder = JsonSettingsBuilder(petrinet, jsonSettings)
        
        val generationDescription = builder.buildDescription()
        val (initialMarking, finalMarking) = markings
        
        return PetrinetGenerators.GenerationKit(petrinet, initialMarking, finalMarking, generationDescription)
    }
}