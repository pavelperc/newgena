package com.pavelperc.newgena.gui.views.settingsview

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.gui.views.PetrinetImageView
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.event.EventTarget
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.util.Duration
import tornadofx.*

fun EventTarget.petrinetSetupPanel(controller: SettingsUIController, settingsView: SettingsView) {
    val petrinetSetup = controller.petrinetSetupModel
    val marking = controller.markingModel
    
    fieldset("petrinetSetup") {
        addClass(Styles.fieldSetFrame)
        
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
                //                    enableWhen(controller.isPetrinetDirty)
                
                toggleClass(Styles.redButton, controller.isPetrinetDirty)
//                            toggleClass(Styles.greenButton, isPetrinetUpdated)
                action {
                    // may crash
                    try {
                        controller.loadPetrinet()
                        settingsView.notification("Petrinet loaded", "okey, okey...") { hideAfter(Duration(2000.0)) }
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
                        val petrinetView = find<PetrinetImageView>()
                        petrinetView.draw(true)
                        petrinetView.openWindow(owner = settingsView.currentStage)
                        
                    } catch (e: Exception) {
                        alert(Alert.AlertType.ERROR, "Failed to update arcs and draw.", e.message)
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
        
        arrayField(
                petrinetSetup.inhibitorArcIds,
                listValidator = validateEdges,
                predefinedValuesToHints = { controller.inputEdgeIdsWithHints },
                hintName = "hint",
                valuesName = "id"
        )
        arrayField(
                petrinetSetup.resetArcIds,
                listValidator = validateEdges,
                predefinedValuesToHints = { controller.inputEdgeIdsWithHints },
                hintName = "hint",
                valuesName = "id"
        )
        // TODO restore inhibitor arcs!!
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