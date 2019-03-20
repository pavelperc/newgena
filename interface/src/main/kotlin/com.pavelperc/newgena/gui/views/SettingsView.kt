package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.models.markInhResetArcsByIds
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.util.Duration
import org.controlsfx.control.Notifications
import tornadofx.*
import java.lang.Exception


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
                petrinetsetup()
                
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
                
                button("Commit and print") {
                    enableWhen(controller.allModelsAreValid)
                    action {
                        settings.commit()
                        println(settings.item)
                    }
                }
            }
        }
    }
    
    fun EventTarget.petrinetsetup() {
        fieldset("petrinetSetup") {
            addClass(Styles.innerFieldset)
            
            field("petrinetFile") {
                
                textfield(petrinetSetup.petrinetFile).required()
                var btnLoadModel: Button? = null
                // select file
                button(graphic = FontAwesomeIconView(FontAwesomeIcon.FILE)) {
                    action {
                        if (controller.requestPetrinetFileChooseDialog()) {
                            btnLoadModel?.fire()
                        }
                    }
                    isFocusTraversable = false
                }
                btnLoadModel = button("Load model") {
                    enableWhen(controller.isPetrinetDirty)
                    
                    toggleClass(Styles.redButton, controller.isPetrinetDirty)
//                            toggleClass(Styles.greenButton, isPetrinetUpdated)
                    
                    action {
                        // may crash
                        controller.loadPetrinet()
                        println("loaded petrinet")
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
                val textField = this
                // catch only replacing the whole list
//                petrinetSetup.inhibitorArcIds.addValidator(textField) {value->
//                    
//                }
            }
            arrayField(petrinetSetup.resetArcIds)
            field(" ") { 
                button("update arcs in petrinet") {
                    enableWhen(controller.isPetrinetUpdated)
                    action {
                        try {
                            // what if it deletes old, but fails with adding new?
                            controller.updateInhResetArcsFromModel()
                            
                            Notifications
                                    .create()
                                    .title("Arcs are updated.")
                                    .owner(this)
                                    .hideAfter(Duration(1000.0))
                                    .position(Pos.TOP_CENTER)
                                    .show()
        
                        } catch (e: Exception) {
                            alert(Alert.AlertType.ERROR, "Failed to update arcs, but previous arcs are reset.", e.message)
                        }
                    }
                }
            }
        }
    }
    
}
