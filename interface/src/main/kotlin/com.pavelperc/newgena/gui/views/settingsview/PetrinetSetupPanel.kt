package com.pavelperc.newgena.gui.views.settingsview

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.gui.views.FastPnView
import com.pavelperc.newgena.gui.views.PetrinetDrawProvider
import com.pavelperc.newgena.gui.views.PetrinetImageView
import com.pavelperc.newgena.petrinet.output.makePnml
import com.pavelperc.newgena.petrinet.petrinetExtensions.deepCopy
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import javafx.util.Duration
import tornadofx.*
import java.io.File
import java.io.FileOutputStream

fun EventTarget.petrinetSetupPanel(controller: SettingsUIController, settingsView: SettingsView) {
    val petrinetSetup = controller.petrinetSetupModel
    val marking = controller.markingModel
    
    fieldset("petrinetSetup") {
        addClass(Styles.fieldSetFrame)
        
        docField("petrinetFile", Orientation.VERTICAL) {
            hbox {
                useMaxSize = true
                hgrow = Priority.ALWAYS
                
                textfield(petrinetSetup.petrinetFile) {
                    required()
                    useMaxSize = true
                    hgrow = Priority.ALWAYS
                }
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
                    //                    enableWhen(controller.isPetrinetDirty)
                    
                    toggleClass(Styles.redButton, controller.isPetrinetDirty)
//                            toggleClass(Styles.greenButton, isPetrinetUpdated)
                    action {
                        // may crash
                        try {
                            controller.loadPetrinet()
                            settingsView.notification("Petrinet loaded", "okey, okey...") { hideAfter(Duration(2000.0)) }
                        } catch (e: Exception) {
                            controller.unloadPetrinet()
                            error("Failed to load model:", e.message)
                        }
                    }
                    isFocusTraversable = false
                }
            }
            hbox {
                button("draw") {
                    enableWhen(controller.isPetrinetLoaded)
                    action {
                        try {
                            val petrinetView = find<PetrinetImageView>(FX.defaultScope, "petrinetDrawProvider" to controller)
                            petrinetView.draw(true)
                            petrinetView.openWindow(owner = settingsView.currentStage)
                            
                        } catch (e: Exception) {
                            alert(Alert.AlertType.ERROR, "Failed to update arcs and draw.", e.message)
                        }
                    }
                }
                button("create") {
                    controller.isPetrinetLoaded.onChange { isLoaded ->
                        if (isLoaded) {
                            this@button.text = "edit"
                        } else {
                            this@button.text = "create"
                        }
                    }
                    
                    action {
                        val fastPnEditor = FastPnView(controller.petrinet?.deepCopy()) { updatedPetrinet ->
                            controller.loadUpdatedPetrinet(updatedPetrinet)
                        }
                        val stage = fastPnEditor.openWindow(owner = settingsView.currentStage, escapeClosesWindow = false)
                        fastPnEditor.setOnCloseAction(stage)
                        
                    }
                }
                button("save") {
                    enableWhen(controller.isPetrinetLoaded)
                    action {
                        val path = petrinetSetup.petrinetFile.value
                        confirm("Save petrinet", "Save to $path?") {
                            try {
                                controller.savePetrinet(path)
                                settingsView.notification("Saved petrinet to $path")
                            } catch (e: Exception) {
                                error("Can not save petrinet.", e.message)
                            }
                        }
                    }
                }
                button("save as") {
                    enableWhen(controller.isPetrinetLoaded)
                    action {
                        try {
                            val path = controller.savePetrinetAs()
                            if (path != null) {
                                settingsView.notification("Saved petrinet to $path")
                            }
                        } catch (e: Exception) {
                            error("Can not save petrinet.", e.message)
                        }
                    }
                }
            }
        }
        
        val validateEdges: Validator<List<String>> = { list ->
            val input = list.toSet()
            val unknown = input - input.intersect(controller.inputEdgeIdsWithHints.keys)
            if (controller.petrinet != null && unknown.isNotEmpty()) {
                warning("Not found input edges: $unknown")
            } else null
        }
    }
    
    // marking:
    fieldset("Marking") {
        addClass(Styles.fieldSetFrame)
        
        val validatePlaces: Validator<Map<String, Int>> = xx@{ map ->
            if (map.values.any { it <= 0 })
                return@xx error("All place counts should be positive.")
            
            val input: Set<String> = map.keys
            val unknown = input - input.intersect(controller.placeIdsWithHints.keys)
            if (controller.petrinet != null && unknown.isNotEmpty()) {
                warning("Not found places: $unknown")
            } else {
                null
            }
        }
        
        checkboxField(marking.isUsingInitialMarkingFromPnml)
        intMapField(
                marking.initialPlaceIds,
                mapValidator = validatePlaces,
                predefinedValuesToHints = { controller.placeIdsWithHints },
                hintName = "label",
                valuesName = "id"
        )
        intMapField(
                marking.finalPlaceIds,
                mapValidator = validatePlaces,
                predefinedValuesToHints = { controller.placeIdsWithHints },
                hintName = "label",
                valuesName = "id"
        )
    }
}