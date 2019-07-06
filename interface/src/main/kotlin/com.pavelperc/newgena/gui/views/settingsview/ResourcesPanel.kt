package com.pavelperc.newgena.gui.views.settingsview

import com.pavelperc.newgena.gui.controller.SettingsUIController
import com.pavelperc.newgena.gui.customfields.*
import com.pavelperc.newgena.gui.views.ResourceGroupsEditor
import com.pavelperc.newgena.gui.views.ResourceMappingEditor
import javafx.event.EventTarget
import javafx.scene.layout.Priority
import tornadofx.*

fun EventTarget.resourcesPanel(controller: SettingsUIController) {
    val time = controller.timeModel
    checkboxField(time.isUsingResources)
    
    foldingFieldSet("Resources", time.isUsingResources) {
        hgrow = Priority.ALWAYS
        
        checkboxField(time.isUsingComplexResourceSettings)
        checkboxField(time.isUsingSynchronizationOnResources)
        
        arrayField(time.simplifiedResources, valuesName = "name")
    
        docField("resourceGroups") {
            readOnlyTextField(time.resourceGroups, { newList ->
                newList.flatMap {
                    it.roles.flatMap {
                        it.resources.map { it.name }
                    }
                }.let { if (it.isEmpty()) "Empty." else it.joinToString("; ") }
            })
            
            button("Edit") {
                action {
                    ResourceGroupsEditor(time.resourceGroups.value) { groups ->
                        time.resourceGroups.value = groups.toMutableList()
                    }.openWindow(escapeClosesWindow = false)
                }
            }
        }
    
        docField("transitionIdsToResources") {
            // TODO status text for transitionIdsToResources 
//                                    val Status = object {
//                                        val incorrect = "Ids doesn't match with model transitions."
//                                        val correct = "Correct."
//                                        val unknown = "Unknown: Petrinet is not loaded or empty"
//                                        val empty = "Empty."
//                                    }
//    
//                                    fun getStatus(): String {
//                                        val delayIds = time.transitionIdsToDelays.value.keys
//                                        val petrinetIds = controller.transitionIdsWithHints.keys
//        
//                                        return when {
//                                            petrinetIds.isEmpty() -> Status.unknown
//                                            delayIds.isEmpty() -> Status.empty
//                                            petrinetIds != delayIds -> Status.incorrect
//                                            else -> Status.correct
//                                        }
//                                    }
//    
//                                    val label = textfield(getStatus()) {
//                                        hgrow = Priority.ALWAYS
//                                        isEditable = false
//                                        style {
//                                            backgroundColor += Color.TRANSPARENT
//                                        }
//                                    }
//                                    time.transitionIdsToDelays.addValidator(label) {
//                                        getStatus().let { status ->
//                                            label.text = status
//                                            when (status) {
//                                                Status.correct, Status.unknown -> null
//                                                else -> warning(status)
//                                            }
//                                        }
//                                    }
//                                    controller.petrinetProp.onChange {
//                                        label.text = getStatus()
//                                    }
            
            button("Edit") {
                action {
                    val transitionsWithHintsAndArtificialEvents = controller.transitionIdsWithHints +
                            controller.noiseModel.artificialNoiseEvents.value
                                    .map { it.activity.toString() to "" }
                                    .toMap()
                    
                    ResourceMappingEditor(
                            time.transitionIdsToResources.value,
                            transitionsWithHintsAndArtificialEvents,
                            time.simplifiedResources.value,
                            time.resourceGroups.value
                    ) { newMapping ->
                        time.transitionIdsToResources.value = newMapping.toMutableMap()
                    }.openWindow(escapeClosesWindow = false)
                }
            }
        }
    }
}