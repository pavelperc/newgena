package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.Property
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.util.Duration
import tornadofx.*


class SettingsView : View("Settings") {
    
    private val controller by inject<SettingsUIController>()
    
    private val settings = controller.settingsModel
    private val petrinetSetup = controller.petrinetSetupModel
    private val marking = controller.markingModel
    
    override val root = Form()
    
    private var saidHello = false
    
    override fun onDock() {
        super.onDock()
        if (!saidHello) {
            saidHello = true
            runAsync {
                runLater(Duration(200.0)) {
                    notification("Hello!") {
                        position(Pos.TOP_CENTER)
                    }
                }
            }
        }
    }
    
    init {
        with(root) {
            fieldset {
                addClass(Styles.mainSettingsPanel)
                field("outputFolder") {
                    textfield(settings.outputFolder).required()
                    
                    button(graphic = FontAwesomeIconView(FontAwesomeIcon.FOLDER)) {
                        action {
                            controller.requestOutputFolderChooseDialog()
                        }
                        isFocusTraversable = false
                    }
                }
                petrinetSetupPanel()
                
                // marking:
                
                fieldset("Marking") {
                    
                    fun TextField.validatePlaces(prop: Property<ObservableList<String>>) {
                        prop.addValidator(this, ValidationTrigger.OnChange(300)) { value ->
                            val input = value?.toSet() ?: emptySet()
                            val unknown = input - input.intersect(controller.placeIds)
                            if (controller.petrinet != null && unknown.isNotEmpty()) {
                                warning("Not found places: $unknown")
                            } else null
                        }
                    }
                    
                    
                    
                    checkboxField(marking.isUsingInitialMarkingFromPnml)
                    arrayField(marking.initialPlaceIds) {
                        validatePlaces(marking.initialPlaceIds)
                    }
                    arrayField(marking.finalPlaceIds) {
                        validatePlaces(marking.finalPlaceIds)
                    }
                }
                
                
                intField(settings.numberOfLogs) { validUint() }
                intField(settings.numberOfTraces) { validUint() }
                intField(settings.maxNumberOfSteps) { validUint() }
                
                checkboxField(settings.isRemovingEmptyTraces)
                checkboxField(settings.isRemovingUnfinishedTraces)
                
                checkboxField(settings.isUsingNoise)
                
                
                checkboxField(settings.isUsingStaticPriorities) {
                    action {
                        if (isSelected)
                            settings.isUsingTime.value = false
                    }
                }
                checkboxField(settings.isUsingTime) {
                    action {
                        if (isSelected)
                            settings.isUsingStaticPriorities.value = false
                    }
                }
                settingsLoadingPanel()
                
                button("Generate logs!") {
                    enableWhen(controller.allModelsAreValid)
                    shortcut("Ctrl+G")
                    tooltip("Ctrl+G")
                    
                    action {
                        try {
                            val generationKit = controller.prepareGenerationKit()
                            
                            val view = find<GenerationView>(mapOf(
                                    "generationKit" to generationKit,
                                    "outputFolder" to settings.outputFolder.value)
                            )
                            replaceWith(view, ViewTransition.Slide(0.2.seconds))
                            
                        } catch (e: Exception) {
                            alert(Alert.AlertType.ERROR, "Couldn't apply settings:", e.message)
                        }
                        
                    }
                    
                }
            }
        }
    }
    
