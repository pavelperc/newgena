package com.pavelperc.newgena.gui.views.settingsview

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.gui.views.*
import com.pavelperc.newgena.loaders.settings.JsonSettings
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import javafx.util.Duration
import tornadofx.*


class SettingsView : View("Settings") {
    
    private val controller by inject<SettingsUIController>()
    
    private val settings = controller.settingsModel
    
    override val root = VBox()
    
    private var saidHello = false
    
    override fun onDock() {
        super.onDock()
        if (!saidHello) {
            saidHello = true
            runAsync {
                runLater(Duration(200.0)) {
                    notification("Hello!", duration = 1000) {
                        position(Pos.TOP_CENTER)
                    }
                }
            }
        }
    }
    
    init {
        with(root) {
            scrollableFieldset {
                label("Using settings version: ${JsonSettings.LAST_SETTINGS_VERSION}") {
                    useMaxSize = true
                    style {
                        textAlignment = TextAlignment.LEFT
                        textFill = Color.GRAY
                    }
                }
                
                // Common settings:
                
                field("outputFolder") {
                    textfield(settings.outputFolder).required()
                    
                    button(graphic = FontAwesomeIconView(FontAwesomeIcon.FOLDER)) {
                        action {
                            controller.requestOutputFolderChooseDialog()
                        }
                        isFocusTraversable = false
                    }
                }
                
                // ---- PETRINET SETUP ----
                petrinetSetupPanel(controller, this@SettingsView)
                
                
                intField(settings.numberOfLogs, nonNegative = true)
                intField(settings.numberOfTraces, nonNegative = true)
                intField(settings.maxNumberOfSteps, nonNegative = true)
                
                checkboxField(settings.isRemovingEmptyTraces)
                checkboxField(settings.isRemovingUnfinishedTraces)
                
                
                // --- NOISE ---
                noisePanel(controller)
                
                // --- PRIORITIES ---
                prioritiesPanel(controller)
    
                // ---TIME---
                timePanel(controller)
            }
            
            // ------- Non scrollable part --------
            form {
                fieldset {
                    settingsLoadingPanel()
                    
                    button("Generate logs!") {
                        enableWhen(controller.allModelsAreValid)
                        shortcut("Ctrl+G")
                        tooltip("Ctrl+G")
                        
                        action {
                            try {
                                val generationKit = controller.prepareGenerationKit()
                                
                                val generationView = find<GenerationView>(mapOf(
                                        "generationKit" to generationKit,
                                        "outputFolder" to settings.outputFolder.value)
                                )
                                if (generationView.isDocked) {
                                    generationView.close()
                                }
                                generationView.openWindow(escapeClosesWindow = false)
                                
//                                replaceWith(generationView, ViewTransition.Slide(0.2.seconds))
                                
                                
                            } catch (e: Exception) {
                                alert(Alert.AlertType.ERROR, "Couldn't apply settings:", e.message)
                                e.printStackTrace()
                            }
                            
                        }
                        
                    }
                }
            }
        }
    }
    
    
    // ---LOADING SETTINGS---
    private fun EventTarget.settingsLoadingPanel() {
        hbox {
            //            button("print") {
//                enableWhen(controller.allModelsAreValid)
//                action {
//                    settings.commit()
//                    println(controller.jsonSettings)
//                }
//            }
            vbox {
                // just to bind tooltip!
                button("Save settings") {
                    shortcut("Ctrl+S")
                    controller.allModelsAreValid.onChange { valid ->
                        toggleClass(Styles.redButton, !valid)
                        this@vbox.tooltip(if (!valid)
                            "Some settings are invalid!" else null)
                    }
                    
                    enableWhen(controller.allModelsAreValid.and(controller.settingsAreNotSaved))
                    
                    action {
                        val result: Boolean
                        if (controller.hasNewSettings.value) {
                            result = controller.saveJsonSettingsAs()
                        } else {
                            result = controller.saveJsonSettings(controller.jsonSettingsPath.value)
                        }
                        
                        if (result)
                            notification("Settings were saved.")
                    }
                }
            }
            
            button("New settings") {
                action {
                    confirmIf(controller.settingsAreNotSaved.value,
                            "Settings may be not saved.", "Continue?") {
                        
                        controller.makeNewSettings()
                    }
                    
                }
            }
            button("Load settings") {
                action {
                    if (controller.settingsAreNotSaved.value
                            && !confirmed("Settings may be not saved.", "Continue?")) {
                        return@action
                    }
                    
                    try {
                        if (!controller.loadJsonSettings())
                            return@action // when we canceled the fileChooser.
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
                        error("Settings are loaded, but failed to load petrinet:", e.message)
                    }
                }
            }
            
        }
        
        hbox {
            label("Loaded settings: ")
            label(controller.jsonSettingsPath)
            hiddenWhen(controller.hasNewSettings)
        }
        label("Unsaved Settings") {
            visibleWhen(controller.hasNewSettings)
        }
    }
}
