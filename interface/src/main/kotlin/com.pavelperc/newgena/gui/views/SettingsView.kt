package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.event.EventTarget
import javafx.scene.control.Alert
import javafx.scene.control.Button
import tornadofx.*


class SettingsView : View("Settings") {
    
    private val controller by inject<SettingsUIController>()
    
    private val settings = controller.settingsModel
    private val petrinetSetup = controller.petrinetSetupModel
    private val marking = controller.markingModel
    
    override val root = Form()
    
    init {
        with(root) {
            fieldset {
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
            button("Commit, save and print") {
                shortcut("Ctrl+Shift+S")
                
                enableWhen(controller.allModelsAreValid
                        .and(controller.someModelIsDirty))
                action {
                    if (controller.saveJsonSettingsAs())
                        notification("Settings were saved.")
                    
                    println(settings.item)
                }
            }
            button("Fast Save") {
                shortcut("Ctrl+S")
                
                enableWhen(controller.allModelsAreValid
                        .and(controller.jsonSettingsPath.isNotNull)
                        .and(controller.someModelIsDirty))
                
                action {
                    if (controller.saveJsonSettings(controller.jsonSettingsPath.value))
                        notification("Settings were saved.")
                    println(settings.item)
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
            addClass(Styles.innerFieldset)
            
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
                        notification("Petrinet loaded", "okey, okey...")
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
            
            
            arrayField(petrinetSetup.inhibitorArcIds) {
                // catch only replacing the whole list
//                petrinetSetup.inhibitorArcIds.addValidator(textField) {value->
//                    
//                }
            }
            arrayField(petrinetSetup.resetArcIds)
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
