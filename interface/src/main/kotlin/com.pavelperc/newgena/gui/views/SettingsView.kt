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
    
    override fun onDock() {
        super.onDock()
        runAsync {
            runLater(Duration(200.0)) {
                notification("Hello!") {
                    position(Pos.TOP_CENTER)
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
            }
        }
    }
    
    fun EventTarget.settingsLoadingPanel() {
        hbox {
            button("Save model") {
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
                        confirm("Model is not saved.", "Continue?") {
                            controller.makeNewSettings()
                        }
                    } else {
                        controller.makeNewSettings()
                    }
                }
            }
            button("Load settings") {
                action {
                    controller.loadJsonSettings()
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
                        controller.loadPetrinet()
                        notification("Petrinet loaded", "okey, okey...") { hideAfter(Duration(2000.0)) }
//                                alert(Alert.AlertType.INFORMATION, "Not implemented.")
                    }
                    isFocusTraversable = false
                }
                button("draw") {
                    enableWhen(controller.isPetrinetUpdated)
                    action {
                        val petrinetImage = PetrinetImageView()
                        petrinetImage.openWindow()
                    }
                }
                
            }
            
            fun TextField.validateEdges(prop: Property<ObservableList<String>>) {
                prop.addValidator(this, ValidationTrigger.OnChange(300)) { value ->
                    val input = value?.toSet() ?: emptySet()
                    val unknown = input - input.intersect(controller.edgeIds)
                    if (controller.petrinet != null && unknown.isNotEmpty()) {
                        warning("Not found edges: $unknown")
                    } else null
                }
            }
            
            arrayField(petrinetSetup.inhibitorArcIds) { validateEdges(petrinetSetup.inhibitorArcIds) }
            arrayField(petrinetSetup.resetArcIds) { validateEdges(petrinetSetup.resetArcIds) }
            
            field {
                button("update arcs in petrinet") {
                    enableWhen(controller.isPetrinetUpdated)
                    action {
                        try {
                            // what if it deletes old, but fails with adding new?
                            controller.updateInhResetArcsFromModel()
                            
                            notification {
                                title("Arcs are updated.")
                                text("Wow, Nothing crashed!")
                            }
                            
                        } catch (e: Exception) {
                            alert(Alert.AlertType.ERROR, "Failed to update arcs, but previous arcs are reset.", e.message)
                        }
                    }
                }
            }
        }
    }
    
}
