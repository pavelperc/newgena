package com.pavelperc.newgena.gui.views

import com.pavelperc.newgena.gui.app.Styles
import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.event.EventTarget
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
            
            
            arrayField(petrinetSetup.inhibitorArcIds)
            arrayField(petrinetSetup.resetArcIds)
        }
    }
    
}