    fun EventTarget.settingsLoadingPanel() {
        hbox {
            button("print") {
                enableWhen(controller.allModelsAreValid
                        .and(controller.someModelIsDirty))
                action {
                    settings.commit()
                    println(controller.jsonSettings)
                }
            }
            button("Save settings") {
                shortcut("Ctrl+S")
                enableWhen(controller.allModelsAreValid
                        .and(controller.someModelIsDirty))
                action {
                    val result: Boolean
                    if (controller.jsonSettingsPath.value == null) {
                        result = controller.saveJsonSettingsAs()
                    } else {
                        result = controller.saveJsonSettings(controller.jsonSettingsPath.value)
                    }
                    
                    if (result)
                        notification("Settings were saved.")
                }
            }
            
            button("New settings") {
                action {
                    
                    if (controller.someModelIsDirty.value) {
                        confirm("Settings are not saved.", "Continue?") {
                            controller.makeNewSettings()
                        }
                    } else {
                        controller.makeNewSettings()
                    }
                }
            }
            button("Load settings") {
                action {
                    try {
                        controller.loadJsonSettings()
                    } catch (e: Exception) {
                        error("Broken json settings:", e.message)
                        return@action
                    }
                    
                    try {
                        controller.loadPetrinet()
                        notification {
                            title("Settings and petrinet are loaded.")
                            text("Wow, Nothing crashed!")
                        }
                    } catch (e: Exception) {
                        error("Failed to load petrinet:", e.message)
                    }
                }
            }
            
        }
        
        hbox {
            label("Loaded settings: ")
            label(controller.jsonSettingsPath)
            visibleWhen(controller.jsonSettingsPath.isNotNull)
        }
        label("Unsaved Settings") {
            visibleWhen(controller.jsonSettingsPath.isNull)
        }
    }
    
    fun EventTarget.petrinetSetupPanel() {
        fieldset("petrinetSetup") {
            
            field("petrinetFile") {
                
                textfield(petrinetSetup.petrinetFile).required()
                var btnLoadPetrinet: Button? = null
                // select file
                button(graphic = FontAwesomeIconView(FontAwesomeIcon.FILE)) {
                    action {
                        if (controller.requestPetrinetFileChooseDialog()) {
                            btnLoadPetrinet?.fire()
                        }
                    }
                    isFocusTraversable = false
                }
                btnLoadPetrinet = button("Load model") {
                    enableWhen(controller.isPetrinetDirty)
                    
                    toggleClass(Styles.redButton, controller.isPetrinetDirty)
//                            toggleClass(Styles.greenButton, isPetrinetUpdated)
                    action {
                        // may crash
                        try {
                            controller.loadPetrinet()
                            notification("Petrinet loaded", "okey, okey...") { hideAfter(Duration(2000.0)) }
                        } catch (e: Exception) {
                            error("Failed to load model:", e.message)
                        }
                    }
                    isFocusTraversable = false
                }
                button("draw") {
                    enableWhen(controller.isPetrinetUpdated)
                    action {
                        try {
                            controller.updateInhResetArcsFromModel()
                            
                            val petrinetImage = PetrinetImageView()
                            petrinetImage.openWindow()
                            
                        } catch (e: Exception) {
                            alert(Alert.AlertType.ERROR, "Failed to update arcs, previous arcs are reset to normal.", e.message)
                        }
                    }
                }
                
            }
            
            fun TextField.validateEdges(prop: Property<ObservableList<String>>) {
                prop.addValidator(this, ValidationTrigger.OnChange(300)) { value ->
                    val input = value?.toSet() ?: emptySet()
                    val unknown = input - input.intersect(controller.inputEdgeIds)
                    if (controller.petrinet != null && unknown.isNotEmpty()) {
                        warning("Not found input edges: $unknown")
                    } else null
                }
            }
            
            arrayField(petrinetSetup.inhibitorArcIds) { validateEdges(petrinetSetup.inhibitorArcIds) }
            arrayField(petrinetSetup.resetArcIds) { validateEdges(petrinetSetup.resetArcIds) }

//            field {
//                button("update arcs in petrinet") {
//                    enableWhen(controller.isPetrinetUpdated)
//                    action {
//                        try {
//                            // what if it deletes old, but fails with adding new?
//                            controller.updateInhResetArcsFromModel()
//                            
//                            notification {
//                                title("Arcs are updated.")
//                                text("Wow, Nothing crashed!")
//                            }
//                            
//                        } catch (e: Exception) {
//                            alert(Alert.AlertType.ERROR, "Failed to update arcs, previous arcs are reset to normal.", e.message)
//                        }
//                    }
//                }
//            }
        }
    }
    
}
